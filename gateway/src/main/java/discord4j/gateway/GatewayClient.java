/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.gateway;

import discord4j.common.ResettableInterval;
import discord4j.common.close.CloseException;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.Heartbeat;
import discord4j.gateway.json.Opcode;
import discord4j.gateway.json.dispatch.Dispatch;
import discord4j.gateway.json.dispatch.Ready;
import discord4j.gateway.json.dispatch.Resumed;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.GatewayStateChange;
import discord4j.gateway.retry.RetryContext;
import discord4j.gateway.retry.RetryOptions;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;

/**
 * Represents a Discord WebSocket client, called Gateway, implementing its lifecycle.
 * <p>
 * Keeps track of a single websocket session by wrapping an instance of
 * {@link discord4j.gateway.DiscordWebSocketHandler}
 * each time a new WebSocket connection to Discord is made, therefore only one instance of this class is enough to
 * handle the lifecycle of the Gateway operations, that could span multiple WebSocket sessions over time.
 * <p>
 * Provides automatic reconnecting through a configurable retry policy, allows downstream consumers to receive
 * inbound events through {@link #dispatch()} and direct raw payloads through {@link #receiver()} and allows a user to
 * submit events through {@link #sender()}.
 */
public class GatewayClient {

    private final Logger log;
    private final PayloadReader payloadReader;
    private final PayloadWriter payloadWriter;
    private final RetryOptions retryOptions;
    private final IdentifyOptions identifyOptions;
    private final String token;
    private final GatewayObserver observer;
    private final GatewayLimiter limiter;

    private final EmitterProcessor<Dispatch> dispatch = EmitterProcessor.create(false);
    private final EmitterProcessor<GatewayPayload<?>> receiver = EmitterProcessor.create(false);
    private final EmitterProcessor<GatewayPayload<?>> sender = EmitterProcessor.create(false);
    private final EmitterProcessor<GatewayPayload<Heartbeat>> heartbeats = EmitterProcessor.create(false);

    private final AtomicBoolean resumable = new AtomicBoolean(true);
    private final AtomicInteger sequence = new AtomicInteger(0);
    private final AtomicLong lastSent = new AtomicLong(0);
    private final AtomicLong lastAck = new AtomicLong(0);
    private final AtomicLong responseTime = new AtomicLong(0);
    private final ResettableInterval heartbeat = new ResettableInterval();
    private final AtomicReference<String> sessionId = new AtomicReference<>("");

    private final AtomicBoolean connected = new AtomicBoolean(false);

    private final FluxSink<Dispatch> dispatchSink;
    private final FluxSink<GatewayPayload<?>> receiverSink;
    private final FluxSink<GatewayPayload<?>> senderSink;
    private final FluxSink<GatewayPayload<Heartbeat>> heartbeatSink;

    /**
     * Initializes a new GatewayClient.
     *
     * @param payloadReader strategy to read and decode incoming gateway messages
     * @param payloadWriter strategy to encode and write outgoing gateway messages
     * @param retryOptions reconnect policy used in this client
     * @param token Discord bot token
     * @param identifyOptions used to IDENTIFY or RESUME a gateway connection, specifying the sharding options
     *         and to set an initial presence
     * @param observer consumer observing gateway and underlying websocket lifecycle changes
     * @param limiter rate-limiting policy used for IDENTIFY requests, allowing shard coordination
     */
    public GatewayClient(PayloadReader payloadReader, PayloadWriter payloadWriter,
            RetryOptions retryOptions, String token, IdentifyOptions identifyOptions,
            GatewayObserver observer, GatewayLimiter limiter) {
        this.log = Loggers.getLogger("discord4j.gateway.client." + identifyOptions.getShardIndex());
        this.payloadReader = Objects.requireNonNull(payloadReader);
        this.payloadWriter = Objects.requireNonNull(payloadWriter);
        this.retryOptions = Objects.requireNonNull(retryOptions);
        this.token = Objects.requireNonNull(token);
        this.identifyOptions = Objects.requireNonNull(identifyOptions);
        this.observer = observer;
        this.limiter = limiter;
        this.dispatchSink = dispatch.sink(FluxSink.OverflowStrategy.LATEST);
        this.receiverSink = receiver.sink(FluxSink.OverflowStrategy.LATEST);
        this.senderSink = sender.sink(FluxSink.OverflowStrategy.LATEST);
        this.heartbeatSink = heartbeats.sink(FluxSink.OverflowStrategy.LATEST);
    }

