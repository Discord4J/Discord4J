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
