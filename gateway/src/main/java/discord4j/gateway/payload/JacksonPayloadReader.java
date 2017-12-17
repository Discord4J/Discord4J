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
package discord4j.gateway.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.json.payload.GatewayPayload;
import io.netty.buffer.ByteBuf;
import reactor.core.Exceptions;

import java.io.IOException;

public class JacksonPayloadReader implements PayloadReader {

	private final ObjectMapper mapper;

	public JacksonPayloadReader(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public GatewayPayload<?> read(ByteBuf payload) {
		try {
			return mapper.readValue(payload.array(), GatewayPayload.class);
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}
}
