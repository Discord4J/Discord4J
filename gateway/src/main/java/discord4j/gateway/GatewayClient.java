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
import discord4j.common.json.payload.GatewayPayload;
import discord4j.common.json.payload.Heartbeat;
import discord4j.common.json.payload.Opcode;
import discord4j.common.json.payload.dispatch.Dispatch;
import discord4j.common.json.payload.dispatch.Ready;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.GatewayStateChange;
import discord4j.gateway.retry.RetryContext;
import discord4j.gateway.retry.RetryOptions;
import discord4j.gateway.websocket.CloseException;
import discord4j.gateway.websocket.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Represents a Discord gateway (websocket) client, implementing its lifecycle.
 * <p>
 * This is the next component downstream from {@link discord4j.gateway.websocket.WebSocketHandler}, that keeps track of
 * a single websocket session. It wraps an instance of {@link discord4j.gateway.DiscordWebSocketHandler} each time a
 * new connection to the gateway is made, therefore only one instance of this class is enough to handle the lifecycle
 * of Discord gateway operations, that could span multiple websocket sessions over time.
 * <p>
 * It provides automatic reconnecting through a configurable retry policy, allows downstream consumers to receive
 * inbound events through {@link #dispatch()} and direct raw payloads through {@link #receiver()} and provides
 * {@link #sender()} to submit events.
 */
public class GatewayClient {

	private static final Logger log = Loggers.getLogger(GatewayClient.class);
	private static final Predicate<? super Throwable> ABNORMAL_ERROR = t ->
			!(t instanceof CloseException) || ((CloseException) t).getCode() != 1000;

	private final WebSocketClient webSocketClient = new WebSocketClient();

	private final PayloadReader payloadReader;
	private final PayloadWriter payloadWriter;
	private final RetryOptions retryOptions;
	private final String token;

	private final EmitterProcessor<Dispatch> dispatch = EmitterProcessor.create(false);
	private final EmitterProcessor<GatewayPayload<?>> receiver = EmitterProcessor.create(false);
	private final EmitterProcessor<GatewayPayload<?>> sender = EmitterProcessor.create(false);
	private final AtomicInteger lastSequence = new AtomicInteger(0);
	private final AtomicReference<String> sessionId = new AtomicReference<>("");
	private final ResettableInterval heartbeat = new ResettableInterval();
	private final FluxSink<Dispatch> dispatchSink;
	private final FluxSink<GatewayPayload<?>> receiverSink;
	private final FluxSink<GatewayPayload<?>> senderSink;

	public GatewayClient(PayloadReader payloadReader, PayloadWriter payloadWriter,
			RetryOptions retryOptions, String token) {
		this.payloadReader = payloadReader;
		this.payloadWriter = payloadWriter;
		this.retryOptions = retryOptions;
		this.token = token;

		// initialize the sinks to safely produce values downstream
		// we use LATEST backpressure handling to avoid overflow on no subscriber situations
		this.dispatchSink = dispatch.sink(FluxSink.OverflowStrategy.LATEST);
		this.receiverSink = receiver.sink(FluxSink.OverflowStrategy.LATEST);
		this.senderSink = sender.sink(FluxSink.OverflowStrategy.LATEST);
	}

	/**
	 * Establish a reconnecting gateway connection to the given URL.
	 *
	 * @param gatewayUrl the URL used to establish a websocket connection
	 * @return a Mono signaling completion
	 */
	public Mono<Void> execute(String gatewayUrl) {
		return Mono.defer(() -> {
			final DiscordWebSocketHandler handler = new DiscordWebSocketHandler(payloadReader, payloadWriter);

			// Internally subscribe to Ready to signal completion of retry mechanism
			Disposable readySub = dispatch.ofType(Ready.class).subscribe(d -> {
				RetryContext retryContext = retryOptions.getRetryContext();
				if (retryContext.getResetCount() == 0) {
					dispatchSink.next(GatewayStateChange.connected());
				} else {
					dispatchSink.next(GatewayStateChange.retrySucceeded(retryContext.getAttempts()));
				}
				retryContext.reset();
			});

			// Subscribe each inbound GatewayPayload to the receiver sink
			Disposable inboundSub = handler.inbound().subscribe(receiverSink::next);

			// Subscribe the receiver to process and transform the inbound payloads into Dispatch events
			Disposable receiverSub = receiver.map(this::updateSequence)
					.map(payload -> payloadContext(payload, handler))
					.subscribe(PayloadHandlers::handle);

			// Subscribe the handler's outbound exchange with our outgoing signals
			// routing error and completion signals to close the gateway
			Disposable senderSub = sender.subscribe(handler.outbound()::onNext, t -> handler.close(), handler::close);

			// Create the heartbeat loop, and subscribe it using the sender sink
			Disposable heartbeatSub = heartbeat.ticks()
					.map(l -> new Heartbeat(lastSequence.get()))
					.map(GatewayPayload::heartbeat)
					.subscribe(handler.outbound()::onNext);

			return webSocketClient.execute(gatewayUrl, handler)
					.doOnTerminate(() -> {
						inboundSub.dispose();
						receiverSub.dispose();
						senderSub.dispose();
						heartbeatSub.dispose();
						readySub.dispose();
						heartbeat.stop();
					});
		}).retryWhen(Retry.<RetryContext>onlyIf(context -> ABNORMAL_ERROR.test(context.exception()))
				.withApplicationContext(retryOptions.getRetryContext())
				.backoff(retryOptions.getBackoff())
				.jitter(retryOptions.getJitter())
				.doOnRetry(context -> {
					int attempt = context.applicationContext().getAttempts();
					long backoff = context.backoff().toMillis();
					log.debug("Retry attempt {} in {} ms", attempt, backoff);
					if (attempt == 1) {
						dispatchSink.next(GatewayStateChange.retryStarted(Duration.ofMillis(backoff)));
					} else {
						dispatchSink.next(GatewayStateChange.retryFailed(attempt - 1, Duration.ofMillis(backoff)));
					}
					context.applicationContext().next();
				})).doOnTerminate(() -> dispatchSink.next(GatewayStateChange.disconnected()));
	}

	private GatewayPayload<?> updateSequence(GatewayPayload<?> payload) {
		if (payload.getSequence() != null) {
			lastSequence.set(payload.getSequence());
		}
		return payload;
	}

	private PayloadContext<?> payloadContext(GatewayPayload<?> payload, DiscordWebSocketHandler handler) {
		return new PayloadContext.Builder()
				.setPayload(payload)
				.setDispatch(dispatchSink)
				.setSender(senderSink)
				.setLastSequence(lastSequence)
				.setSessionId(sessionId)
				.setHeartbeat(heartbeat)
				.setToken(token)
				.setHandler(handler)
				.build();
	}

	/**
	 * Terminates this client's current gateway connection, and optionally, reconnect to it.
	 *
	 * @param reconnect if this client should attempt to reconnect after closing
	 */
	public void close(boolean reconnect) {
		if (reconnect) {
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
}
