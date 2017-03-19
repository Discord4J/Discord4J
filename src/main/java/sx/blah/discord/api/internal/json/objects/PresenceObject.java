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

package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json presence object.
 */
public class PresenceObject {
	/**
	 * The user associated with this presence.
	 */
	public UserObject user;
	/**
	 * The status of the presence.
	 */
	public String status;
	/**
	 * The roles the user has.
	 */
	public RoleObject[] roles;
	/**
	 * The nickname of the user.
	 */
	public String nick;
	/**
	 * The guild id of the presence.
	 */
	public String guild_id;
	/**
	 * The game of the presence.
	 */
	public GameObject game;
}
