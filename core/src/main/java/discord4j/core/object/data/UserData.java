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

public class UserData {

	private final long id;
	private final String username;
	private final String discriminator;
	@Nullable
	private final String avatar;
	@Nullable
	private final Boolean bot;

	public UserData(long id, String username, String discriminator, @Nullable String avatar, @Nullable Boolean bot) {
		this.id = id;
		this.username = username;
		this.discriminator = discriminator;
		this.avatar = avatar;
		this.bot = bot;
	}

	public long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getDiscriminator() {
		return discriminator;
	}

	@Nullable
	public String getAvatar() {
		return avatar;
	}

	@Nullable
	public Boolean getBot() {
		return bot;
	}
}
