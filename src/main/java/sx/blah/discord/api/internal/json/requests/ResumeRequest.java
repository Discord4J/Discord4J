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
 * Used to replay missed events when the bot successfully resumes on the gateway.
 */
public class ResumeRequest {

	/**
	 * The bot's authentication token.
	 */
	public String token;

	/**
	 * The session ID to resume.
	 * @see sx.blah.discord.api.internal.DiscordWS#sessionId
	 */
	public String session_id;

	/**
	 * The last cached sequence number.
	 * @see sx.blah.discord.api.internal.DiscordWS#seq
	 */
	public long seq;

	public ResumeRequest(String token, String session_id, long seq) {
		this.token = token;
		this.session_id = session_id;
		this.seq = seq;
	}
}
