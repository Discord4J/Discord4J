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

import java.util.ArrayList;
import java.util.List;

/**
 * @author qt
 * @since 4:57 PM 17 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * Defines a text channel in a guild/server.
 */
public class Channel {
    /**
     * User-friendly channel name (e.g. "general")
     */
    protected String name;

    /**
     * Channel ID.
     */
    protected final String id;

    /**
     * Messages that have been sent into this channel
     */
    protected final List<Message> messages;

    /**
     * Indicates whether or not this channel is a PM channel.
     */
    protected boolean isPrivate;

    /**
     * The guild this channel belongs to.
     */
    private final Guild parent;

    public Channel(String name, String id, Guild parent) {
        this(name, id, parent, new ArrayList<>());
    }

    public Channel(String name, String id, Guild parent, List<Message> messages) {
        this.name = name;
        this.id = id;
        this.messages = messages;
        this.parent = parent;
        this.isPrivate = false;
    }

    // Getters.

    public String getName() {
        return name;
    }

    public String getID() {
        return id;
    }

    public List<Message> getMessages() {
        return messages;
    }

	public Message getMessageByID(String messageID) {
		for (Message message : messages) {
			if (message.getID().equalsIgnoreCase(messageID))
				return message;
		}

		return null;
	}

    public void addMessage(Message message) {
        if (message.getChannel().getID().equalsIgnoreCase(this.getID())) {
            messages.add(message);
        }
    }

    public Guild getParent() {
        return parent;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    // still STOLEN from hydrabolt :P
    public String mention() {
        return "<#" + this.getID() + ">";
    }

    @Override public String toString() {
        return mention();
    }
}
