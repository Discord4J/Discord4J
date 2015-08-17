package sx.blah.discord.obj;

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
    private String name;

    /**
     * Channel ID.
     */
    private final String id;

    /**
     * Messages that have been sent into this channel
     */
    private final List<Message> messages;

    public Channel(String name, String id) {
        this.name = name;
        this.id = id;
        this.messages = new ArrayList<>();
    }

    public Channel(String name, String id, List<Message> messages) {
        this.name = name;
        this.id = id;
        this.messages = messages;
    }

    // Getters.

    public String getName() {
        return name;
    }

    public String getChannelID() {
        return id;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        if (message.getChannelID().equalsIgnoreCase(this.getChannelID())) {
            messages.add(message);
        }
    }
}
