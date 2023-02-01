package discord4j.core.object.automod;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.User;
import discord4j.core.spec.AutoModRuleEditMono;
import discord4j.core.spec.AutoModRuleEditSpec;
import discord4j.discordjson.json.AutoModRuleData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

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

    /**
     * Gets the data of the rule.
     *
     * @return The data of the rule.
     */
    public AutoModRuleData getData() {
        return data;
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

    /**
     * Gets the {@link Snowflake} of the creator of the rule.
     *
     * @return The {@link Snowflake} of the creator
     */
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

    /**
     * Gets the name of the rule
     *
     * @return the name of the rule
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the type of trigger used in the rule.
     *
     * @return The type of trigger used in the rule.
     */
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
     * Requests to edit this AutoMod rule. Properties specifying how to edit this rule can be set via the {@code
     * withXxx} methods of the returned {@link AutoModRuleEditMono}.
     *
     * @param name new name to set
     * @param eventType type of event to set
     * @param enabled status of the rule
     * @return A {@link AutoModRuleEditMono} where, upon successful completion, emits the edited {@link AutoModRule}. If
     * an error is received, it is emitted through the {@code AutoModRuleEditMono}.
     */
    public AutoModRuleEditMono edit(String name, EventType eventType, boolean enabled) {
        return AutoModRuleEditMono.of(name, eventType.value, enabled, this);
    }

    /**
     * Requests to edit this AutoMod rule.
     *
     * @param spec an immutable object that specifies how to edit this AutoMod Rule
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link AutoModRule}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<AutoModRule> edit(AutoModRuleEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                        () -> gateway.getRestClient().getAutoModService()
                                .modifyAutoModRule(getGuildId().asLong(), getId().asLong(), spec.asRequest(),
                                        spec.reason()))
                .map(data -> new AutoModRule(gateway, data));
    }

    /**
     * Requests to delete this rule.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the rule has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this rule while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the rule has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return gateway.getRestClient().getAutoModService()
                .deleteAutoModRule(getGuildId().asLong(), getId().asLong(), reason);
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

        KEYWORD_PRESET(4),

        MENTION_SPAM(5);

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
