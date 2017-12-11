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

import discord4j.common.json.payload.GatewayPayload;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.websocket.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.UnicastProcessor;

import java.util.logging.Level;

public class DiscordWebSocketHandler implements WebSocketHandler {

	private final ZlibDecompressor decompressor = new ZlibDecompressor();
	private final UnicastProcessor<GatewayPayload<?>> inboundExchange = UnicastProcessor.create();
	private final UnicastProcessor<GatewayPayload<?>> outboundExchange = UnicastProcessor.create();
	private final MonoProcessor<Void> completionNotifier = MonoProcessor.create();

	private final PayloadReader reader;
	private final PayloadWriter writer;

	public DiscordWebSocketHandler(PayloadReader reader, PayloadWriter writer) {
		this.reader = reader;
		this.writer = writer;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		session.closeFuture()
				.map(CloseException::new)
				.subscribe(completionNotifier::onError);

		session.receive()
				.map(WebSocketMessage::getPayload)
				.compose(decompressor::completeMessages)
				.map(reader::read)
				.log("discord4j.gateway.session.inbound", Level.FINE)
				.subscribe(inboundExchange::onNext, this::onError, completionNotifier::onComplete);

		session.send(outboundExchange
				.log("discord4j.gateway.session.outbound", Level.FINE)
				.map(writer::write)
				.map(buf -> new WebSocketMessage(WebSocketMessage.Type.TEXT, buf)))
				.subscribe();

		return completionNotifier;
	}

	public void close() {
		outboundExchange.onComplete();
		inboundExchange.onComplete();
		completionNotifier.onComplete();
	}

	public UnicastProcessor<GatewayPayload<?>> inbound() {
		return inboundExchange;
	}

	public UnicastProcessor<GatewayPayload<?>> outbound() {
		return outboundExchange;
	}

	private void onError(Throwable error) {
		completionNotifier.onError(new CloseException(new CloseStatus(1006, error.toString()), error));
	}
}
