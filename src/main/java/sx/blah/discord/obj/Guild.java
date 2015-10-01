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

    /**
     * The name of the guild.
     */
    private final String name;

    /**
     * The ID of this guild.
     */
    private final String id;

    public Guild(String name, String id, List<Channel> channels, List<User> users) {
        this.name = name;
        this.channels = channels;
        this.users = users;
        this.id = id;
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
            if (c.getChannelID().equalsIgnoreCase(id))
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
	    if(null == users)
		    return null;
        for (User user : users) {
            if (null != user
		            && null != user.getID()
                    && user.getID().equalsIgnoreCase(id))
                return user;
        }

        return null; // Not found, return null.
    }

    /**
     * @return The name of the guild
     */
    public String getName() {
        return name;
    }

    /**
     * @return The ID of this guild.
     */
    public String getID() {
        return id;
    }

    public void addUser(User user) {
		this.users.add(user);
    }

	public void addChannel(Channel channel) {
		this.channels.add(channel);
	}
}