    /**
     * Establish a reconnecting gateway connection to the given URL.
     *
     * @param gatewayUrl the URL used to establish a websocket connection
     * @return a Mono signaling completion
     */
    public Mono<Void> execute(String gatewayUrl) {
        return Mono.defer(() -> {
            final int shard = identifyOptions.getShardIndex();
            final DiscordWebSocketHandler handler = new DiscordWebSocketHandler(payloadReader, payloadWriter,
                    receiverSink, sender, heartbeats, shard, limiter);

            if (identifyOptions.getResumeSequence() != null) {
                this.sequence.set(identifyOptions.getResumeSequence());
                this.sessionId.set(identifyOptions.getResumeSessionId());
            } else {
                resumable.set(false);
            }

            lastAck.set(System.currentTimeMillis());

            Mono<Void> readyHandler = dispatch.filter(GatewayClient::isReadyOrResume)
                    .flatMap(event -> {
                        connected.compareAndSet(false, true);
                        RetryContext retryContext = retryOptions.getRetryContext();
                        ConnectionObserver.State state;
                        if (retryContext.getResetCount() == 0) {
                            log.info("Connected to Gateway");
                            dispatchSink.next(GatewayStateChange.connected());
                            state = GatewayObserver.CONNECTED;
                        } else {
                            log.info("Reconnected to Gateway");
                            dispatchSink.next(GatewayStateChange.retrySucceeded(retryContext.getAttempts()));
                            state = GatewayObserver.RETRY_SUCCEEDED;
                        }
                        retryContext.reset();
                        identifyOptions.setResumeSessionId(sessionId.get());
                        resumable.set(true);
                        notifyObserver(state, identifyOptions);
                        return Mono.just(event);
                    })
                    .then();

            // Subscribe the receiver to process and transform the inbound payloads into Dispatch events
            Mono<Void> receiverFuture = receiver.map(this::updateSequence)
                    .map(payload -> payloadContext(payload, handler, this))
                    .doOnNext(PayloadHandlers::handle)
                    .doOnComplete(() -> log.debug("Receiver future completed"))
                    .then();

            // Subscribe the handler's outbound exchange with our outgoing signals
            // routing error and completion signals to close the gateway
            Mono<Void> senderFuture = sender.doOnError(t -> handler.close())
                    .doOnComplete(handler::close)
                    .doOnComplete(() -> log.debug("Sender future completed"))
                    .then();

            // Create the heartbeat loop, and subscribe it using the sender sink
            Mono<Void> heartbeatHandler = heartbeat.ticks()
                    .flatMap(t -> {
                        long delay = System.currentTimeMillis() - lastAck.get();
                        if (delay > heartbeat.getPeriod().toMillis() + getResponseTime()) {
                            log.warn("Missing heartbeat ACK for {} ms", delay);
                            handler.error(new RuntimeException("Reconnecting due to zombie or failed connection"));
                            return Mono.empty();
                        } else {
                            log.debug("Sending heartbeat {} ms after last ACK", delay);
                            lastSent.set(System.currentTimeMillis());
                            return Mono.just(GatewayPayload.heartbeat(new Heartbeat(sequence.get())));
                        }
                    })
                    .doOnNext(heartbeatSink::next)
                    .then();

            Mono<Void> httpFuture = HttpClient.create()
                    .headers(headers -> headers.add(USER_AGENT, "DiscordBot(https://discord4j.com, 3)"))
                    .observe(observer())
                    .websocket(Integer.MAX_VALUE)
                    .uri(gatewayUrl)
                    .handle(handler::handle)
                    .doOnComplete(() -> log.debug("WebSocket future complete"))
                    .doOnError(t -> log.debug("WebSocket future threw an error", t))
                    .doOnCancel(() -> log.debug("WebSocket future cancelled"))
                    .doOnTerminate(heartbeat::stop)
                    .then();

            return Mono.zip(httpFuture, readyHandler, receiverFuture, senderFuture, heartbeatHandler)
                    .doOnError(t -> log.error("Gateway client error", t))
                    .doOnCancel(() -> close(false))
                    .then();
        })
                .retryWhen(retryFactory())
                .doOnCancel(logDisconnected())
                .doOnTerminate(logDisconnected());
    }

