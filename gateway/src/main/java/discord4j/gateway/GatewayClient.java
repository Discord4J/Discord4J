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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.gateway;

import discord4j.common.ResettableInterval;
import discord4j.common.jackson.Possible;
import discord4j.common.json.payload.*;
import discord4j.common.json.payload.dispatch.Dispatch;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.websocket.CloseException;
import discord4j.gateway.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class GatewayClient {

	private static final Logger log = LoggerFactory.getLogger(GatewayClient.class);
	private static final Predicate<? super Throwable> ABNORMAL_ERROR = t ->
			!(t instanceof CloseException) || ((CloseException) t).getCode() != 1000;

	private static final Map<Opcode<?>, PayloadHandler<?>> HANDLERS = new HashMap<>();

	static {
		addHandler(Opcode.DISPATCH, ctx -> ctx.client.dispatch.onNext(ctx.payload));

		addHandler(Opcode.HELLO, ctx -> {
			Duration interval = Duration.ofMillis(ctx.payload.getHeartbeatInterval());
			ctx.client.heartbeat.start(interval);

			// log trace

			IdentifyProperties props = new IdentifyProperties("linux", "disco", "disco");
			Identify identify = new Identify(ctx.client.token, props, false, 250, Possible.absent(), Possible
					.absent());
			GatewayPayload<Identify> response = GatewayPayload.identify(identify);

			// payloadSender.send(response)
			ctx.handler.outbound().onNext(response);
		});

		addHandler(Opcode.HEARTBEAT_ACK, ctx -> {
			log.debug("Received heartbeat ack.");
		});
	}

	private final WebSocketClient webSocketClient = new WebSocketClient();
	private final EmitterProcessor<Dispatch> dispatch = EmitterProcessor.create(false);
	private final AtomicInteger lastSequence = new AtomicInteger(0);
	private final ResettableInterval heartbeat = new ResettableInterval();

	private final PayloadReader payloadReader;
	private final PayloadWriter payloadWriter;
	private final String token;

	public GatewayClient(PayloadReader payloadReader, PayloadWriter payloadWriter, String token) {
		this.payloadReader = payloadReader;
		this.payloadWriter = payloadWriter;
		this.token = token;
	}

	private static <T extends PayloadData> void addHandler(Opcode<T> op, PayloadHandler<T> handler) {
		HANDLERS.put(op, handler);
	}

	@SuppressWarnings("unchecked")
	private static <T extends PayloadData> PayloadHandler<T> getHandler(Opcode<T> op) {
		return (PayloadHandler<T>) HANDLERS.get(op);
	}

	public Mono<Void> execute(String gatewayUrl) {
		return Mono.defer(() -> {
			final DiscordWebSocketHandler wsHandler = new DiscordWebSocketHandler(payloadReader, payloadWriter);
			wsHandler.inbound().subscribe(payload -> handlePayload(payload, wsHandler));

			heartbeat.ticks()
					.map(l -> new Heartbeat(lastSequence.get()))
					.map(GatewayPayload::heartbeat)
					.subscribe(wsHandler.outbound()::onNext);

			return webSocketClient.execute(gatewayUrl, wsHandler);
		}).doOnError(ABNORMAL_ERROR, t -> heartbeat.stop())
				.retryWhen(Retry.onlyIf(ctx -> ABNORMAL_ERROR.test(ctx.exception()))
						.exponentialBackoffWithJitter(Duration.ofSeconds(5), Duration.ofSeconds(120)));
	}

	public Flux<Dispatch> dispatch() {
		return dispatch;
	}

	private <T extends PayloadData> void handlePayload(GatewayPayload<T> payload, DiscordWebSocketHandler wsHandler) {
		if (payload.getSequence() != null) {
			lastSequence.set(payload.getSequence());
		}

		PayloadHandler<T> payloadHandler = getHandler(payload.getOp());
		if (payloadHandler != null) {
			payloadHandler.handle(new Context<>(this, wsHandler, payload.getData()));
		}
	}

	@FunctionalInterface
	private interface PayloadHandler<T extends PayloadData> {
		void handle(Context<T> context);
	}

	private static class Context<T extends PayloadData> {

		private final GatewayClient client;
		private final DiscordWebSocketHandler handler;
		@Nullable
		private final T payload;

		Context(GatewayClient client, DiscordWebSocketHandler handler, @Nullable T payload) {
			this.client = client;
			this.handler = handler;
			this.payload = payload;
		}
	}
}
