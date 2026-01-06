/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.IntegrationAccount;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.IntegrationData;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

/**
 * A Discord integration.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#integration-object">Integration Resource</a>
 */
public class Integration implements Entity {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final IntegrationData data;

    /** The ID of the guild this integration is associated to. */
    private final long guildId;

    /**
     * Constructs an {@code Integration} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this integration is associated to.
     */
    public Integration(final GatewayDiscordClient gateway, final IntegrationData data, final long guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the ID of the guild this integration is associated to.
     *
     * @return The ID of the guild this integration is associated to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Gets the data of the integration.
     *
     * @return The data of the integration.
     */
    public IntegrationData getData() {
        return data;
    }

    /**
     * Gets the integration name.
     *
     * @return The integration name.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the integration type (twitch, youtube, or discord).
     *
     * @return The integration type (twitch, youtube, or discord).
     */
    public String getType() {
        return data.type();
    }

    /**
     * Gets whether the integration is enabled.
     *
     * @return Whether the integration is enabled.
     */
    public boolean isEnabled() {
        return data.enabled().toOptional().orElse(true);
    }

    /**
     * Gets whether the integration is syncing.
     *
     * @return Whether the integration is syncing.
     */
    public boolean isSyncing() {
        return data.syncing().toOptional().orElse(false);
    }

    /**
     * Gets the id that this integration uses for "subscribers".
     *
     * @return The id that this integration uses for "subscribers".
     */
    public Optional<Snowflake> getSubscriberRoleId() {
        return data.roleId().toOptional().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the role that this integration uses for "subscribers".
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role role} that this integration uses
     * for "subscribers". If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getSubscriberRole() {
        return Mono.justOrEmpty(getSubscriberRoleId())
                .flatMap(id -> gateway.getRoleById(getGuildId(), id));
    }

    /**
     * Requests to retrieve the role that this integration uses for "subscribers", using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the role
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role role} that this integration uses
     * for "subscribers". If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getSubscriberRole(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getSubscriberRoleId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getRoleById(getGuildId(), id));
    }

    /**
     * Gets whether emoticons should be synced for this integration (twitch only currently).
     *
     * @return Whether emoticons should be synced for this integration (twitch only currently).
     */
    public boolean isEnableEmoticons() {
        return data.enableEmoticons().toOptional().orElse(false);
    }

    /**
     * Gets the behavior of expiring subscribers, if present.
     *
     * @return The behavior of expiring subscribers, if present.
     */
    public Optional<ExpireBehavior> getExpireBehavior() {
        return data.expireBehavior().toOptional().map(ExpireBehavior::of);
    }

    /**
     * Gets the grace period (in days) before expiring subscribers, if present.
     *
     * @return The grace period (in days) before expiring subscribers, if present.
     */
    public Optional<Integer> getExpireGracePeriod() {
        return data.expireGracePeriod().toOptional();
    }

    /**
     * Gets the user for this integration, if present.
     *
     * @return The user for this integration, if present.
     */
    public Optional<User> getUser() {
        return data.user().toOptional().map(data -> new User(gateway, data));
    }

    /**
     * Gets the integration account information.
     *
     * @return The integration account information.
     */
    public IntegrationAccount getAccount() {
        return new IntegrationAccount(gateway, data.account());
    }

    /**
     * Gets	when this integration was last synced, if present.
     *
     * @return When this integration was last synced, if present.
     */
    public Optional<Instant> getSyncedAt() {
        return data.syncedAt().toOptional()
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets how many subscribers this integration has, if present.
     *
     * @return How many subscribers this integration has, if present.
     */
    public Optional<Integer> getSubscriberCount() {
        return data.subscriberCount().toOptional();
    }

    /**
     * Gets whether integration has been revoked.
     *
     * @return Whether integration has been revoked.
     */
    public boolean isRevoked() {
        return data.revoked().toOptional().orElse(false);
    }

    /**
     * Gets the bot/OAuth2 application for discord integrations, if present.
     *
     * @return The bot/OAuth2 application for discord integrations, if present.
     */
    public Optional<IntegrationApplication> getApplication() {
        return data.application().toOptional()
                .map(data -> new IntegrationApplication(gateway, data));
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "Integration{" +
                "data=" + data +
                '}';
    }

    /** Represents the various integration expire behaviors. */
    public enum ExpireBehavior {

        /** Unknown. */
        UNKNOWN(-1),

        /** Remove role. */
        REMOVE_ROLE(0),

        /** Kick. */
        KICK(1);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs an {@code Integration.ExpireBehavior}.
         *
         * @param value The underlying value as represented by Discord.
         */
        ExpireBehavior(final int value) {
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
         * Gets the integration expire behaviors. It is guaranteed that invoking {@link #getValue()} from the returned
         * enum will equal ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The integration expire behaviors.
         */
        public static Integration.ExpireBehavior of(final int value) {
            switch (value) {
                case 0: return REMOVE_ROLE;
                case 1: return KICK;
                default: return UNKNOWN;
            }
        }
    }

}
