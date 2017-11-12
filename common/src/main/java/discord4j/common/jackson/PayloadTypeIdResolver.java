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

package discord4j.common.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import discord4j.common.json.payload.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PayloadTypeIdResolver extends TypeIdResolverBase {

	private Map<Integer, Class<? extends Payload>> opCodes = new HashMap<>();

	@Override
	public void init(JavaType baseType) {
		opCodes.put(Opcodes.DISPATCH, Dispatch.class);
		opCodes.put(Opcodes.HEARTBEAT, Heartbeat.class);
		opCodes.put(Opcodes.IDENTIFY, Identify.class);
		opCodes.put(Opcodes.STATUS_UPDATE, StatusUpdate.class);
		opCodes.put(Opcodes.VOICE_STATE_UPDATE, VoiceStateUpdate.class);
		opCodes.put(Opcodes.VOICE_SERVER_PING, Dispatch.class); // TODO
		opCodes.put(Opcodes.RESUME, Dispatch.class); // TODO
		opCodes.put(Opcodes.RECONNECT, Dispatch.class); // TODO
		opCodes.put(Opcodes.REQUEST_GUILD_MEMBERS, Dispatch.class); // TODO
		opCodes.put(Opcodes.INVALID_SESSION, InvalidSession.class);
		opCodes.put(Opcodes.HELLO, Hello.class);
		opCodes.put(Opcodes.HEARTBEAT_ACK, Dispatch.class); // TODO
	}

	@Override
	public String idFromValue(Object value) {
		return idFromValueAndType(value, value.getClass());
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> subType) {
		return ((GatewayPayload) value).getOp() + "";
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String id) throws IOException {
		return context.constructType(opCodes.get(Integer.parseInt(id)));
	}

	@Override
	public JsonTypeInfo.Id getMechanism() {
		return JsonTypeInfo.Id.CUSTOM;
	}
}
