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

public class GuildEmojiData {

	private final long id;
	private final String name;
	private final long[] roles;
	private final long user;
	private final boolean requiresColons;
	private final boolean managed;
	private final boolean animated;

	public GuildEmojiData(long id, String name, long[] roles, long user, boolean requiresColons, boolean managed,
			boolean animated) {
		this.id = id;
		this.name = name;
		this.roles = roles;
		this.user = user;
		this.requiresColons = requiresColons;
		this.managed = managed;
		this.animated = animated;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long[] getRoles() {
		return roles;
	}

	public long getUser() {
		return user;
	}

	public boolean requiresColons() {
		return requiresColons;
	}

	public boolean isManaged() {
		return managed;
	}

	public boolean isAnimated() {
		return animated;
	}
}
