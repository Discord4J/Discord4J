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

package sx.blah.discord.api.internal;

/**
 * Represents OP codes sent on the main gateway. The {@link #ordinal()} of these objects is the op code sent to Discord.
 */
public enum GatewayOps {
	/**
	 * Dispatches an event.
	 */
	DISPATCH,
	/**
	 * Used for ping checking.
	 */
	HEARTBEAT,
	/**
	 * Used for client handshake.
	 */
	IDENTIFY,
	/**
	 * Used to update the client status.
	 */
	STATUS_UPDATE,
	/**
	 * Used to join/move/leave voice channels.
	 */
	VOICE_STATE_UPDATE,
	/**
	 * Used for voice ping checking.
	 */
	VOICE_SERVER_PING,
	/**
	 * Used to resume a closed connection.
	 */
	RESUME,
	/**
	 * Used to redirect clients to a new gateway.
	 */
	RECONNECT,
	/**
	 * Used to request guild members.
	 */
	REQUEST_GUILD_MEMBERS,
	/**
	 * Used to notify client they have an invalid session id.
	 */
	INVALID_SESSION,
	/**
	 * Received immediately after connecting.
	 */
	HELLO,
	/**
	 * Sent after a heartbeat was received.
	 */
	HEARTBEAT_ACK,
	/**
	 * Unknown opcode.
	 */
	UNKNOWN;

	/**
	 * Gets a GatewayOps from an opcode or {@link #UNKNOWN} if an unknown code is passed.
	 *
	 * @param opcode The integer opcode.
	 * @return The appropriate GatewayOps or {@link #UNKNOWN}.
	 */
	public static GatewayOps get(int opcode) {
		if (opcode >= values().length) {
			return UNKNOWN;
		} else {
			return values()[opcode];
		}
	}
}
