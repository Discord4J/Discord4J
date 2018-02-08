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
package discord4j.core.object.data;

import javax.annotation.Nullable;

public class WebhookData {

	private final long id;
	private final long guildId;
	private final long channelId;
	private final long user;
	@Nullable
	private final String name;
	@Nullable
	private final String avatar;
	private final String token;

	public WebhookData(long id, long guildId, long channelId, long user, @Nullable String name,
			@Nullable String avatar, String token) {
		this.id = id;
		this.guildId = guildId;
		this.channelId = channelId;
		this.user = user;
		this.name = name;
		this.avatar = avatar;
		this.token = token;
	}

	public long getId() {
		return id;
	}

	public long getGuildId() {
		return guildId;
	}

	public long getChannelId() {
		return channelId;
	}

	public long getUser() {
		return user;
	}

	@Nullable
	public String getName() {
		return name;
	}

	@Nullable
	public String getAvatar() {
		return avatar;
	}

	public String getToken() {
		return token;
	}
}
