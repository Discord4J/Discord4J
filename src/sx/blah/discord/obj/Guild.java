package sx.blah.discord.obj;

import java.util.List;

/**
 * @author qt
 * @since 4:05 PM 17 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * This class defines a guild/server/clan/whatever it's called.
 */
public class Guild {
    /**
     * All text channels in the guild.
     */
    private final List<Channel> channels;

    /**
     * All users connected to the guild.
     */
    private final List<User> users;

    public Guild(List<Channel> channels, List<User> users) {
        this.channels = channels;
        this.users = users;
    }

    /**
     * @return All channels on the server.
     */
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * @param id The ID of the channel you want to find.
     * @return The channel with given ID.
     */
    public Channel getChannelByID(String id) {
        for (Channel c : channels) {
            if (c.getId().equalsIgnoreCase(id))
                return c;
        }

        return null; // Not found, return null.
    }

    /**
     * @return All users connected to the guild.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * @param id ID of the user you want to find.
     * @return The user with given ID.
     */
    public User getUserByID(String id) {
        for (User user : users) {
            if (user.getId().equalsIgnoreCase(id))
                return user;
        }

        return null; // Not found, return null.
    }
}
