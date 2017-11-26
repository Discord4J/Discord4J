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

import discord4j.common.json.payload.GatewayPayload;
import io.netty.buffer.ByteBuf;

/**
 * Strategy for encoding a {@link discord4j.common.json.payload.GatewayPayload} and writing its contents to a {@link
 * io.netty.buffer.ByteBuf}.
 */
public interface PayloadWriter {

	/**
	 * Write a given payload to the output buffer.
	 *
	 * @param payload the output payload
	 * @return the outbound {@code ByteBuf}
	 */
	ByteBuf write(GatewayPayload payload);
}
