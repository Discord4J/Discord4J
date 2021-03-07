package discord4j.rest.util;

import java.util.Arrays;

public enum InteractionResponseType {
    /**
     * Unknown type
     */
    UNKNOWN(-1),
    /**
     * ACK a Ping
     */
    PONG(1),
    /**
     * ACK a command without sending a message, eating the user's input
     */
    ACKNOWLEDGE(2),
    /**
     * respond with a message, eating the user's input
     */
    CHANNEL_MESSAGE(3),
    /**
     * respond with a message, showing the user's input
     */
    CHANNEL_MESSAGE_WITH_SOURCE(4),
    /**
     * ACK a command without sending a message, showing the user's input
     */
    ACKNOWLEDGE_WITH_SOURCE(5);

    /**
     * The underlying value as represented by Discord.
     */
    private final int value;

    /**
     * Constructs an {@code InteractionResponseType}.
     *
     * @param value The underlying value as represented by Discord.
     */
    InteractionResponseType(final int value) {
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
     * Gets the type of an interaction response. It is guaranteed that invoking {@link #getValue()} from the returned enum will
     * equal ({@code ==}) the supplied {@code value}.
     *
     * @param value The underlying value as represented by Discord.
     * @return The type of response.
     */
    public static InteractionResponseType of(final int value) {
        return Arrays.stream(values()).filter(type -> type.getValue() == value).findFirst().orElse(UNKNOWN);
    }
}
