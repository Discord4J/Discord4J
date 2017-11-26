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
package discord4j.common.json.payload.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.json.response.ChannelResponse;
import discord4j.common.json.response.UnavailableGuildResponse;
import discord4j.common.json.response.UserResponse;

import java.util.Arrays;

public class Ready implements Dispatch {

	@JsonProperty("v")
	private int version;
	private UserResponse user;
	@JsonProperty("private_channels")
	private ChannelResponse[] privateChannels;
	private UnavailableGuildResponse[] guilds;
	@JsonProperty("session_id")
	private String sessionId;
	@JsonProperty("_trace")
	private String[] trace;

	// FIXME
	private Object user_settings;
	private Object[] relationships;
	private Object[] presences;

	public int getVersion() {
		return version;
	}

	public UserResponse getUser() {
		return user;
	}

	public ChannelResponse[] getPrivateChannels() {
		return privateChannels;
	}

	public UnavailableGuildResponse[] getGuilds() {
		return guilds;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String[] getTrace() {
		return trace;
	}

	@Override
	public String toString() {
		return "Ready[" +
				"version=" + version +
				", user=" + user +
				", privateChannels=" + Arrays.toString(privateChannels) +
				", guilds=" + Arrays.toString(guilds) +
				", sessionId=" + sessionId +
				", trace=" + Arrays.toString(trace) +
				", user_settings=" + user_settings +
				", relationships=" + Arrays.toString(relationships) +
				", presences=" + Arrays.toString(presences) +
				']';
	}
}
