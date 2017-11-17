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
package discord4j.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import discord4j.common.json.payload.*;
import discord4j.common.json.payload.dispatch.Dispatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PayloadDeserializer extends StdDeserializer<GatewayPayload> {

	private static final String OP_FIELD = "op";
	private static final String D_FIELD = "d";
	private static final String T_FIELD = "t";
	private static final String S_FIELD = "s";

	private static final Map<Integer, Class<? extends Payload>> payloadTypes = new HashMap<>();
	private static final Map<String, Class<? extends Dispatch>> dispatchTypes = new HashMap<>();

	static {
		payloadTypes.put(Opcodes.DISPATCH, Dispatch.class);
		payloadTypes.put(Opcodes.HEARTBEAT, Heartbeat.class);
		payloadTypes.put(Opcodes.IDENTIFY, Identify.class);
		payloadTypes.put(Opcodes.STATUS_UPDATE, StatusUpdate.class);
		payloadTypes.put(Opcodes.VOICE_STATE_UPDATE, VoiceStateUpdate.class);
		payloadTypes.put(Opcodes.VOICE_SERVER_PING, null); // Never sent
		payloadTypes.put(Opcodes.RESUME, Resume.class);
		payloadTypes.put(Opcodes.RECONNECT, null); // Body always null. For completion
		payloadTypes.put(Opcodes.REQUEST_GUILD_MEMBERS, RequestGuildMembers.class);
		payloadTypes.put(Opcodes.INVALID_SESSION, InvalidSession.class);
		payloadTypes.put(Opcodes.HELLO, Hello.class);
		payloadTypes.put(Opcodes.HEARTBEAT_ACK, null); // Body always null. For completion
	}

	public PayloadDeserializer() {
		super(GatewayPayload.class);
	}

	@Override
	public GatewayPayload deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode payload = p.getCodec().readTree(p);

		int op = payload.get(OP_FIELD).asInt();
		String t = payload.get(T_FIELD).asText();
		Integer s = payload.get(S_FIELD).isNull() ? null : payload.get(S_FIELD).intValue();

		Class<? extends Payload> payloadType = getPayloadType(op, t);
		Payload data = payloadType == null ? null : p.getCodec().treeToValue(payload.get(D_FIELD), payloadType);

		return new GatewayPayload(op, data, s, t);
	}

	private static Class<? extends Payload> getPayloadType(int op, String t) {
		if (op == Opcodes.DISPATCH) {
			return dispatchTypes.get(t);
		}
		return payloadTypes.get(op);
	}
}