    private static boolean isReadyOrResume(Dispatch d) {
        return Ready.class.isAssignableFrom(d.getClass()) || Resumed.class.isAssignableFrom(d.getClass());
    }

    private GatewayPayload<?> updateSequence(GatewayPayload<?> payload) {
        if (payload.getSequence() != null) {
            sequence.set(payload.getSequence());
            identifyOptions.setResumeSequence(sequence.get());
            notifyObserver(GatewayObserver.SEQUENCE, identifyOptions);
        }
        return payload;
    }

    private PayloadContext<?> payloadContext(GatewayPayload<?> payload, DiscordWebSocketHandler handler,
            GatewayClient client) {
        return new PayloadContext<>(payload, handler, client);
    }

    private Retry<RetryContext> retryFactory() {
        return Retry.<RetryContext>any()
                .withApplicationContext(retryOptions.getRetryContext())
                .backoff(retryOptions.getBackoff())
                .jitter(retryOptions.getJitter())
                .retryMax(retryOptions.getMaxRetries())
                .doOnRetry(context -> {
                    connected.compareAndSet(true, false);
                    int attempt = context.applicationContext().getAttempts();
                    long backoff = context.backoff().toMillis();
                    log.info("Retry attempt {} in {} ms", attempt, backoff);
                    if (attempt == 1) {
                        dispatchSink.next(GatewayStateChange.retryStarted(Duration.ofMillis(backoff)));
                        if (!resumable.get() || !isResumableError(context.exception())) {
                            resumable.compareAndSet(true, false);
                            notifyObserver(GatewayObserver.RETRY_STARTED, identifyOptions);
                        } else {
                            notifyObserver(GatewayObserver.RETRY_RESUME_STARTED, identifyOptions);
                        }
                    } else {
                        dispatchSink.next(GatewayStateChange.retryFailed(attempt - 1,
                                Duration.ofMillis(backoff)));
                        // TODO: add attempt/backoff values to GatewayObserver
                        notifyObserver(GatewayObserver.RETRY_FAILED, identifyOptions);
                        resumable.set(false);
                    }
                    context.applicationContext().next();
                });
    }

    private boolean isResumableError(Throwable t) {
        if (t instanceof CloseException) {
            CloseException closeException = (CloseException) t;
            return closeException.getCode() < 4000;
        }
        return true;
    }

    private Runnable logDisconnected() {
        return () -> {
            log.info("Disconnected from Gateway");
            connected.compareAndSet(true, false);
            dispatchSink.next(GatewayStateChange.disconnected());
            notifyObserver(GatewayObserver.DISCONNECTED, identifyOptions);
        };
    }

    private ConnectionObserver observer() {
        return (connection, newState) -> {
            log.debug("{} {}", newState, connection);
            observer.onStateChange(newState, identifyOptions);
        };
    }

    private void notifyObserver(ConnectionObserver.State state, IdentifyOptions options) {
        observer.onStateChange(state, options);
    }

    /**
     * Terminates this client's current gateway connection, and optionally, reconnect to it.
     *
     * @param reconnect if this client should attempt to reconnect after closing
     */
    public void close(boolean reconnect) {
        if (reconnect) {
            resumable.set(false);
            senderSink.next(new GatewayPayload<>(Opcode.RECONNECT, null, null, null));
        } else {
            senderSink.next(new GatewayPayload<>());
            senderSink.complete();
        }
    }

