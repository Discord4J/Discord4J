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

package sx.blah.discord.handle.obj;

import sx.blah.discord.api.IDiscordClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a private channel where you could direct message a user.
 */
public class PrivateChannel extends Channel {
	
	/**
     * The recipient of this private channel.
     */
    protected final User recipient;
    
    public PrivateChannel(IDiscordClient client, User recipient, String id) {
        this(client, recipient, id, new ArrayList<>());
    }

    public PrivateChannel(IDiscordClient client, User recipient, String id, List<Message> messages) {
        super(client, recipient.getName(), id, null, null, messages);
        this.recipient = recipient;
        this.isPrivate = true;
    }

    /**
     * Indicates the user with whom you are communicating.
     * 
     * @return The user.
     */
    public User getRecipient() {
        return recipient;
    }
}
