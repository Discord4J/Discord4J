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
 * Represents a json invite with metadata.
 */
public class ExtendedInviteObject extends InviteObject {
	/**
	 * Number of times this invite has been used.
	 */
	public int uses;
	/**
	 * Max number of times this invite can be used.
	 */
	public int max_uses;
	/**
	 * Duration (in seconds) after which the invite expires.
	 */
	public int max_age;
	/**
	 * Whether this invite only grants temporary membership.
	 */
	public boolean temporary;
	/**
	 * When this invite was created.
	 */
	public String created_at;
	/**
	 * Whether this invite is revoked.
	 */
	public boolean revoked;
}
