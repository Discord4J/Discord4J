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

import discord4j.common.json.payload.dispatch.Dispatch;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class GatewayClient {

	private static final Logger log = LoggerFactory.getLogger(GatewayClient.class);

	private final WebSocketClient webSocketClient = new WebSocketClient();
	private final AtomicInteger lastSequence = new AtomicInteger();
	//private final HeartbeatHandler heartbeatHandler = new HeartbeatHandler(lastSequence);
	private final DiscordWebSocketHandler wsHandler;
	private final Flux<Dispatch> dispatch;

	public GatewayClient(PayloadReader payloadReader, PayloadWriter payloadWriter) {
		this.wsHandler = new DiscordWebSocketHandler(payloadReader, payloadWriter);
		this.dispatch = wsHandler.inbound().ofType(Dispatch.class);
	}

	public Mono<Void> execute(String gatewayUrl) {
		wsHandler.inbound().subscribe(payload -> {

		}, error -> {
			log.warn("Gateway connection terminated: {}", error.toString());
		});

		return webSocketClient.execute(gatewayUrl, wsHandler);
	}
}
