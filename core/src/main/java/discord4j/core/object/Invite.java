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
package discord4j.core.object;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.CategorizableChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.InviteData;
import discord4j.discordjson.json.PartialGuildData;
import discord4j.discordjson.json.UserData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A Discord invite.
 *
 * @see <a href="https://discord.com/developers/docs/resources/invite">Invite Resource</a>
 */
public class Invite implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final InviteData data;

    /**
     * Constructs a {@code Invite} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Invite(final GatewayDiscordClient gateway, final InviteData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public final GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the invite code (unique ID).
     *
     * @return The invite code (unique ID).
     */
    public final String getCode() {
        return data.code();
    }

    /**
     * Gets the ID of the guild this invite is associated to.
     *
     * @return The ID of the guild this invite is associated to.
     */
    public final Optional<Snowflake> getGuildId() {
        return data.guild().toOptional()
            .map(PartialGuildData::id)
            .map(Snowflake::of);
    }

    /**
     * Requests to retrieve the guild this invite is associated to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this invite is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Guild> getGuild() {
        return getGuildId().map(gateway::getGuildById).orElse(Mono.empty());
    }

    /**
     * Requests to retrieve the guild this invite is associated to, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this invite is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return getGuildId()
                .map(id -> gateway.withRetrievalStrategy(retrievalStrategy).getGuildById(id))
                .orElse(Mono.empty());
    }

    /**
     * Gets the ID of the channel this invite is associated to.
     *
     * @return The ID of the channel this invite is associated to.
     */
    public final Snowflake getChannelId() {
        return Snowflake.of(data.channel().id());
    }

    /**
     * Requests to retrieve the channel this invite is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link CategorizableChannel channel} this invite is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<CategorizableChannel> getChannel() {
        return gateway.getChannelById(getChannelId()).cast(CategorizableChannel.class);
    }

    /**
     * Requests to retrieve the channel this invite is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link CategorizableChannel channel} this invite is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<CategorizableChannel> getChannel(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy)
                .getChannelById(getChannelId())
                .cast(CategorizableChannel.class);
    }

    /**
     * Gets the ID of the user who created the invite, if present.
     *
     * @return The ID of the user who created the invite, if present.
     */
    public final Optional<Snowflake> getInviterId() {
        return data.inviter().toOptional()
            .map(UserData::id)
            .map(Snowflake::of);
    }

    /**
     * Gets the user who created the invite, if present.
     *
     * @return The user who created the invite, if present.
     */
    public final Optional<User> getInviter() {
        return data.inviter().toOptional()
                .map(data -> new User(gateway, data));
    }

    /**
     * Gets the ID of the target user this invite is associated to, if present.
     *
     * @return The ID of the target user this invite is associated to, if present.
     */
    public final Optional<Snowflake> getTargetUserId() {
        return data.targetUser().toOptional()
            .map(UserData::id)
            .map(Snowflake::of);
    }

    /**
     * Gets the target user this invite is associated to, if present.
     *
     * @return The target user this invite is associated to, if present.
     */
    public final Optional<User> getTargetUser() {
        return data.targetUser().toOptional()
                .map(data -> new User(gateway, data));
    }

    /**
     * Gets the type of target user for this invite, if present.
     *
     * @return The type of target user for this invite, if present.
     * @deprecated Use {@link Invite#getTargetType()}
     */
    @Deprecated
    public final Optional<Type> getTargetUserType() {
        return getTargetType();
    }

    /**
     * Gets the type of target for this voice channel invite, if present.
     *
     * @return The type of target for this voice channel invite, if present.
     */
    public final Optional<Type> getTargetType() {
        return data.targetType().toOptional()
            .map(Type::of);
    }

    /**
     * Gets an approximate count of online members, returned from the {@link discord4j.rest.route.Routes#INVITE_GET}
     * endpoint when {@code with_counts} is true.
     *
     * @return An approximate count of online members, returned from the {@link discord4j.rest.route.Routes#INVITE_GET}
     * endpoint when {@code with_counts} is true.
     */
    public final OptionalInt getApproximatePresenceCount() {
        return data.approximatePresenceCount().toOptional()
            .map(OptionalInt::of)
            .orElse(OptionalInt.empty());
    }

    /**
     * Gets an approximate count of total members, returned from the {@link discord4j.rest.route.Routes#INVITE_GET}
     * endpoint when {@code with_counts} is true.
     *
     * @return An approximate count of total members, returned from the {@link discord4j.rest.route.Routes#INVITE_GET}
     * endpoint when {@code with_counts} is true.
     */
    public final OptionalInt getApproximateMemberCount() {
        return data.approximateMemberCount().toOptional()
            .map(OptionalInt::of)
            .orElse(OptionalInt.empty());
    }

    /**
     * Requests to delete this invite.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the invite has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this invite while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the invite has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Void> delete(@Nullable final String reason) {
        return gateway.getRestClient().getInviteService()
                .deleteInvite(getCode(), reason)
                .then();
    }

    /**
     * Gets the raw data as represented by Discord.
     *
     * @return The raw data as represented by Discord.
     */
    InviteData getData() {
        return data;
    }

    /** Represents the various types of target user for an invite. */
    public enum Type {

        /** Unknown type. */
        UNKNOWN(-1),

        /** Stream. */
        STREAM(1),

        /** Embedded application. */
        EMBEDDED_APPLICATION(2);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Invite.Type}.
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
         * Gets the type of target user. It is guaranteed that invoking {@link #getValue()} from the returned enum
         * will be equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of target user.
         */
        public static Type of(final int value) {
            switch (value) {
                case 1: return STREAM;
                case 2: return EMBEDDED_APPLICATION;
                default: return UNKNOWN;
            }
        }

    }

    @Override
    public String toString() {
        return "Invite{" +
                "data=" + data +
                '}';
    }
}
