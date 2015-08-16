package sx.blah.discord.obj;

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
     * Creates User objects (orly?)
     */
    public User(String name, String id) {
        this.id = id;
        this.name = name;
    }

    public User(String name, String id, String avatar) {
        this(name, id);
        this.avatar = avatar;
    }

    // -- Getters and setters. Pretty boring.

    public String getId() {
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
