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

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.impl.events.MessageSendEvent;
import sx.blah.discord.json.requests.MessageRequest;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Requests;

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
    protected final Guild parent;
	
	/**
     * The client that created this object.
     */
    protected final IDiscordClient client;

    public Channel(IDiscordClient client, String name, String id, Guild parent) {
        this(client, name, id, parent, new ArrayList<>());
    }

    public Channel(IDiscordClient client, String name, String id, Guild parent, List<Message> messages) {
        this.client = client;       
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

    public Message sendMessage(String content) {
        if (client.isReady()) {
           content = DiscordUtils.escapeString(content);
        
            try {
                MessageResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.CHANNELS + id + "/messages",
                        new StringEntity(DiscordUtils.GSON.toJson(new MessageRequest(content, new String[0])), "UTF-8"),
                        new BasicNameValuePair("authorization", client.getToken()),
                        new BasicNameValuePair("content-type", "application/json")), MessageResponse.class);
            
                String time = response.timestamp;
                String messageID = response.id;
            
                Message message = new Message(client, messageID, content, client.getOurUser(), this, DiscordUtils.convertFromTimestamp(time));
                addMessage(message); //Had to be moved here so that if a message is edited before the MESSAGE_CREATE event, it doesn't error
                client.getDispatcher().dispatch(new MessageSendEvent(message));
                return message;
            } catch (HTTP403Exception e) {
                Discord4J.LOGGER.error("Received 403 error attempting to send message; is your login correct?");
                return null;
            }
        
        } else {
            Discord4J.LOGGER.error("Bot has not signed in yet!");
            return null;
        }
    }
    
    @Override 
    public String toString() {
        return mention();
    }
}
