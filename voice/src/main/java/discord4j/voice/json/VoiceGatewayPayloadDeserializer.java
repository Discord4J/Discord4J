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
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.jspecify.annotations.Nullable;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VoiceGatewayPayloadDeserializer extends StdDeserializer<VoiceGatewayPayload<?>> {

    private static final Logger LOG = Loggers.getLogger(VoiceGatewayPayloadDeserializer.class);

    public VoiceGatewayPayloadDeserializer() {
        super(VoiceGatewayPayload.class);
    }

    @Nullable
    @Override
    public VoiceGatewayPayload<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode json = codec.readTree(p);
        return deserialize(json, codec);
    }

    public static VoiceGatewayPayload<?> deserialize(JsonNode json, ObjectCodec codec) throws IOException {
        int op = json.get("op").asInt();
        JsonNode d = json.get("d");
        switch (op) {
            case Hello.OP:
                return new Hello(d.get("heartbeat_interval").asLong());
            case Ready.OP:
                List<String> modes = new ArrayList<>();
                if (d.has("modes")) {
                    for (JsonNode mode : d.get("modes")) {
                        modes.add(mode.asText());
                    }
                }
                return new Ready(d.get("ssrc").asInt(), d.get("ip").asText(), d.get("port").asInt(), modes,
                        d.has("auth_session_id") && !d.get("auth_session_id").isNull()
                                ? d.get("auth_session_id").asText()
                                : null);
            case HeartbeatAck.OP:
                return new HeartbeatAck(d.asLong());
            case SessionDescription.OP:
                ArrayNode arrayNode = ((ArrayNode) d.get("secret_key"));
                byte[] secret_key = codec.readValue(arrayNode.traverse(codec), byte[].class);
                return new SessionDescription(d.get("mode").asText(), secret_key,
                        d.has("dave_protocol_version") ? d.get("dave_protocol_version").asInt() : 0);
            case Speaking.OP:
                return new Speaking(d.get("user_id").asText(), d.get("ssrc").asInt(), d.get("speaking").asBoolean());
            case ClientsConnect.OP:
                List<String> userIds = new ArrayList<>();
                if (d.has("user_ids")) {
                    for (JsonNode userId : d.get("user_ids")) {
                        userIds.add(userId.asText());
                    }
                }
                return new ClientsConnect(userIds);
            case VoiceDisconnect.OP:
                return new VoiceDisconnect(d.get("user_id").asText());
            case DaveProtocolPrepareTransition.OP:
                return new DaveProtocolPrepareTransition(d.get("transition_id").asInt(),
                        d.get("protocol_version").asInt());
            case DaveProtocolExecuteTransition.OP:
                return new DaveProtocolExecuteTransition(d.get("transition_id").asInt());
            case DaveProtocolPrepareEpoch.OP:
                return new DaveProtocolPrepareEpoch(d.get("epoch").asLong(),
                        d.get("protocol_version").asInt());
            case MlsInvalidCommitWelcome.OP:
                return new MlsInvalidCommitWelcome(d.get("transition_id").asInt());
            case Resumed.OP:
                return new Resumed(d.asText()); // actually "d": null
            default:
                LOG.debug("Received voice gateway payload with unhandled OP: {}", op);
                return null;
        }
    }
}
