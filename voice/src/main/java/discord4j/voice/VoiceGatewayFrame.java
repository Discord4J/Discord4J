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

package discord4j.voice;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

final class VoiceGatewayFrame {

    private final boolean binary;
    private final ByteBuf content;

    private VoiceGatewayFrame(boolean binary, ByteBuf content) {
        this.binary = binary;
        this.content = content;
    }

    static VoiceGatewayFrame text(ByteBuf content) {
        return new VoiceGatewayFrame(false, content);
    }

    static VoiceGatewayFrame binary(ByteBuf content) {
        return new VoiceGatewayFrame(true, content);
    }

    static VoiceGatewayFrame inbound(WebSocketFrame frame) {
        return frame instanceof BinaryWebSocketFrame ? binary(frame.content()) : text(frame.content());
    }

    ByteBuf getContent() {
        return content;
    }

    boolean isBinary() {
        return binary;
    }

    WebSocketFrame toWebSocketFrame() {
        return binary ? new BinaryWebSocketFrame(content) : new TextWebSocketFrame(content);
    }
}
