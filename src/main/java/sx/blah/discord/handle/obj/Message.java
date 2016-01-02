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
import sx.blah.discord.handle.impl.events.MessageUpdateEvent;
import sx.blah.discord.json.requests.MessageRequest;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Requests;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a discord message.
 */
public class Message {
    /**
     * The ID of the message. Used for message updating.
     */
    protected final String messageID;

    /**
     * The actual message (what you see
     * on your screen, the content).
     */
    protected String content;

	/**
	 * The User who sent the message.
	 */
	protected final User author;

    /**
     * The ID of the channel the message was sent in.
     */
    protected final Channel channel;

	/**
     * The time the message was received.
     */
    protected LocalDateTime timestamp;
	
	/**
	 * The list of users mentioned by this message.
	 */
	protected List<User> mentions;
	
	/**
	 * The attachments, if any, on the message.
	 */
	protected List<Attachment> attachments;
	
	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

    public Message(IDiscordClient client, String messageID, String content, User user, Channel channel,
				   LocalDateTime timestamp, List<User> mentions, List<Attachment> attachments) {
        this.client = client;
		this.messageID = messageID;
        this.content = content;
	    this.author = user;
        this.channel = channel;
	    this.timestamp = timestamp;
		this.mentions = mentions;
		this.attachments = attachments;
    }
	
	/**
	 * Gets the string content of the message.
	 * 
	 * @return The content of the message
	 */
    public String getContent() {
        return content;
    }
	
	/**
	 * Sets the CACHED content of the message.
	 * 
	 * @param content The new message content.
	 */
	@Deprecated
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * Gets the channel that this message belongs to.
	 * 
	 * @return The channel.
	 */
    public Channel getChannel() {
        return channel;
    }
	
	/**
	 * Gets the user who authored this message.
	 * 
	 * @return The author.
	 */
	public User getAuthor() {
		return author;
	}
	
	/**
	 * Gets the message id.
	 * 
	 * @return The id.
	 */
    public String getID() {
        return messageID;
    }
	
	/**
	 * Sets the CACHED version of the message timestamp.
	 * 
	 * @param timestamp The timestamp.
	 */
	@Deprecated
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Gets the timestamp for when this message was sent/edited.
	 * 
	 * @return The timestamp.
	 */
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Gets the users mentioned in this message.
	 * 
	 * @return The users mentioned.
	 */
	public List<User> getMentions() {
		return mentions;
	}
	
	/**
	 * Gets the attachments in this message.
	 * 
	 * @return The attachments.
	 */
	public List<Attachment> getAttachments() {
		return attachments;
	}

    /**
     * Adds an "@mention," to the author of the referenced Message
     * object before your content
	 * 
     * @param content Message to send.
     */
    public void reply(String content) throws IOException {
        getChannel().sendMessage(String.format("%s, %s", this.getAuthor(), content));
    }
	
	/**
	 * Edits the message. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param content The new content for the message to contain.
	 * @return The new message (this).
	 */
	public Message edit(String content) {
		if (client.isReady()) {
			content = DiscordUtils.escapeString(content);
			
			try {
				MessageResponse response = DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.CHANNELS + channel.getID() + "/messages/" + messageID,
						new StringEntity(DiscordUtils.GSON.toJson(new MessageRequest(content, new String[0], false)), "UTF-8"),
						new BasicNameValuePair("authorization", client.getToken()),
						new BasicNameValuePair("content-type", "application/json")), MessageResponse.class);
				
				Message oldMessage = new Message(client, this.messageID, this.content, author, channel, timestamp, mentions, attachments);
				this.content = response.content;
				this.timestamp = DiscordUtils.convertFromTimestamp(response.edited_timestamp);
				this.mentions = DiscordUtils.mentionsFromJSON(client, response);
				this.attachments = DiscordUtils.attachmentsFromJSON(response);
				//Event dispatched here because otherwise there'll be an NPE as for some reason when the bot edits a message,
				// the event chain goes like this:
				//Original message edited to null, then the null message edited to the new content
				client.getDispatcher().dispatch(new MessageUpdateEvent(oldMessage, this));
			} catch (HTTP403Exception e) {
				Discord4J.LOGGER.error("Received 403 error attempting to send message; is your login correct?");
			}
			
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
		}
		return this;
	}
	
	/**
	 * Deletes the message.
	 */
	public void delete() {
		if (client.isReady()) {
			try {
				Requests.DELETE.makeRequest(DiscordEndpoints.CHANNELS + channel.getID() + "/messages/" + messageID,
						new BasicNameValuePair("authorization", client.getToken()));
			} catch (HTTP403Exception e) {
				Discord4J.LOGGER.error("Received 403 error attempting to delete message; is your login correct?");
			}
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
		}
	}
	
	/**
	 * Acknowledges a message and all others before it (marks it as "read").
	 */
	public void acknowledge() throws HTTP403Exception {
		Requests.POST.makeRequest(DiscordEndpoints.CHANNELS + getChannel().getID() + "/messages/" + getID() + "/ack",
				new BasicNameValuePair("authorization", client.getToken()));
		channel.setLastReadMessageID(getID());
	}
	
	/**
	 * Checks if the message has been read by this account.
	 * 
	 * @return True if the message has been read, false if otherwise.
	 */
	public boolean isAcknowledged() {
		if (channel.getLastReadMessageID().equals(getID()))
			return true;
		
		Message lastRead = channel.getLastReadMessage();
		LocalDateTime timeStamp = lastRead.getTimestamp();
		return timeStamp.compareTo(getTimestamp()) >= 0;
	}
	
	
	@Override
	public boolean equals(Object other) {
		return this.getClass().isAssignableFrom(other.getClass()) && ((Message) other).getID().equals(getID());
	}
	
	/**
	 * Represents an attachment included in the message.
	 */
	public static class Attachment {
		
		/**
		 * The file name of the attachment.
		 */
		protected final String filename;
		
		/**
		 * The size, in bytes of the attachment.
		 */
		protected final int filesize;
		
		/**
		 * The attachment id.
		 */
		protected final String id;
		
		/**
		 * The download link for the attachment.
		 */
		protected final String url;
		
		public Attachment(String filename, int filesize, String id, String url) {
			this.filename = filename;
			this.filesize = filesize;
			this.id = id;
			this.url = url;
		}
		
		/**
		 * Gets the file name for the attachment.
		 * 
		 * @return The file name of the attachment.
		 */
		public String getFilename() {
			return filename;
		}
		
		/**
		 * Gets the size of the attachment.
		 * 
		 * @return The size, in bytes of the attachment.
		 */
		public int getFilesize() {
			return filesize;
		}
		
		/**
		 * Gets the id of the attachment.
		 * 
		 * @return The attachment id.
		 */
		public String getId() {
			return id;
		}
		
		/**
		 * Gets the direct link to the attachment.
		 * 
		 * @return The download link for the attachment.
		 */
		public String getUrl() {
			return url;
		}
	}
}
