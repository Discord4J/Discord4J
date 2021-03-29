package discord4j.rest.util;

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
     * Respond to an interaction with a message
     */
    CHANNEL_MESSAGE_WITH_SOURCE(4),
    /**
     * ACK an interaction and send a response later, the user sees a loading state
     */
    DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5);

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
        switch (value) {
            case 1: return PONG;
            case 4: return CHANNEL_MESSAGE_WITH_SOURCE;
            case 5: return DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE;
            default: return UNKNOWN;
        }
    }
}
