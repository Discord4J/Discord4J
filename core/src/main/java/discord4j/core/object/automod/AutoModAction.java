package discord4j.core.object.automod;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.AutoModActionData;

import java.util.Objects;

public class AutoModAction {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final AutoModActionData data;

    public AutoModAction(final GatewayDiscordClient gateway, final AutoModActionData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    public Type getType() {
        return Type.of(data.type());
    }

    public Object getMetadata() {
        //TODO: Waiting for the PR def
        return null;
    }

    /**
     * Represents an Action Type of AutoMod Action.
     */
    public enum Type {
        /**
         * Unknown type.
         */
        UNKNOWN(-1),

        BLOCK_MESSAGE(1),

        LOG_TO_CHANNEL(2);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code AutoModAction.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
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
         * Gets the type of message. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of message.
         */
        public static AutoModAction.Type of(final int value) {
            switch (value) {
                case 1: return BLOCK_MESSAGE;
                case 2: return LOG_TO_CHANNEL;
                default: return UNKNOWN;
            }
        }
    }

}
