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
 * Represents OP codes sent on the voice gateway. The {@link #ordinal()} of these objects is the op code sent to Discord.
 */
public enum VoiceOps {

	/**
	 * Used to begin a voice websocket connection
	 */
	IDENTIFY,
	/**
	 * Used to select the voice protocol
	 */
	SELECT_PROTOCOL,
	/**
	 * Used to complete the websocket handshake
	 */
	READY,
	/**
	 * Used to keep the websocket connection alive
	 */
	HEARTBEAT,
	/**
	 * Used to describe the session
	 */
	SESSION_DESCRIPTION,
	/**
	 * Used to indicate which users are speaking
	 */
	SPEAKING,
	/**
	 * Unknown opcode.
	 */
	UNKNOWN;

	/**
	 * Gets a VoiceOps from an opcode or {@link #UNKNOWN} if an unknown code is passed.
	 *
	 * @param opcode The integer opcode.
	 * @return The appropriate VoiceOps or {@link #UNKNOWN}.
	 */
	public static VoiceOps get(int opcode) {
		if (opcode >= values().length) {
			return UNKNOWN;
		} else {
			return values()[opcode];
		}
	}
}
