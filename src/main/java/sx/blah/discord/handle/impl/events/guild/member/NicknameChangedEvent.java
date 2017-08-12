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

import java.util.Optional;

/**
 * Dispatched when the nickname of a member is changed.
 */
public class NicknameChangedEvent extends GuildMemberEvent {

	private final String oldNickname, newNickname;

	public NicknameChangedEvent(IGuild guild, IUser user, String oldNickname, String newNickname) {
		super(guild, user);
		this.oldNickname = oldNickname;
		this.newNickname = newNickname;
	}

	/**
	 * Gets the old nickname.
	 *
	 * @return The old nickname.
	 */
	public Optional<String> getOldNickname() {
		return Optional.ofNullable(oldNickname);
	}

	/**
	 * Gets the new nickname.
	 *
	 * @return The new nickname.
	 */
	public Optional<String> getNewNickname() {
		return Optional.ofNullable(newNickname);
	}
}
