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
import discord4j.common.json.payload.PayloadData;
import discord4j.common.json.payload.dispatch.Dispatch;
import discord4j.common.json.payload.dispatch.Ready;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.websocket.CloseException;
import discord4j.gateway.websocket.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.retry.BackoffDelay;
import reactor.retry.Jitter;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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
 * It provides automatic reconnecting through exponential backoff with jitter policy, provides {@link #dispatch()}
 * to subscribe to gateway events coming inbound and {@link #sender()} to submit events going outbound.
 */
public class GatewayClient {

	private static final Logger log = Loggers.getLogger(GatewayClient.class);
	private static final Predicate<? super Throwable> ABNORMAL_ERROR = t ->
			!(t instanceof CloseException) || ((CloseException) t).getCode() != 1000;
	private static final Map<Opcode<?>, PayloadHandler<?>> HANDLERS = new HashMap<>();

	static {
		addHandler(Opcode.DISPATCH, PayloadHandlers::handleDispatch);
		addHandler(Opcode.HEARTBEAT, PayloadHandlers::handleHeartbeat);
		addHandler(Opcode.RECONNECT, PayloadHandlers::handleReconnect);
		addHandler(Opcode.INVALID_SESSION, PayloadHandlers::handleInvalidSession);
		addHandler(Opcode.HELLO, PayloadHandlers::handleHello);
		addHandler(Opcode.HEARTBEAT_ACK, PayloadHandlers::handleHeartbeatAck);
	}

	private final WebSocketClient webSocketClient = new WebSocketClient();
	private final PayloadReader payloadReader;
	private final PayloadWriter payloadWriter;

	final EmitterProcessor<Dispatch> dispatch = EmitterProcessor.create(false);
	final EmitterProcessor<GatewayPayload<?>> sender = EmitterProcessor.create(false);
	final AtomicInteger lastSequence = new AtomicInteger(0);
	final AtomicReference<String> sessionId = new AtomicReference<>("");
	final ResettableInterval heartbeat = new ResettableInterval();
	final String token;

	public GatewayClient(PayloadReader payloadReader, PayloadWriter payloadWriter, String token) {
		this.payloadReader = payloadReader;
		this.payloadWriter = payloadWriter;
		this.token = token;
	}

	/**
	 * Establish a reconnecting gateway connection to the given URL.
	 *
	 * @param gatewayUrl the URL used to establish a websocket connection
	 * @return a Mono signaling completion
	 */
	public Mono<Void> execute(String gatewayUrl) {
		RetryContext retryContext = new RetryContext(Duration.ofSeconds(5), Duration.ofSeconds(120));
		return Mono.defer(() -> {
			final DiscordWebSocketHandler wsHandler = new DiscordWebSocketHandler(payloadReader, payloadWriter);

			Disposable readySub = dispatch.ofType(Ready.class).subscribe(d -> retryContext.reset());
			Disposable inboundSub = wsHandler.inbound().subscribe(payload -> handlePayload(payload, wsHandler));
			Disposable senderSub = sender.map(payload -> {
				if (Opcode.RECONNECT.equals(payload.getOp())) {
					wsHandler.error(new RuntimeException("Reconnecting due to user action"));
				}
				return payload;
			}).subscribe(wsHandler.outbound()::onNext, t -> wsHandler.close(), wsHandler::close);

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
		}).retryWhen(Retry.<RetryContext>onlyIf(ctx -> ABNORMAL_ERROR.test(ctx.exception()))
				.withApplicationContext(retryContext)
				.backoff(context -> {
					RetryContext appContext = (RetryContext) context.applicationContext();
					Duration nextBackoff;
					try {
						long factor = (long) Math.pow(2, (appContext.attempts - 1));
						nextBackoff = appContext.firstBackoff.multipliedBy(factor);
					} catch (ArithmeticException e) {
						nextBackoff = appContext.maxBackoffInterval;
					}
					return new BackoffDelay(appContext.firstBackoff, appContext.maxBackoffInterval, nextBackoff);
				})
				.jitter(Jitter.random())
				.doOnRetry(context -> {
					log.info("Attempt {} retrying in {} ms",
							context.applicationContext().attempts, context.backoff().toMillis());
					context.applicationContext().next();
				}));
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

	@SuppressWarnings("unchecked")
	private static <T extends PayloadData> PayloadHandler<T> getHandler(Opcode<T> op) {
		return (PayloadHandler<T>) HANDLERS.get(op);
	}

	private static <T extends PayloadData> void addHandler(Opcode<T> op, PayloadHandler<T> handler) {
		HANDLERS.put(op, handler);
	}

	private <T extends PayloadData> void handlePayload(GatewayPayload<T> payload, DiscordWebSocketHandler wsHandler) {
		if (payload.getSequence() != null) {
			lastSequence.set(payload.getSequence());
		}

		PayloadHandler<T> payloadHandler = getHandler(payload.getOp());
		if (payloadHandler != null) {
			payloadHandler.handle(new PayloadContext<>(this, wsHandler, payload.getData()));
		} else {
			log.warn("Received GatewayPayload with no registered handler: " + payload.getOp());
		}
	}

	private static class RetryContext {

		final Duration firstBackoff;
		final Duration maxBackoffInterval;
		boolean connected = false;
		int attempts = 1;

		private RetryContext(Duration firstBackoff, Duration maxBackoffInterval) {
			this.firstBackoff = firstBackoff;
			this.maxBackoffInterval = maxBackoffInterval;
		}

		void next() {
			connected = false;
			attempts++;
		}

		void reset() {
			connected = true;
			attempts = 1;
		}
	}
}
