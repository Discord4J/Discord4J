/**
 * Copyright 2002-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package discord4j.gateway.adapter;

import discord4j.gateway.CloseStatus;
import discord4j.gateway.HandshakeInfo;
import discord4j.gateway.WebSocketMessage;
import discord4j.gateway.buffer.NettyDataBuffer;
import discord4j.gateway.buffer.NettyDataBufferFactory;
import io.netty.buffer.ByteBuf;
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
			HandshakeInfo info, NettyDataBufferFactory bufferFactory) {

		WebSocketConnection delegate = new WebSocketConnection(inbound, outbound);

		Objects.requireNonNull(delegate, "Native session is required.");
		Objects.requireNonNull(info, "HandshakeInfo is required.");
		Objects.requireNonNull(bufferFactory, "DataBuffer factory is required.");

		this.delegate = delegate;
		this.id = Integer.toHexString(System.identityHashCode(delegate));
		this.handshakeInfo = info;
		this.bufferFactory = bufferFactory;
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
	private final NettyDataBufferFactory bufferFactory;


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
		NettyDataBuffer buffer = bufferFactory().wrap(bytes);
		return new WebSocketMessage(WebSocketMessage.Type.TEXT, buffer);
	}

	public WebSocketMessage binaryMessage(Function<NettyDataBufferFactory, NettyDataBuffer> payloadFactory) {
		NettyDataBuffer payload = payloadFactory.apply(bufferFactory());
		return new WebSocketMessage(WebSocketMessage.Type.BINARY, payload);
	}

	public WebSocketMessage pingMessage(Function<NettyDataBufferFactory, NettyDataBuffer> payloadFactory) {
		NettyDataBuffer payload = payloadFactory.apply(bufferFactory());
		return new WebSocketMessage(WebSocketMessage.Type.PING, payload);
	}

	public WebSocketMessage pongMessage(Function<NettyDataBufferFactory, NettyDataBuffer> payloadFactory) {
		NettyDataBuffer payload = payloadFactory.apply(bufferFactory());
		return new WebSocketMessage(WebSocketMessage.Type.PONG, payload);
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id=" + getId() + ", uri=" + getHandshakeInfo().getUri() + "]";
	}


	public NettyDataBufferFactory bufferFactory() {
		return this.bufferFactory;
	}


	protected WebSocketMessage toMessage(WebSocketFrame frame) {
		NettyDataBuffer payload = bufferFactory().wrap(frame.content());
		return new WebSocketMessage(MESSAGE_TYPES.get(frame.getClass()), payload);
	}

	protected WebSocketFrame toFrame(WebSocketMessage message) {
		ByteBuf byteBuf = NettyDataBufferFactory.toByteBuf(message.getPayload());
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

	public Mono<Void> close(CloseStatus status) {
		return Mono.error(new UnsupportedOperationException(
				"Currently in Reactor Netty applications are expected to use the " +
						"Cancellation returned from subscribing to the \"receive\"-side Flux " +
						"in order to close the WebSocket session."));
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
