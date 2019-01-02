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
package discord4j.voice.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import reactor.util.Logger;
import reactor.util.Loggers;

import javax.annotation.Nullable;
import java.io.IOException;

public class VoiceGatewayPayloadDeserializer extends StdDeserializer<VoiceGatewayPayload<?>> {

    private static final Logger LOG = Loggers.getLogger(VoiceGatewayPayloadDeserializer.class);

    public VoiceGatewayPayloadDeserializer() {
        super(VoiceGatewayPayload.class);
    }

    @Nullable
    @Override
    public VoiceGatewayPayload<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode json = p.getCodec().readTree(p);
        int op = json.get("op").asInt();
        JsonNode d =json.get("d");

        switch (op) {
            case Hello.OP:
                return new Hello(d.get("heartbeat_interval").asLong());
            case Ready.OP:
                return new Ready(d.get("ssrc").asInt(), d.get("ip").asText(), d.get("port").asInt());
            case HeartbeatAck.OP:
                return new HeartbeatAck(d.asLong());
            case SessionDescription.OP:
                ArrayNode arrayNode = ((ArrayNode) d.get("secret_key"));
                byte[] secret_key = p.getCodec().readValue(arrayNode.traverse(p.getCodec()), byte[].class);
                return new SessionDescription(d.get("mode").asText(), secret_key);
            case Speaking.OP:
                return new Speaking(d.get("user_id").asText(), d.get("ssrc").asInt(), d.get("speaking").asBoolean());
            case VoiceDisconnect.OP:
                return new VoiceDisconnect(d.get("user_id").asText());
            default:
                LOG.trace("Received voice gateway payload with unhandled OP: {}", op);
                return null;
        }
    }
}
