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
import discord4j.gateway.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class GatewayClient {

	private static final Logger log = LoggerFactory.getLogger(GatewayClient.class);

	private final WebSocketClient webSocketClient = new WebSocketClient();
	private final DiscordWebSocketHandler wsHandler;
	private final EmitterProcessor<Dispatch> dispatch = EmitterProcessor.create(false);
	private final String token;

	private final AtomicInteger lastSequence = new AtomicInteger(0);
	private final ResettableInterval heartbeatHandler = new ResettableInterval();

	public GatewayClient(PayloadReader payloadReader, PayloadWriter payloadWriter, String token) {
		this.wsHandler = new DiscordWebSocketHandler(payloadReader, payloadWriter);
		this.token = token;
	}

	public Mono<Void> execute(String gatewayUrl) {
		wsHandler.inbound().subscribe(this::handlePayload, error -> {
			log.warn("Gateway connection terminated: {}", error.toString());
		});

		heartbeatHandler.subscribe(l -> {
			GatewayPayload heartbeat =
					new GatewayPayload(Opcodes.HEARTBEAT, new Heartbeat(lastSequence.get()), null, null);
			wsHandler.outbound().onNext(heartbeat);
		});

		return webSocketClient.execute(gatewayUrl, wsHandler);
	}

	public Flux<Dispatch> dispatch() {
		return dispatch;
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
			wsHandler.outbound().onNext(response);
		}
	}
}
