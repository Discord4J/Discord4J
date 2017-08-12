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

package sx.blah.discord.api.internal.json.responses;

import sx.blah.discord.api.internal.json.objects.ChannelObject;
import sx.blah.discord.api.internal.json.objects.UnavailableGuildObject;
import sx.blah.discord.api.internal.json.objects.UserObject;

/**
 * Received on the gateway when the handshake has been completed.
 */
public class ReadyResponse {
	/**
	 * The gateway protocol version.
	 */
	public String v;
	/**
	 * The self user object.
	 */
	public UserObject user;
	/**
	 * The shard information.
	 */
	public int[] shard;
	/**
	 * The ID of the gateway session. Used for resuming.
	 */
	public String session_id;
	/**
	 * Array of DM channels the bot has.
	 */
	public ChannelObject[] private_channels;
	/**
	 * Array of unavailable guilds.
	 */
	public UnavailableGuildObject[] guilds;
	/**
	 * Array of servers connected to. Used for debugging.
	 */
	public String[] _trace;
}
