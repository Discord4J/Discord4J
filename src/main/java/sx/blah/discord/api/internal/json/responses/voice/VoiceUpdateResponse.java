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

package sx.blah.discord.api.internal.json.responses.voice;

/**
 * Received when initially connecting to voice and when the current voice instance fails over to a new server.
 */
public class VoiceUpdateResponse {
	/**
	 * The voice connection token.
	 */
	public String token;
	/**
	 * The ID of the guild this server update is for.
	 */
	public String guild_id;
	/**
	 * The voice server host.
	 */
	public String endpoint;
}
