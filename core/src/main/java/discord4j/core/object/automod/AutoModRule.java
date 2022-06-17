package discord4j.core.object.automod;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.AutoModRuleData;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AutoModRule implements Entity {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final AutoModRuleData data;

    public AutoModRule(final GatewayDiscordClient gateway, final AutoModRuleData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    public Snowflake getGuildId() {
        return Snowflake.of(data.guildId());
    }

    public Snowflake getCreatorId() {
        return Snowflake.of(data.creatorId());
    }

    /**
     * Requests to retrieve the {@link User} who has created this rule.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} that has started typing.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getCreatorUser() {
        return getClient().getUserById(getCreatorId());
    }

    /**
     * Gets if the rule is enabled.
     *
     * @return {@code true} if this rule is enabled, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return data.enabled();
    }

    public String getName() {
        return data.name();
    }

    public TriggerType getTriggerType() {
        return TriggerType.of(data.triggerType());
    }

    public AutoModRuleTriggerMetaData getTriggerMetaData() {
        return new AutoModRuleTriggerMetaData(gateway, data.triggerMetadata());
    }

    public EventType getEventType() {
        return EventType.of(data.eventType());
    }

    public List<Snowflake> getExemptRolesIds() {
        return data.exemptRoles().stream()
                .map(Snowflake::of)
                .collect(Collectors.toList());
    }

    public List<Snowflake> getExemptChannelsIds() {
        return data.exemptChannels().stream()
                .map(Snowflake::of)
                .collect(Collectors.toList());
    }

    /**
     * Represents a Trigger Type of AutoMod Rule.
     */
    public enum TriggerType {
        /**
         * Unknown type.
         */
        UNKNOWN(-1),

        KEYWORD(1),

        HARMFUL_LINK(2),

        SPAM(3),

        KEYWORD_PRESET(4);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code AutoModRule.TriggerType}.
         *
         * @param value The underlying value as represented by Discord.
         */
        TriggerType(final int value) {
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
        public static AutoModRule.TriggerType of(final int value) {
            for (AutoModRule.TriggerType type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * Represents an Event Type of AutoMod Rule.
     */
    public enum EventType {
        /**
         * Unknown type.
         */
        UNKNOWN(-1),

        /**
         * When a member sends or edits a message in the guild
         */
        MESSAGE_SEND(1);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code AutoModRule.EventType}.
         *
         * @param value The underlying value as represented by Discord.
         */
        EventType(final int value) {
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
        public static AutoModRule.EventType of(final int value) {
            switch (value) {
                case 1: return MESSAGE_SEND;
                default: return UNKNOWN;
            }
        }

    }

}
