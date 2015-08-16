package sx.blah.discord.obj;

/**
 * @author qt
 * @since 7:53 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * Stores relevant data about messages.
 */
public class Message {
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

    public Message(String content, String author_id, String channel_id) {
        this.content = content;
        this.author_id = author_id;
        this.channel_id = channel_id;
    }

    // Getters. Boring.

    public String getContent() {
        return content;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public String getChannel_id() {
        return channel_id;
    }
}
