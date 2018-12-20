package discord4j.voice.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;

public class VoiceGatewayPayloadDeserializer extends StdDeserializer<VoiceGatewayPayload<?>> {

    public VoiceGatewayPayloadDeserializer() {
        super(VoiceGatewayPayload.class);
    }

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
                return null;
        }
    }
}
