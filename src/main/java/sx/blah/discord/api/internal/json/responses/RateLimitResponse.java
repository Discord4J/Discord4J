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

/**
 * Received when an action has been rate limited.
 */
public class RateLimitResponse {

	/**
	 * Whether the bot has been globally rate limited.
	 */
	public boolean global;

	/**
	 * The amount of time (in milliseconds) before this client can send another request with the same bucket.
	 */
	public long retry_after;

	/**
	 * The message returned by Discord.
	 */
	public String message;
}
