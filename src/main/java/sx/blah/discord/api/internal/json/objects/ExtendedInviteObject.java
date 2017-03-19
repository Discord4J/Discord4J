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
 * The extended invite response received from creating an invite.
 */
public class ExtendedInviteObject extends InviteObject {

	/**
	 * The time (in seconds) the invite lasts for
	 */
	public long max_age;

	/**
	 * Whether the invite has been revoked
	 */
	public boolean revoked;

	/**
	 * The time and date the invite was created
	 */
	public String created_at;

	/**
	 * Whether the invite only temporarily accepts a user
	 */
	public boolean temporary;

	/**
	 * The current number of uses of the invite
	 */
	public int uses;

	/**
	 * The maximum amount of times this invite can accept a user
	 */
	public int max_uses;

	/**
	 * The user who created this invite
	 */
	public UserObject inviter;
}
