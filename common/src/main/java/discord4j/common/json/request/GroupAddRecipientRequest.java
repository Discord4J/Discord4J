/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.common.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupAddRecipientRequest {

	@JsonProperty("access_token")
	private final String accessToken;
	private final String nick;

	public GroupAddRecipientRequest(String accessToken, String nick) {
		this.accessToken = accessToken;
		this.nick = nick;
	}

	@Override
	public String toString() {
		return "GroupAddRecipientRequest[" +
				"accessToken='" + accessToken + '\'' +
				", nick='" + nick + '\'' +
				']';
	}
}
