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

package sx.blah.discord.api.internal.json.requests;

/**
 * Sent to request offline members on the gateway for a guild.
 */
public class GuildMembersRequest {
	/**
	 * The ID of the guild.
	 */
	public String guild_id;
	/**
	 * String the username starts with or empty for all users.
	 */
	public String query = "";
	/**
	 * The limit on users to receive or 0 for max.
	 */
	public int limit = 0;

	public GuildMembersRequest(String guild_id) {
		this.guild_id = guild_id;
	}
}
