package sx.blah.discord.obj;

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
    private final String channel_id;

    /**
     * All users @mentioned in the
     * message.
     */
    private final String[] mentionedIDs;

	/**
     * The time the message was received.
     */
    private final LocalDateTime timestamp;

    public Message(String messageID, String content, User user, String channel_id, String[] mentionedIDs, LocalDateTime timestamp) {
        this.messageID = messageID;
        this.content = content;
	    this.author = user;
        this.channel_id = channel_id;
        this.mentionedIDs = mentionedIDs;
	    this.timestamp = timestamp;
    }

    // Getters. Boring.

    public String getContent() {
        return content;
    }

    public String getChannelID() {
        return channel_id;
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
    public void reply(String content, DiscordClient client) throws IOException, ParseException {
        client.sendMessage("@" + this.getAuthor().getName() + ", "
                + content, this.getChannelID(), this.getAuthor().getID());
    }
}
