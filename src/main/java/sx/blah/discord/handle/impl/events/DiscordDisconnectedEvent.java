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

package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.GatewayOps;

/**
 * Dispatched when a shard disconnects from Discord.
 * @deprecated Use {@link DisconnectedEvent}
 */
public class DiscordDisconnectedEvent extends DisconnectedEvent {

	public DiscordDisconnectedEvent(Reason reason, IShard shard) {
		super(DisconnectedEvent.Reason.valueOf(reason.name()), shard);
	}

	/**
	 * The possible reasons for the shard disconnecting.
	 * @deprecated Use {@link DisconnectedEvent.Reason}
	 */
	public enum Reason {
		/**
		 * The gateway received {@link GatewayOps#INVALID_SESSION}. The shard will attempt to begin a new session.
		 */
		INVALID_SESSION_OP,
		/**
		 * The gateway received {@link GatewayOps#RECONNECT}. The shard will attempt to resume.
		 */
		RECONNECT_OP,
		/**
		 * A direct call to {@link IShard#logout()} was made.
		 */
		LOGGED_OUT,
		/**
		 * Something unknown caused the websocket to close. The shard will attempt to resume.
		 */
		ABNORMAL_CLOSE
	}
}
