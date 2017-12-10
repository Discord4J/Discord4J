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
import reactor.core.publisher.UnicastProcessor;
import reactor.retry.Backoff;
import reactor.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class GatewayClient {

	private static final Logger log = LoggerFactory.getLogger(GatewayClient.class);

	private final WebSocketClient webSocketClient = new WebSocketClient();
	private final EmitterProcessor<Dispatch> dispatch = EmitterProcessor.create(false);
	private final AtomicInteger lastSequence = new AtomicInteger(0);
	private final ResettableInterval heartbeatHandler = new ResettableInterval();

	private final PayloadReader payloadReader;
	private final PayloadWriter payloadWriter;
	private final String token;

	private UnicastProcessor<GatewayPayload> outbound;

	public GatewayClient(PayloadReader payloadReader, PayloadWriter payloadWriter, String token) {
		this.payloadReader = payloadReader;
		this.payloadWriter = payloadWriter;
		this.token = token;
	}

	public Mono<Void> execute(String gatewayUrl) {
		return Mono.defer(() -> {
			final DiscordWebSocketHandler wsHandler = new DiscordWebSocketHandler(payloadReader, payloadWriter);
			this.outbound = wsHandler.outbound();

			wsHandler.inbound().subscribe(this::handlePayload);

			heartbeatHandler.out()
					.map(l -> new GatewayPayload(Opcodes.HEARTBEAT, new Heartbeat(lastSequence.get()), null, null))
					.subscribe(outbound()::onNext);

			return webSocketClient.execute(gatewayUrl, wsHandler);
		}).retryWhen(Retry.onlyIf(ctx -> {
			Throwable t = ctx.exception();
			return !(t instanceof CloseException) || ((CloseException) t).getCode() != 1000;
		}).backoff(Backoff.fixed(Duration.ofSeconds(5))).retryMax(5));
	}

	public Flux<Dispatch> dispatch() {
		return dispatch;
	}

	public UnicastProcessor<GatewayPayload> outbound() {
		return outbound;
	}

	private <T extends GatewayPayload> void handlePayload(T payload) {
		if (payload.getSequence() != null) {
			lastSequence.set(payload.getSequence());
		}

		// TODO: can something go here besides a long if else?
		Payload data = payload.getData();
		if (data instanceof Dispatch) {
			dispatch.onNext((Dispatch) data);
		} else if (data instanceof Hello) {
			Duration interval = Duration.ofMillis(((Hello) data).getHeartbeatInterval());
			heartbeatHandler.start(interval);

			// log trace

			IdentifyProperties props = new IdentifyProperties("linux", "disco", "disco");
			Identify identify = new Identify(token, props, false, 250, Possible.absent(), Possible.absent());
			GatewayPayload response = new GatewayPayload(Opcodes.IDENTIFY, identify, null, null);

			// payloadSender.send(response)
			outbound().onNext(response);
		}
	}
}
