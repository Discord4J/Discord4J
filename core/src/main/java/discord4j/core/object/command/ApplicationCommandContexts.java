package discord4j.core.object.command;

/**
 * The contexts in which an application command can be used.
 */
public enum ApplicationCommandContexts {

    /**
     * Interaction can be used within servers.
     */
    GUILD(0),

    /**
     * Interaction can be used within DMs with the app's bot user
     */
    BOT_DM(1),

    /**
     * Interaction can be used within Group DMs and DMs other than the app's bot user
     */
    PRIVATE_CHANNEL(2);

    /**
     * The underlying value as represented by Discord.
     */
    private final int value;

    ApplicationCommandContexts(final int value) {
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
    public static ApplicationCommandContexts of(final int value) {
        switch (value) {
            default:
            case 0:
                return GUILD;
            case 1:
                return BOT_DM;
            case 2:
                return PRIVATE_CHANNEL;
        }
    }
}
