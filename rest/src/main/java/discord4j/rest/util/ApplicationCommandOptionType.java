package discord4j.rest.util;

import java.util.Arrays;

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
        return Arrays.stream(values()).filter(type -> type.getValue() == value).findFirst().orElse(UNKNOWN);
    }
}
