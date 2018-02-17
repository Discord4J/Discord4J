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
package discord4j.gateway.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A text or binary message received on a {@link WebSocketSession}.
 */
public class WebSocketMessage {

	private final Type type;
	private final ByteBuf payload;

	public WebSocketMessage(Type type, ByteBuf payload) {
		this.type = Objects.requireNonNull(type, "'type' must not be null");
		this.payload = Objects.requireNonNull(payload, "'payload' must not be null");
	}

	/**
	 * Create a new WebSocket message from a string.
	 *
	 * @param payload the payload contents
	 * @return a {@code WebSocketMessage} with text contents
	 */
	public static WebSocketMessage fromText(String payload) {
		byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
		ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
		return new WebSocketMessage(WebSocketMessage.Type.TEXT, buffer);
	}

	/**
	 * Create a new WebSocket message from a byte buffer.
	 *
	 * @param payload the payload contents
	 * @return a {@code WebSocketMessage} with binary contents
	 */
	public static WebSocketMessage fromBinary(ByteBuf payload) {
		return new WebSocketMessage(Type.BINARY, payload);
	}

	/**
	 * Create a new WebSocket message from a WebSocket frame.
	 *
	 * @param frame the original frame
	 * @return the message built from the given frame
	 */
	public static WebSocketMessage fromFrame(WebSocketFrame frame) {
		ByteBuf payload = frame.content();
		return new WebSocketMessage(Type.fromFrameClass(frame.getClass()), payload);
	}

	/**
	 * Create a new WebSocket frame from a WebSocket message.
	 *
	 * @param message the original message
	 * @return the frame built from the given message
	 */
	public static WebSocketFrame toFrame(WebSocketMessage message) {
		ByteBuf byteBuf = message.getPayload();

		switch (message.getType()) {
			case TEXT:
				return new TextWebSocketFrame(byteBuf);
			case BINARY:
				return new BinaryWebSocketFrame(byteBuf);
			default:
				throw new IllegalArgumentException("Unknown websocket message type: " + message.getType());
		}
	}

	/**
	 * Return the message type (text, binary, etc).
	 *
	 * @return the message type
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Return the message payload.
	 *
	 * @return the payload represented as a byte buffer
	 */
	public ByteBuf getPayload() {
		return this.payload;
	}

	/**
	 * Return the message payload as UTF-8 text. This is a useful for text WebSocket messages.
	 *
	 * @return the payload represented as a String
	 */
	public String getPayloadAsText() {
		byte[] bytes = new byte[this.payload.readableBytes()];
		this.payload.readBytes(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, payload);
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
		return (this.type.equals(otherMessage.type) && Objects.equals(this.payload, otherMessage.payload));
	}

	/**
	 * WebSocket message types.
	 */
	public enum Type {
		TEXT, BINARY;

		public static Type fromFrameClass(Class<?> clazz) {
			if (clazz.equals(TextWebSocketFrame.class)) {
				return TEXT;
			} else if (clazz.equals(BinaryWebSocketFrame.class)) {
				return BINARY;
			}

			throw new IllegalArgumentException("Unknown frame class: " + clazz);
		}
	}

}
