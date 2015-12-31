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
import sx.blah.discord.handle.obj.Invite;
import sx.blah.discord.handle.obj.Message;

/**
 * This event is dispatched when a message the bot receives includes an invite link.
 */
public class InviteReceivedEvent extends Event {
	
	private final Invite invite;
	private final Message message;

	public InviteReceivedEvent(Invite invite, Message message) {
		this.invite = invite;
		this.message = message;
	}
	
	/**
	 * Gets the invite received.
	 * 
	 * @return The invite.
	 */
	public Invite getInvite() {
		return invite;
	}
	
	/**
	 * Gets the message which contains the invite.
	 * 
	 * @return The message.
	 */
	public Message getMessage() {
		return message;
	}
}
