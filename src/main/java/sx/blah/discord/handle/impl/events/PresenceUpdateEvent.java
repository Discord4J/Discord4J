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
import sx.blah.discord.handle.obj.Presences;

/**
 * This event is dispatched when a user changes his/her presence.
 */
public class PresenceUpdateEvent extends Event {
	
	private final IGuild guild;
	private final IUser user;
	private final Presences oldPresence, newPresence;
	
	public PresenceUpdateEvent(IGuild guild, IUser user, Presences oldPresence, Presences newPresence) {
		this.guild = guild;
		this.user = user;
		this.oldPresence = oldPresence;
		this.newPresence = newPresence;
	}
	
	/**
	 * Gets the user's new presence.
	 *
	 * @return The presence.
	 */
	public Presences getNewPresence() {
		return newPresence;
	}
	
	/**
	 * Gets the user's old presence.
	 *
	 * @return The presence.
	 */
	public Presences getOldPresence() {
		return oldPresence;
	}
	
	/**
	 * Gets the user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
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
