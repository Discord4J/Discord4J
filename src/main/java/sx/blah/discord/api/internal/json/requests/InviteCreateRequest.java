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
 * Sent to create an invite for a channel.
 */
public class InviteCreateRequest {

	/**
	 * The time until the invite expires (in seconds). Set to 0 for an infinite invite.
	 */
	public int max_age;

	/**
	 * The maximum number of times this invite can be used. Set to 0 for no limit.
	 */
	public int max_uses;

	/**
	 * Whether the users added through this invite are temporary.
	 */
	public boolean temporary;

	/**
	 * Whether to reuse similar invites.
	 */
	public boolean unique;

	public InviteCreateRequest(int max_age, int max_uses, boolean temporary, boolean unique) {
		this.max_age = max_age;
		this.max_uses = max_uses;
		this.temporary = temporary;
		this.unique = unique;
	}
}
