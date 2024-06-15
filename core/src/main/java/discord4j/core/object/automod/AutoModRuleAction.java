package discord4j.core.object.automod;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.AutoModActionData;
import discord4j.discordjson.json.AutoModActionMetaData;

import java.util.Objects;
import java.util.Optional;

/**
 * An Auto Moderation Rule Action
 *
 * @see <a href="https://discord.com/developers/docs/resources/auto-moderation#auto-moderation-action-object">Auto Moderation Rule Action</a>
 */
public class AutoModRuleAction implements DiscordObject {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final AutoModActionData data;

    public AutoModRuleAction(final GatewayDiscordClient gateway, final AutoModActionData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    public AutoModActionData getData() {
        return data;
    }

    /**
     * Gets the type of this action.
     *
     * @return A {@link Type}
     */
    public Type getType() {
        return Type.of(data.type());
    }

    /**
     * Gets the MetaData of the action if set.
     *
     * @return the MetaData of the action if available.
     */
    public Optional<AutoModRuleActionMetaData> getMetadata() {
        return this.data.metadata().toOptional().map(data -> new AutoModRuleActionMetaData(gateway, data));
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * An Auto Moderation Rule Action MetaData
     *
     * @see <a href="https://discord.com/developers/docs/resources/auto-moderation#auto-moderation-action-object-action-metadata">Auto Moderation Rule Action MetaData</a>
     */
    public static class AutoModRuleActionMetaData implements DiscordObject {
        /**
         * The gateway associated to this object.
         */
        private final GatewayDiscordClient gateway;

        /**
         * The raw data as represented by Discord.
         */
        private final AutoModActionMetaData data;

        public AutoModRuleActionMetaData(final GatewayDiscordClient gateway, final AutoModActionMetaData data) {
            this.gateway = Objects.requireNonNull(gateway);
            this.data = Objects.requireNonNull(data);
        }

        /**
         * Gets the channel´s id to which user content should be logged.
         * <br>
         * <b>Note:</b> This is mostly present the action is of type {@link Type#SEND_ALERT_MESSAGE}.
         *
         * @return The ID of the channel involved, if present.
         */
        public Optional<Snowflake> getChannelId() {
            return data.channelId().toOptional().map(Snowflake::of);
        }

        /**
         * Gets the timeout duration.
         * <br>
         * <b>Note:</b> This is mostly present the action is of type {@link Type#TIMEOUT}.
         *
         * @return The duration in seconds, if present.
         */
        public Optional<Integer> getDurationTimeout() {
            return data.duration().toOptional();
        }

        /**
         * Gets the custom message to show when the action is executed.
         * <br>
         * <b>Note:</b> This is mostly present the action is of type {@link Type#BLOCK_MESSAGE}.
         *
         * @return The custom message, if present.
         */
        public Optional<String> getCustomMessage() {
            return data.customMessage().toOptional();
        }

        @Override
        public GatewayDiscordClient getClient() {
            return this.gateway;
        }
    }

    /**
     * Represents an Action Type of AutoMod Action.
     */
    public enum Type {
        /**
         * Unknown type.
         */
        UNKNOWN(-1),

        /**
         * Blocks the content of a message according to the rule
         */
        BLOCK_MESSAGE(1),

        /**
         * Logs user content to a specified channel
         */
        SEND_ALERT_MESSAGE(2),

        /**
         * Timeout user for a specified duration
         * <br>
         * <b>Note:</b> This action can only be setup for {@link AutoModRule.TriggerType#KEYWORD} and {@link AutoModRule.TriggerType#MENTION_SPAM} rules and
         * the user need to have the permission {@link discord4j.rest.util.Permission#MODERATE_MEMBERS} for use the action
         */
        TIMEOUT(3),

        /**
         * Prevents a member from using text, voice, or other interactions
         */
        BLOCK_MEMBER_INTERACTION(4);

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
        public static AutoModRuleAction.Type of(final int value) {
            for (AutoModRuleAction.Type type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

}
