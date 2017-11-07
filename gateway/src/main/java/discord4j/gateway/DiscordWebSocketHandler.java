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

import discord4j.common.GatewayPayload;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.websocket.WebSocketHandler;
import discord4j.gateway.websocket.WebSocketMessage;
import discord4j.gateway.websocket.WebSocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.io.IOException;

public class DiscordWebSocketHandler implements WebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(DiscordWebSocketHandler.class);

	private final ZlibDecompressor decompressor = new ZlibDecompressor();
	private final UnicastProcessor<GatewayPayload> inboundExchange = UnicastProcessor.create();
	private final UnicastProcessor<GatewayPayload> outboundExchange = UnicastProcessor.create();

	private final PayloadReader reader;
	private final PayloadWriter writer;

	public DiscordWebSocketHandler(PayloadReader reader, PayloadWriter writer) {
		this.reader = reader;
		this.writer = writer;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		session.closeFuture()
				.map(status -> new RuntimeException("WebSocket closed with code=" + status.getStatusCode() + " and reason=" + status.getReasonText()))
				.subscribe(inboundExchange::onError);

		session.receive().subscribe(msg -> {
			try {
				decompressor.push(msg.getPayload());
			} catch (IOException e) {
				throw Exceptions.propagate(e);
			}
		}, inboundExchange::onError, this::onComplete);

		decompressor.completeMessages()
				.log("session-inbound")
				.map(reader::read)
				.subscribe(inboundExchange::onNext);

		Mono<Void> sessionEnd = session.send(outboundExchange
				.log("session-outbound")
				.map(writer::write)
				.map(buf -> new WebSocketMessage(WebSocketMessage.Type.TEXT, buf)));

		return sessionEnd;
	}

	private void onComplete() {
		outboundExchange.onComplete();
		inboundExchange.onComplete();
	}

	public UnicastProcessor<GatewayPayload> inbound() {
		return inboundExchange;
	}

	public UnicastProcessor<GatewayPayload> outbound() {
		return outboundExchange;
	}
}
