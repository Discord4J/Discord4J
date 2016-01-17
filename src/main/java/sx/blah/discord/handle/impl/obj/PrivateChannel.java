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

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrivateChannel extends Channel implements IPrivateChannel {
	
	/**
	 * The recipient of this private channel.
	 */
	protected final IUser recipient;
	
	public PrivateChannel(IDiscordClient client, IUser recipient, String id) {
		this(client, recipient, id, new ArrayList<>());
	}
	
	public PrivateChannel(IDiscordClient client, IUser recipient, String id, List<IMessage> messages) {
		super(client, recipient.getName(), id, null, null, 0, messages, new HashMap<>(), new HashMap<>());
		this.recipient = recipient;
		this.isPrivate = true;
	}
	
	@Override
	public IUser getRecipient() {
		return recipient;
	}
}
