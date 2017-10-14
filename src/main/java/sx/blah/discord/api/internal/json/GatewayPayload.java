/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api.internal.json;

import sx.blah.discord.api.internal.GatewayOps;
import sx.blah.discord.api.internal.VoiceOps;

/**
 * Generic payload sent on the main or voice gateway.
 */
public class GatewayPayload {
	/**
	 * The event name. (Only used for OP 0)
	 */
	public String t;
	/**
	 * The sequence number.
	 */
	public Integer s;
	/**
	 * The opcode for the payload.
	 */
	public Integer op;
	/**
	 * The data of the payload.
	 */
	public Object d;

	public GatewayPayload() {}

	public GatewayPayload(GatewayOps op, Object request) {
		this(null, null, op.ordinal(), request);
	}

	public GatewayPayload(VoiceOps op, Object request) {
		this(null, null, op.ordinal(), request);
	}

	private GatewayPayload(String t, Integer s, Integer op, Object d) {
		this.t = t;
		this.s = s;
		this.op = op;
		this.d = d;
	}
}
