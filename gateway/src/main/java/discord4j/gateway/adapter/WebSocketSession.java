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
package discord4j.gateway.adapter;

import discord4j.gateway.HandshakeInfo;
import discord4j.gateway.WebSocketMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.NettyPipeline;
import reactor.ipc.netty.http.websocket.WebsocketInbound;
import reactor.ipc.netty.http.websocket.WebsocketOutbound;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Spring WebSocketSession implementation that adapts to Reactor Netty's WebSocket {@link
 * reactor.ipc.netty.NettyInbound} and {@link reactor.ipc.netty.NettyOutbound}.
 *
 * @author Rossen Stoyanchev
 */
public class WebSocketSession {


	public WebSocketSession(WebsocketInbound inbound, WebsocketOutbound outbound,
			HandshakeInfo info, ByteBufAllocator byteBufAllocator) {

		WebSocketConnection delegate = new WebSocketConnection(inbound, outbound);

		Objects.requireNonNull(delegate, "Native session is required.");
		Objects.requireNonNull(info, "HandshakeInfo is required.");
		Objects.requireNonNull(byteBufAllocator, "ByteBuf allocator is required.");

		this.delegate = delegate;
		this.id = Integer.toHexString(System.identityHashCode(delegate));
		this.handshakeInfo = info;
		this.byteBufAllocator = byteBufAllocator;
	}

	/**
	 * The default max size for aggregating inbound WebSocket frames.
	 */
	protected static final int DEFAULT_FRAME_MAX_SIZE = 64 * 1024;


	private static final Map<Class<?>, WebSocketMessage.Type> MESSAGE_TYPES;

	static {
		MESSAGE_TYPES = new HashMap<>(4);
		MESSAGE_TYPES.put(TextWebSocketFrame.class, WebSocketMessage.Type.TEXT);
		MESSAGE_TYPES.put(BinaryWebSocketFrame.class, WebSocketMessage.Type.BINARY);
		MESSAGE_TYPES.put(PingWebSocketFrame.class, WebSocketMessage.Type.PING);
		MESSAGE_TYPES.put(PongWebSocketFrame.class, WebSocketMessage.Type.PONG);
	}

	private final WebSocketConnection delegate;
	private final String id;
	private final HandshakeInfo handshakeInfo;
	private final ByteBufAllocator byteBufAllocator;


	protected WebSocketConnection getDelegate() {
		return this.delegate;
	}

	public String getId() {
		return this.id;
	}

	public HandshakeInfo getHandshakeInfo() {
		return this.handshakeInfo;
	}


	// WebSocketMessage factory methods

	public WebSocketMessage textMessage(String payload) {
		byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
		ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
		return new WebSocketMessage(WebSocketMessage.Type.TEXT, buffer);
	}

	public WebSocketMessage binaryMessage(Function<ByteBufAllocator, ByteBuf> payloadFactory) {
		ByteBuf payload = payloadFactory.apply(byteBufAllocator());
		return new WebSocketMessage(WebSocketMessage.Type.BINARY, payload);
	}

	public WebSocketMessage pingMessage(Function<ByteBufAllocator, ByteBuf> payloadFactory) {
		ByteBuf payload = payloadFactory.apply(byteBufAllocator());
		return new WebSocketMessage(WebSocketMessage.Type.PING, payload);
	}

	public WebSocketMessage pongMessage(Function<ByteBufAllocator, ByteBuf> payloadFactory) {
		ByteBuf payload = payloadFactory.apply(byteBufAllocator());
		return new WebSocketMessage(WebSocketMessage.Type.PONG, payload);
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id=" + getId() + ", uri=" + getHandshakeInfo().getUri() + "]";
	}


	public ByteBufAllocator byteBufAllocator() {
		return this.byteBufAllocator;
	}


	protected WebSocketMessage toMessage(WebSocketFrame frame) {
		ByteBuf payload = frame.content();
		return new WebSocketMessage(MESSAGE_TYPES.get(frame.getClass()), payload);
	}

	protected WebSocketFrame toFrame(WebSocketMessage message) {
		ByteBuf byteBuf = message.getPayload();
		if (WebSocketMessage.Type.TEXT.equals(message.getType())) {
			return new TextWebSocketFrame(byteBuf);
		} else if (WebSocketMessage.Type.BINARY.equals(message.getType())) {
			return new BinaryWebSocketFrame(byteBuf);
		} else if (WebSocketMessage.Type.PING.equals(message.getType())) {
			return new PingWebSocketFrame(byteBuf);
		} else if (WebSocketMessage.Type.PONG.equals(message.getType())) {
			return new PongWebSocketFrame(byteBuf);
		} else {
			throw new IllegalArgumentException("Unexpected message type: " + message.getType());
		}
	}


	public Flux<WebSocketMessage> receive() {
		return getDelegate().getInbound()
				.aggregateFrames(DEFAULT_FRAME_MAX_SIZE)
				.receiveFrames()
				.map(this::toMessage);
	}

	public Mono<Void> send(Publisher<WebSocketMessage> messages) {
		Flux<WebSocketFrame> frames = Flux.from(messages).map(this::toFrame);
		return getDelegate().getOutbound()
				.options(NettyPipeline.SendOptions::flushOnEach)
				.sendObject(frames)
				.then();
	}


	/**
	 * Simple container for {@link reactor.ipc.netty.NettyInbound} and {@link reactor.ipc.netty.NettyOutbound}.
	 */
	public static class WebSocketConnection {

		private final WebsocketInbound inbound;

		private final WebsocketOutbound outbound;


		public WebSocketConnection(WebsocketInbound inbound, WebsocketOutbound outbound) {
			this.inbound = inbound;
			this.outbound = outbound;
		}

		public WebsocketInbound getInbound() {
			return this.inbound;
		}

		public WebsocketOutbound getOutbound() {
			return this.outbound;
		}
	}

}
