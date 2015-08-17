package sx.blah.discord.obj;

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
     * All users connected to the channel.
     */
    private final List<User> users;

    public Channel(String name, String id, List<User> users) {
        this.name = name;
        this.id = id;
        this.users = users;
    }

    // Getters.

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
