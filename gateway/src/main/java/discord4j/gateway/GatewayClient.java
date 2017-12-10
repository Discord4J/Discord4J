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
import reactor.retry.Backoff;
import reactor.retry.Retry;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GatewayClient {

	private static final Logger log = LoggerFactory.getLogger(GatewayClient.class);

	private static final Set<PayloadHandler<?>> HANDLERS = new LinkedHashSet<>();

	static {
		HANDLERS.add(new PayloadHandler<>(Dispatch.class, context -> context.client.dispatch.onNext(context
				.payload)));
		HANDLERS.add(new PayloadHandler<>(Hello.class, context -> {
			Duration interval = Duration.ofMillis(context.payload.getHeartbeatInterval());
			context.client.heartbeat.start(interval);

			// log trace

			IdentifyProperties props = new IdentifyProperties("linux", "disco", "disco");
			Identify identify = new Identify(context.client.token, props, false, 250, Possible.absent(), Possible
					.absent());
			GatewayPayload response = new GatewayPayload(Opcodes.IDENTIFY, identify, null, null);

			// payloadSender.send(response)
			context.handler.outbound().onNext(response);
		}));
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

	public Mono<Void> execute(String gatewayUrl) {
		return Mono.defer(() -> {
			final DiscordWebSocketHandler wsHandler = new DiscordWebSocketHandler(payloadReader, payloadWriter);
			wsHandler.inbound().subscribe(payload -> handlePayload(payload, wsHandler));

			heartbeat.ticks()
					.map(l -> new GatewayPayload(Opcodes.HEARTBEAT, new Heartbeat(lastSequence.get()), null, null))
					.subscribe(wsHandler.outbound()::onNext);

			return webSocketClient.execute(gatewayUrl, wsHandler);
		}).retryWhen(Retry.onlyIf(ctx -> {
			Throwable t = ctx.exception();
			return !(t instanceof CloseException) || ((CloseException) t).getCode() != 1000;
		}).backoff(Backoff.fixed(Duration.ofSeconds(5))).retryMax(5));
	}

	public Flux<Dispatch> dispatch() {
		return dispatch;
	}

	private <T extends GatewayPayload> void handlePayload(T payload, DiscordWebSocketHandler handler) {
		if (payload.getSequence() != null) {
			lastSequence.set(payload.getSequence());
		}
		HANDLERS.stream()
				.filter(h -> h.canHandle(payload.getData()))
				.findFirst()
				.map(GatewayClient::cast)
				.ifPresent(h -> h.handle(new Context<>(this, handler, payload.getData())));
	}

	private static class PayloadHandler<T extends Payload> {

		private final Class<T> type;
		private final Consumer<Context<T>> consumer;

		PayloadHandler(Class<T> type, Consumer<Context<T>> consumer) {
			this.type = type;
			this.consumer = consumer;
		}

		public boolean canHandle(Payload obj) {
			return type.isInstance(obj);
		}

		public void handle(Context<T> context) {
			consumer.accept(context);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			PayloadHandler<?> that = (PayloadHandler<?>) o;
			return Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type);
		}
	}

	private static class Context<T extends Payload> {

		private final GatewayClient client;
		private final DiscordWebSocketHandler handler;
		private final T payload;

		Context(GatewayClient client, DiscordWebSocketHandler handler, T payload) {
			this.client = client;
			this.handler = handler;
			this.payload = payload;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Payload> PayloadHandler<T> cast(PayloadHandler<?> handler) {
		return (PayloadHandler<T>) handler;
	}
}
