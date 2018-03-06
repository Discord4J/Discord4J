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
import discord4j.gateway.retry.GatewayStateChanged;
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
 * inbound events through {@link #dispatch()} and provides {@link #sender()} to submit events.
 */
public class GatewayClient {

	private static final Logger log = Loggers.getLogger(GatewayClient.class);
	private static final Predicate<? super Throwable> ABNORMAL_ERROR = t ->
			!(t instanceof CloseException) || ((CloseException) t).getCode() != 1000;

	private final WebSocketClient webSocketClient = new WebSocketClient();
	private final PayloadReader payloadReader;
	private final PayloadWriter payloadWriter;
	private final RetryOptions retryOptions;

	private final EmitterProcessor<Dispatch> dispatch = EmitterProcessor.create(false);
	private final EmitterProcessor<GatewayPayload<?>> sender = EmitterProcessor.create(false);
	private final AtomicInteger lastSequence = new AtomicInteger(0);
	private final AtomicReference<String> sessionId = new AtomicReference<>("");
	private final ResettableInterval heartbeat = new ResettableInterval();
	private final String token;

	public GatewayClient(PayloadReader payloadReader, PayloadWriter payloadWriter,
			RetryOptions retryOptions, String token) {
		this.payloadReader = payloadReader;
		this.payloadWriter = payloadWriter;
		this.retryOptions = retryOptions;
		this.token = token;
	}

	/**
	 * Establish a reconnecting gateway connection to the given URL.
	 *
	 * @param gatewayUrl the URL used to establish a websocket connection
	 * @return a Mono signaling completion
	 */
	public Mono<Void> execute(String gatewayUrl) {
		return Mono.defer(() -> {
			final DiscordWebSocketHandler wsHandler = new DiscordWebSocketHandler(payloadReader, payloadWriter);

			Disposable readySub = dispatch.ofType(Ready.class).subscribe(d -> {
				RetryContext retryContext = retryOptions.getRetryContext();
				if (retryContext.getResetCount() == 0) {
					dispatch.onNext(GatewayStateChanged.connected());
				} else {
					dispatch.onNext(GatewayStateChanged.retrySucceeded(retryContext.getAttempts()));
				}
				retryContext.reset();
			});
			Disposable inboundSub = wsHandler.inbound()
					.map(this::updateSequence)
					.map(payload -> payloadContext(payload, wsHandler))
					.subscribe(PayloadHandlers::handle);
			Disposable senderSub = sender.subscribe(wsHandler.outbound()::onNext, t -> wsHandler.close(),
					wsHandler::close);

			Disposable heartbeatSub = heartbeat.ticks()
					.map(l -> new Heartbeat(lastSequence.get()))
					.map(GatewayPayload::heartbeat)
					.subscribe(wsHandler.outbound()::onNext);

			return webSocketClient.execute(gatewayUrl, wsHandler)
					.doOnTerminate(() -> {
						inboundSub.dispose();
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
						dispatch.onNext(GatewayStateChanged.retryStarted(Duration.ofMillis(backoff)));
					} else {
						dispatch.onNext(GatewayStateChanged.retryFailed(attempt - 1, Duration.ofMillis(backoff)));
					}
					context.applicationContext().next();
				})).doOnTerminate(() -> dispatch.onNext(GatewayStateChanged.disconnected()));
	}

	private PayloadContext<?> payloadContext(GatewayPayload<?> payload, DiscordWebSocketHandler handler) {
		return new PayloadContext.Builder()
				.setPayload(payload)
				.setDispatch(dispatch)
				.setSender(sender)
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
			sender.onNext(new GatewayPayload<>(Opcode.RECONNECT, null, null, null));
		} else {
			sender.onNext(new GatewayPayload<>()); // trigger a graceful shutdown
			sender.onComplete();
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
	 * Retrieves a new FluxSink to safely produce outbound values. By Reactive Streams Specs Rule 2.12 this can't be
	 * called twice from the same instance (based on object equality).
	 *
	 * @return a serializing FluxSink
	 */
	public FluxSink<GatewayPayload<?>> sender() {
		return sender.sink(FluxSink.OverflowStrategy.ERROR);
	}

	private GatewayPayload<?> updateSequence(GatewayPayload<?> payload) {
		if (payload.getSequence() != null) {
			lastSequence.set(payload.getSequence());
		}
		return payload;
	}
}
