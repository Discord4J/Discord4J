package discord4j.core.object.command;

/**
 * Represents where an app can be installed, also called its supported installation contexts.
 */
public enum ApplicationIntegrationType {

    /**
     * App is installable to servers
     */
    GUILD_INSTALL(0),

    /**
     * App is installable to users
     */
    USER_INSTALL(1);

    /**
     * The underlying value as represented by Discord.
     */
    private final int value;

    ApplicationIntegrationType(final int value) {
        this.value = value;
    }

    /**
     * Gets the underlying value as represented by Discord.
     *
     * @return The underlying value as represented by Discord.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the context of the application command. It is guaranteed that invoking {@link #getValue()} from the
     * returned enum will equal ({@code ==}) the supplied {@code value}.
     *
     * @param value The underlying value as represented by Discord.
     * @return The context of the command.
     */
    public static ApplicationIntegrationType of(final int value) {
        switch (value) {
            default:
            case 0:
                return GUILD_INSTALL;
            case 1:
                return USER_INSTALL;
        }
    }
}
