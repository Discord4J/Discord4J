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

public class MemberData {

	private final long user;
	@Nullable
	private final String nick;
	private final long[] roles;
	private final String joinedAt;
	private final boolean deaf;
	private final boolean mute;

	public MemberData(long user, @Nullable String nick, long[] roles, String joinedAt, boolean deaf, boolean mute) {
		this.user = user;
		this.nick = nick;
		this.roles = roles;
		this.joinedAt = joinedAt;
		this.deaf = deaf;
		this.mute = mute;
	}

	public long getUser() {
		return user;
	}

	@Nullable
	public String getNick() {
		return nick;
	}

	public long[] getRoles() {
		return roles;
	}

	public String getJoinedAt() {
		return joinedAt;
	}

	public boolean isDeaf() {
		return deaf;
	}

	public boolean isMute() {
		return mute;
	}
}
