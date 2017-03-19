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

package sx.blah.discord.api.internal.json.requests.voice;

public class VoiceIdentifyRequest {
	private String server_id;
	private String user_id;
	private String session_id;
	private String token;

	public VoiceIdentifyRequest(String server_id, String user_id, String session_id, String token) {
		this.server_id = server_id;
		this.user_id = user_id;
		this.session_id = session_id;
		this.token = token;
	}
}
