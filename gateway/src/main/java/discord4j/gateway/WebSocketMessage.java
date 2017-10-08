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

import discord4j.gateway.buffer.NettyDataBuffer;
import discord4j.gateway.adapter.WebSocketSession;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Representation of a WebSocket message. <p>See static factory methods in {@link WebSocketSession} for creating
 * messages with the org.springframework.core.io.buffer.DataBufferFactory DataBufferFactory for the session.
 *
 * @author Rossen Stoyanchev
 */
public class WebSocketMessage {

	private final Type type;

	private final NettyDataBuffer payload;


	/**
	 * Constructor for a WebSocketMessage. <p>See static factory methods in {@link WebSocketSession} or alternatively
	 * use {@link WebSocketSession#bufferFactory()} to create the payload and then invoke this constructor.
	 */
	public WebSocketMessage(Type type, NettyDataBuffer payload) {
		Objects.requireNonNull(type, "'type' must not be null");
		Objects.requireNonNull(payload,"'payload' must not be null");
		this.type = type;
		this.payload = payload;
	}


	/**
	 * Return the message type (text, binary, etc).
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Return the message payload.
	 */
	public NettyDataBuffer getPayload() {
		return this.payload;
	}

	/**
	 * Return the message payload as UTF-8 text. This is a useful for text WebSocket messages.
	 */
	public String getPayloadAsText() {
		byte[] bytes = new byte[this.payload.readableByteCount()];
		this.payload.read(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * Retain the data buffer for the message payload, which is useful on runtimes (e.g. Netty) with pooled buffers. A
	 * shortcut for:
	 * <pre>
	 * DataBuffer payload = message.getPayload();
	 * DataBufferUtils.retain(payload);
	 * </pre>
	 */
	public WebSocketMessage retain() {
		retain(this.payload);
		return this;
	}

	/**
	 * Release the payload {@code DataBuffer} which is useful on runtimes (e.g. Netty) with pooled buffers such as
	 * Netty. A shortcut for:
	 * <pre>
	 * DataBuffer payload = message.getPayload();
	 * DataBufferUtils.release(payload);
	 * </pre>
	 */
	public void release() {
		release(this.payload);
	}

	@SuppressWarnings("unchecked")
	private static NettyDataBuffer retain(NettyDataBuffer dataBuffer) {
		return dataBuffer.retain();
	}

	private static boolean release(@Nullable NettyDataBuffer dataBuffer) {
		return dataBuffer != null && dataBuffer.release();
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof WebSocketMessage)) {
			return false;
		}
		WebSocketMessage otherMessage = (WebSocketMessage) other;
		return (this.type.equals(otherMessage.type) && nullSafeEquals(this.payload, otherMessage.payload));
	}

	private static boolean nullSafeEquals(@Nullable Object o1, @Nullable Object o2) {
		return o1 == o2 || o1 != null && o2 != null && o1.equals(o2);
	}

	@Override
	public int hashCode() {
		return this.type.hashCode() * 29 + this.payload.hashCode();
	}


	/**
	 * WebSocket message types.
	 */
	public enum Type {
		TEXT, BINARY, PING, PONG
	}

}
