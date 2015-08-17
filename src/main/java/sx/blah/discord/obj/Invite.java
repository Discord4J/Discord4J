package sx.blah.discord.obj;

/**
 * @author qt
 * @since 9:48 PM 17 Aug, 2015
 * Project: DiscordAPI
 */
public class Invite {
    /**
     * ID of the user who invited you.
     */
    private final String inviterID;

    /**
     * Username of the user who invited you.
     */
    private final String inviterUsername;

    /**
     * ID of the guild you were invited to.
     */
    private final String guildID;

    /**
     * Name of the guild you were invited to.
     */
    private final String guildName;

    /**
     * ID of the channel you were invited from.
     */
    private final String channelID;

    /**
     * Name of the channel you were invited from.
     */
    private final String channelName;

    public Invite(String inviterID, String inviterUsername, String guildID, String guildName, String channelID, String channelName) {
        this.inviterID = inviterID;
        this.inviterUsername = inviterUsername;
        this.guildID = guildID;
        this.guildName = guildName;
        this.channelID = channelID;
        this.channelName = channelName;
    }

    public String getInviterID() {
        return inviterID;
    }

    public String getInviterUsername() {
        return inviterUsername;
    }

    public String getGuildID() {
        return guildID;
    }

    public String getGuildName() {
        return guildName;
    }

    public String getChannelID() {
        return channelID;
    }

    public String getChannelName() {
        return channelName;
    }
}
