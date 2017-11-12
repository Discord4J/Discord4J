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

package discord4j.common.json.payload;

public abstract class Opcodes {

	public static final int DISPATCH = 0;
	public static final int HEARTBEAT = 1;
	public static final int IDENTIFY = 2;
	public static final int STATUS_UPDATE = 3;
	public static final int VOICE_STATE_UPDATE = 4;
	public static final int VOICE_SERVER_PING = 5;
	public static final int RESUME = 6;
	public static final int RECONNECT = 7;
	public static final int REQUEST_GUILD_MEMBERS = 8;
	public static final int INVALID_SESSION = 9;
	public static final int HELLO = 10;
	public static final int HEARTBEAT_ACK = 11;
}
