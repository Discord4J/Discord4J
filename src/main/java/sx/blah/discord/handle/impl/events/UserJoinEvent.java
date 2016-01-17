/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;

/**
 * This is dispatched when a user is added/joins a guild.
 */
public class UserJoinEvent extends Event {
	
	private final IGuild guild;
	private final LocalDateTime joinTime;
	private final IUser userJoined;
	
	public UserJoinEvent(IGuild guild, IUser user, LocalDateTime when) {
		this.guild = guild;
		this.joinTime = when;
		this.userJoined = user;
	}
	
	/**
	 * Gets the timestamp for when the user joined the guild.
	 *
	 * @return The timestamp.
	 */
	public LocalDateTime getJoinTime() {
		return joinTime;
	}
	
	/**
	 * Gets the user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return userJoined;
	}
	
	/**
	 * Gets the guild involved.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
