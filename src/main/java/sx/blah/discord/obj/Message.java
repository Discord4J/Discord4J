package sx.blah.discord.obj;

import org.json.simple.parser.ParseException;
import sx.blah.discord.DiscordClient;

import java.io.IOException;

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
     * The ID of the author.
     */
    private final String author_id;

    /**
     * The ID of the channel the message was sent in.
     */
    private final String channel_id;

    /**
     * The author's username
     */
    private final String authorUsername;

    /**
     * All users @mentioned in the
     * message.
     */
    private final String[] mentionedIDs;

    public Message(String messageID, String content, String author_id, String authorUsername, String channel_id, String[] mentionedIDs) {
        this.messageID = messageID;
        this.content = content;
        this.author_id = author_id;
        this.authorUsername = authorUsername;
        this.channel_id = channel_id;
        this.mentionedIDs = mentionedIDs;
    }

    // Getters. Boring.

    public String getContent() {
        return content;
    }

    public String getAuthorID() {
        return author_id;
    }

    public String getChannelID() {
        return channel_id;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getMessageID() {
        return messageID;
    }

    public String[] getMentionedIDs() {
        return mentionedIDs;
    }

    /**
     * Adds an @mention to the author of the referenced Message
     * object before your content
     *
     * @param content Message to send.
     */
    public void reply(String content, DiscordClient client) throws IOException, ParseException {
        client.sendMessage("@" + this.getAuthorUsername() + ", "
                + content, this.getChannelID(), this.getAuthorID());
    }
}
