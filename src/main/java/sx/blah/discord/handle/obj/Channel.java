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

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.impl.events.MessageSendEvent;
import sx.blah.discord.json.requests.InviteRequest;
import sx.blah.discord.json.requests.MessageRequest;
import sx.blah.discord.json.responses.ExtendedInviteResponse;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Requests;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
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
     * The channel's topic message.
     */
    protected String topic;
	
	/**
     * The last read message.
     */
    protected String lastReadMessageID = null;
    
    /**
     * Whether the bot should send out a typing status
     */
    protected AtomicBoolean isTyping = new AtomicBoolean(false);
    
    /**
     * Keeps track of the time to handle repeated typing status broadcasts
     */
    protected AtomicLong typingTimer = new AtomicLong(0);
    
    /**
     * 5 seconds, the time it takes for one typing status to "wear off"
     */
    protected static final long TIME_FOR_TYPE_STATUS = 5000;
    
	/**
     * The client that created this object.
     */
    protected final IDiscordClient client;

    public Channel(IDiscordClient client, String name, String id, Guild parent, String topic) {
        this(client, name, id, parent, topic, new ArrayList<>());
    }

    public Channel(IDiscordClient client, String name, String id, Guild parent, String topic,  List<Message> messages) {
        this.client = client;       
        this.name = name;
        this.id = id;
        this.messages = messages;
        this.parent = parent;
        this.isPrivate = false;
        this.topic = topic;
    }
	
	/**
     * Gets the name of this channel.
     * 
     * @return The channel name.
     */
    public String getName() {
        return name;
    }
	
	/**
     * Gets the id of this channel.
     * 
     * @return The channel id.
     */
    public String getID() {
        return id;
    }
	
	/**
     * Gets the messages in this channel.
     * 
     * @return The list of messages in the channel.
     */
    public List<Message> getMessages() {
        return messages;
    }
	
	/**
     * Gets a specific message by its id.
     * 
     * @param messageID The message id.
     * @return The message (if found).
     */
    public Message getMessageByID(String messageID) {
		for (Message message : messages) {
			if (message.getID().equalsIgnoreCase(messageID))
				return message;
		}

		return null;
	}
	
	/**
     * CACHES a message to the channel.
     * 
     * @param message The message.
     */
    @Deprecated
    public void addMessage(Message message) {
        if (message.getChannel().getID().equalsIgnoreCase(this.getID())) {
            messages.add(message);
            if (lastReadMessageID == null)
                lastReadMessageID = message.getID();
        }
    }
	
	/**
     * Gets the guild this channel is a part of.
     * 
     * @return The guild.
     * @deprecated Use {@link #getGuild()} instead.
     */
    @Deprecated
    public Guild getParent() {
        return parent;
    }
    
    /**
     * Gets the guild this channel is a part of.
     * 
     * @return The guild.
     */
    public Guild getGuild() {
        return parent;
    }
	
	/**
     * Gets whether or not this channel is a private one–if it is a private one, this object is an instance of {@link PrivateChannel}.
     * 
     * @return True if the channel is private, false if otherwise.
     */
    public boolean isPrivate() {
        return isPrivate;
    }
	
	/**
     * Gets the topic for the channel.
     * 
     * @return The channel topic (null if not set).
     */
    public String getTopic() {
        return topic;
    }
	
	/**
     * Sets the CACHED topic for the channel.
     * 
     * @param topic The new channel topic
     */
    @Deprecated
    public void setTopic(String topic) {
        this.topic = topic;
    }
	
	/**
     * Formats a string to be able to #mention this channel.
     * 
     * @return The formatted string.
     */
    public String mention() {
        return "<#" + this.getID() + ">";
    }
    
    /**
     * Sends a message without tts to the desired channel.
     *
     * @param content The content of the message.
     * @return The message object representing the sent message
     */
    public Message sendMessage(String content) {
        return sendMessage(content, false);
    }
    
    /**
     * Sends a message to the desired channel.
     *
     * @param content The content of the message.
     * @param tts Whether the message should use tts or not.
     * @return The message object representing the sent message
     */
    public Message sendMessage(String content, boolean tts) {
        if (client.isReady()) {
            System.out.println(content);
//            content = DiscordUtils.escapeString(content);
            System.out.println(content);
        
            try {
                MessageResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.CHANNELS + id + "/messages",
                        new StringEntity(DiscordUtils.GSON.toJson(new MessageRequest(content, new String[0], tts)), "UTF-8"),
                        new BasicNameValuePair("authorization", client.getToken()),
                        new BasicNameValuePair("content-type", "application/json")), MessageResponse.class);
            
                String time = response.timestamp;
                String messageID = response.id;
            
                Message message = new Message(client, messageID, content, client.getOurUser(), this, 
                        DiscordUtils.convertFromTimestamp(time), DiscordUtils.mentionsFromJSON(client, response),
                        DiscordUtils.attachmentsFromJSON(response));
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
	
	/**
     * Sends a file to the channel.
     * 
     * @param file The file to send.
     * @return The message sent.
     * 
     * @throws HTTP403Exception
     * @throws IOException
     */
    public Message sendFile(File file) throws HTTP403Exception, IOException {
        if (client.isReady()) {
            //These next two lines of code took WAAAAAY too long to figure out than I care to admit
            HttpEntity fileEntity = MultipartEntityBuilder.create().addBinaryBody("file", file, 
                    ContentType.create(Files.probeContentType(file.toPath())), file.getName()).build();
            MessageResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(
                    DiscordEndpoints.CHANNELS + id + "/messages",
                    fileEntity, new BasicNameValuePair("authorization", client.getToken())), MessageResponse.class);
            Message message = new Message(client, response.id, response.content, client.getOurUser(), this,
                    DiscordUtils.convertFromTimestamp(response.timestamp), DiscordUtils.mentionsFromJSON(client, response),
                    DiscordUtils.attachmentsFromJSON(response));
            addMessage(message);
            client.getDispatcher().dispatch(new MessageSendEvent(message));
            return message;
        } else {
            Discord4J.LOGGER.error("Bot has not signed in yet!");
            return null;
        }
    }
    
    /**
     * Generates an invite for this channel.
     * 
     * @param maxAge How long the invite should be valid, setting it to 0 makes it last forever.
     * @param maxUses The maximum uses for the invite, setting it to 0 makes the invite have unlimited uses.
     * @param temporary Whether users admitted with this invite are temporary.
     * @param useXkcdPass Whether to generate a human-readable code, maxAge cannot be 0 for this to work.
     * @return The newly generated invite.
	 */
    public Invite createInvite(int maxAge, int maxUses, boolean temporary, boolean useXkcdPass) {
        try {
            ExtendedInviteResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.CHANNELS + getID() + "/invites",
					new StringEntity(DiscordUtils.GSON.toJson(new InviteRequest(maxAge, maxUses, temporary, useXkcdPass))),
					new BasicNameValuePair("authorization", client.getToken()),
                    new BasicNameValuePair("content-type", "application/json")), ExtendedInviteResponse.class);
            
            return DiscordUtils.getInviteFromJSON(client, response);
        } catch (HTTP403Exception | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Toggles whether the bot is "typing".
     *
     */
    public synchronized void toggleTypingStatus() {
        isTyping.set(!isTyping.get());
        
        if (isTyping.get()) {
            typingTimer.set(System.currentTimeMillis()-TIME_FOR_TYPE_STATUS);
            new Thread(() -> {
                while (isTyping.get()) {
                    if (typingTimer.get() <= System.currentTimeMillis()-TIME_FOR_TYPE_STATUS) {
                        typingTimer.set(System.currentTimeMillis());
                        try {
                            Requests.POST.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/typing",
                                    new BasicNameValuePair("authorization", client.getToken()));
                        } catch (HTTP403Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }
    
    /**
     * Gets whether the bot is "typing".
     *
     * @return True if the bot is typing, false if otherwise.
     */
    public synchronized boolean getTypingStatus() {
        return isTyping.get();
    }
	
	/**
     * Gets the last read message id.
     * 
     * @return The message id.
     */
    public String getLastReadMessageID() {
        return lastReadMessageID;
    }
	
	/**
     * Gets the last read message
     * 
     * @return The message.
     */
    public Message getLastReadMessage() {
        return getMessageByID(lastReadMessageID);
    }
	
	/**
     * Sets the CACHED last read message id.
     * 
     * @param lastReadMessageID The message id.
     */
    @Deprecated
    public void setLastReadMessageID(String lastReadMessageID) {
        this.lastReadMessageID = lastReadMessageID;
    }
    
    @Override 
    public String toString() {
        return mention();
    }
    
    @Override
    public boolean equals(Object other) {
        return this.getClass().isAssignableFrom(other.getClass()) && ((Channel) other).getID().equals(getID());
    }
}