    /**
     * Obtains the Flux of Dispatch events inbound from the gateway connection made by this client.
     * <p>
     * Can be used like this, for example, to get all created message events:
     * <pre>
     * gatewayClient.dispatch().ofType(MessageCreate.class)
     *     .subscribe(message -&gt; {
     *         System.out.println("Got a message with content: " + message.getMessage().getContent());
     * });
     * </pre>
     *
     * @return a Flux of Dispatch values
     */
    public Flux<Dispatch> dispatch() {
        return dispatch;
    }

    /**
     * Obtains the Flux of raw payloads inbound from the gateway connection made by this client.
     *
     * @return a Flux of GatewayPayload values
     */
    public Flux<GatewayPayload<?>> receiver() {
        return receiver;
    }

    /**
     * Retrieves a new FluxSink to safely produce outbound values. By Reactive Streams Specs Rule 2.12 this can't be
     * called twice from the same instance (based on object equality).
     *
     * @return a serializing FluxSink
     */
    public FluxSink<GatewayPayload<?>> sender() {
        return senderSink;
    }

    /**
     * Retrieve the ID of the current gateway session.
     *
     * @return the ID of the current gateway session. Used for resuming and voice.
     */
    public String getSessionId() {
        return sessionId.get();
    }

    /**
     * Gets the current heartbeat sequence.
     *
     * @return an integer representing the current gateway sequence
     */
    public int getSequence() {
        return sequence.get();
    }

    /**
     * Returns whether this GatewayClient is currently connected to Discord Gateway therefore capable to send and
     * receive payloads.
     *
     * @return true if the gateway connection is currently established, false otherwise.
     */
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * Gets the amount of time it last took Discord to respond to a heartbeat with an ack.
     *
     * @return the time in milliseconds took Discord to respond to the last heartbeat with an ack.
     */
    public long getResponseTime() {
        return responseTime.get();
    }

    /////////////////////////////////
    // Methods for PayloadHandlers //
    /////////////////////////////////

    void ackHeartbeat() {
        lastAck.set(System.currentTimeMillis());
        responseTime.set(lastAck.get() - lastSent.get());
    }

    ////////////////////////////////
    // Fields for PayloadHandlers //
    ////////////////////////////////

    /**
     * Obtains the FluxSink to send Dispatch events towards GatewayClient's users.
     *
     * @return a {@link reactor.core.publisher.FluxSink} for {@link discord4j.gateway.json.dispatch.Dispatch}
     *         objects
     */
    FluxSink<Dispatch> dispatchSink() {
        return dispatchSink;
    }

    /**
     * Gets the atomic reference for the current heartbeat sequence.
     *
     * @return an AtomicInteger representing the current gateway sequence
     */
    AtomicInteger sequence() {
        return sequence;
    }

    /**
     * Gets the atomic reference for the current session ID.
     *
     * @return an AtomicReference of the String representing the current session ID
     */
    AtomicReference<String> sessionId() {
        return sessionId;
    }

    /**
     * Gets the heartbeat manager bound to this GatewayClient.
     *
     * @return a {@link discord4j.common.ResettableInterval} to manipulate heartbeat operations
     */
    ResettableInterval heartbeat() {
        return heartbeat;
    }

    /**
     * Gets the token used to connect to the gateway.
     *
     * @return a token String
     */
    String token() {
        return token;
    }

    /**
     * An boolean value indicating if this client will attempt to RESUME.
     *
     * @return an AtomicBoolean representing resume capabilities
     */
    AtomicBoolean resumable() {
        return resumable;
    }

    /**
     * Gets the configuration object for gateway identifying procedure.
     *
     * @return an IdentifyOptions configuration object
     */
    IdentifyOptions identifyOptions() {
        return identifyOptions;
    }

    /**
     * Gets the configuration object for gateway reconnection procedure.
     *
     * @return a RetryOptions configuration object
     */
    RetryOptions retryOptions() {
        return retryOptions;
    }
}
