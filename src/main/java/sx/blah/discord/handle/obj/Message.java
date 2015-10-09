// Discord4J - Unofficial wrapper for Discord API
// Copyright (c) 2015
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package sx.blah.discord.handle.obj;

import org.json.simple.parser.ParseException;
import sx.blah.discord.DiscordClient;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author qt
 * @since 7:53 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * Stores relevant data about messages.
 */
public class Message {
    /**
     * The ID of the message. Used for message updating.
     */
    private final String messageID;

    /**
     * The actual message (what you see
     * on your screen, the content).
     */
    private final String content;

	/**
	 * The User who sent the message.
	 */
	private final User author;

    /**
     * The ID of the channel the message was sent in.
     */
    private final String channelID;

    /**
     * All users @mentioned in the
     * message.
     */
    private final String[] mentionedIDs;

	/**
     * The time the message was received.
     */
    private final LocalDateTime timestamp;

    public Message(String messageID, String content, User user, String channelID, String[] mentionedIDs, LocalDateTime timestamp) {
        this.messageID = messageID;
        this.content = content;
	    this.author = user;
        this.channelID = channelID;
        this.mentionedIDs = mentionedIDs;
	    this.timestamp = timestamp;
    }

    // Getters. Boring.

    public String getContent() {
        return content;
    }

    public String getChannelID() {
        return channelID;
    }

	public User getAuthor() {
		return author;
	}

    public String getMessageID() {
        return messageID;
    }

    public String[] getMentionedIDs() {
        return mentionedIDs;
    }

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

    /**
     * Adds an @mention to the author of the referenced Message
     * object before your content
     *
     * @param content Message to send.
     */
    public void reply(String content) throws IOException, ParseException {
        DiscordClient.get().sendMessage("@" + this.getAuthor().getName() + ", "
                + content, this.getChannelID(), this.getAuthor().getID());
    }
}
