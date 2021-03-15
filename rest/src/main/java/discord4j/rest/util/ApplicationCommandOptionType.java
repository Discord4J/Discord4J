package discord4j.rest.util;

public enum ApplicationCommandOptionType {
    UNKNOWN(-1),
    SUB_COMMAND(1),
    SUB_COMMAND_GROUP(2),
    STRING(3),
    INTEGER(4),
    BOOLEAN(5),
    USER(6),
    CHANNEL(7),
    ROLE(8);

    /**
     * The underlying value as represented by Discord.
     */
    private final int value;

    /**
     * Constructs an {@code ApplicationCommandOptionType}.
     *
     * @param value The underlying value as represented by Discord.
     */
    ApplicationCommandOptionType(final int value) {
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
     * Gets the type of an application command option. It is guaranteed that invoking {@link #getValue()} from the returned enum will
     * equal ({@code ==}) the supplied {@code value}.
     *
     * @param value The underlying value as represented by Discord.
     * @return The type of option.
     */
    public static ApplicationCommandOptionType of(final int value) {
        switch (value) {
            case 1: return SUB_COMMAND;
            case 2: return SUB_COMMAND_GROUP;
            case 3: return STRING;
            case 4: return INTEGER;
            case 5: return BOOLEAN;
            case 6: return USER;
            case 7: return CHANNEL;
            case 8: return ROLE;
            default: return UNKNOWN;
        }
    }
}
