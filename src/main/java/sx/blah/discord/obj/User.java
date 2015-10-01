package sx.blah.discord.obj;

import sx.blah.discord.DiscordEndpoints;

/**
 * @author qt
 * @since 5:40 PM 15 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * This class defines the Discord user.
 */
public class User {
    /**
     * Display name of the user.
     */
    private String name;

    /**
     * The user's avatar location.
     */
    private String avatar;

    /**
     * User ID.
     */
    private final String id;

    /**
     * User discriminator.
     * Distinguishes users with the same name.
     * <p>
     * This is here in case it becomes necessary.
     */
    private int discriminator;

    /**
     * This user's presence.
     * One of [online/idle/offline].
     */
    private String presence;

	/**
	 * The user's avatar in URL form.
	 */
	private String avatarURL;

    public User(String name, String id, String avatar) {
	    this.id = id;
	    this.name = name;
	    this.avatar = avatar;
	    this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
    }

    // -- Getters and setters. Pretty boring.

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAvatarURL() {
		return avatarURL;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
	    this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }
}
