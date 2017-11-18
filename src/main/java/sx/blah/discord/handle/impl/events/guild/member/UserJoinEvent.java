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

package sx.blah.discord.handle.impl.events.guild.member;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.time.Instant;

/**
 * Dispatched when a member joins a guild.
 */
public class UserJoinEvent extends GuildMemberEvent {

	private final Instant joinTime;

	public UserJoinEvent(IGuild guild, IUser user, Instant when) {
		super(guild, user);
		this.joinTime = when;
	}

	/**
	 * Gets the timestamp of when the user joined the guild.
	 *
	 * @return The timestamp of when the user joined the guild.
	 */
	public Instant getJoinTime() {
		return joinTime;
	}
}
