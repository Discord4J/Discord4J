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
import discord4j.core.spec.InviteCreateFields;
import discord4j.discordjson.json.InviteData;
import discord4j.discordjson.json.PartialGuildData;
import discord4j.discordjson.json.PartialRoleDataFields;
import discord4j.discordjson.json.UserData;
import discord4j.rest.util.MultipartRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

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
        return this.gateway;
    }

    /**
     * Gets the invite code (unique ID).
     *
     * @return The invite code (unique ID).
     */
    public final String getCode() {
        return this.getData().code();
    }

    /**
     * Gets the instant this invite expires, if possible.
     *
     * @return The instant this invite expires, if empty, invite is never expiring.
     */
    public final Optional<Instant> getExpiration() {
        return this.getData().expiresAt().map(srtExpiresAt -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(srtExpiresAt, Instant::from));
    }

    /**
     * Gets the ID of the guild this invite is associated to.
     *
     * @return The ID of the guild this invite is associated to.
     */
    public final Optional<Snowflake> getGuildId() {
        return this.getData().guild().toOptional()
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
        return this.getGuildId().map(this.getClient()::getGuildById).orElse(Mono.empty());
    }

    /**
     * Requests to retrieve the guild this invite is associated to, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this invite is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return this.getGuildId()
                .map(id -> this.getClient().withRetrievalStrategy(retrievalStrategy).getGuildById(id))
                .orElse(Mono.empty());
    }

    /**
     * Gets the ID of the channel this invite is associated to.
     *
     * @return The ID of the channel this invite is associated to.
     */
    public final Snowflake getChannelId() {
        return Snowflake.of(this.getData().channel().id());
    }

    /**
     * Requests to retrieve the channel this invite is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link CategorizableChannel channel} this invite is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<CategorizableChannel> getChannel() {
        return this.getClient().getChannelById(getChannelId()).cast(CategorizableChannel.class);
    }

    /**
     * Requests to retrieve the channel this invite is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link CategorizableChannel channel} this invite is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<CategorizableChannel> getChannel(EntityRetrievalStrategy retrievalStrategy) {
        return this.getClient().withRetrievalStrategy(retrievalStrategy)
                .getChannelById(getChannelId())
                .cast(CategorizableChannel.class);
    }

    /**
     * Gets the ID of the user who created the invite, if present.
     *
     * @return The ID of the user who created the invite, if present.
     */
    public final Optional<Snowflake> getInviterId() {
        return this.getData().inviter().toOptional()
            .map(UserData::id)
            .map(Snowflake::of);
    }

    /**
     * Gets the user who created the invite, if present.
     *
     * @return The user who created the invite, if present.
     */
    public final Optional<User> getInviter() {
        return this.getData().inviter().toOptional()
                .map(data -> new User(this.getClient(), data));
    }

    /**
     * Gets the ID of the target user this invite is associated to, if present.
     *
     * @return The ID of the target user this invite is associated to, if present.
     */
    public final Optional<Snowflake> getTargetUserId() {
        return this.getData().targetUser().toOptional()
            .map(UserData::id)
            .map(Snowflake::of);
    }

    /**
     * Gets the target user this invite is associated to, if present.
     *
     * @return The target user this invite is associated to, if present.
     */
    public final Optional<User> getTargetUser() {
        return this.getData().targetUser().toOptional()
                .map(data -> new User(this.getClient(), data));
    }

    /**
     * Gets the type of target user for this invite, if present.
     *
     * @return The type of target user for this invite, if present.
     * @deprecated Use {@link Invite#getTargetType()}
     */
    @Deprecated
    public final Optional<Type> getTargetUserType() {
        return this.getTargetType();
    }

    /**
     * Gets the type of target for this voice channel invite, if present.
     *
     * @return The type of target for this voice channel invite, if present.
     */
    public final Optional<Type> getTargetType() {
        return this.getData().targetType().toOptional()
            .map(Type::of);
    }

    /**
     * Return the flags of this invite.
     *
     * @return A {@code EnumSet} with the flags of this invite.
     */
    public EnumSet<Flag> getFlags() {
        return this.getData().flags().toOptional()
            .map(Invite.Flag::of)
            .orElse(EnumSet.noneOf(Invite.Flag.class));
    }

    /**
     * Gets an approximate count of online members, returned from the {@link discord4j.rest.route.Routes#INVITE_GET}
     * endpoint when {@code with_counts} is true.
     *
     * @return An approximate count of online members, returned from the {@link discord4j.rest.route.Routes#INVITE_GET}
     * endpoint when {@code with_counts} is true.
     */
    public final OptionalInt getApproximatePresenceCount() {
        return this.getData().approximatePresenceCount().toOptional()
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
        return this.getData().approximateMemberCount().toOptional()
            .map(OptionalInt::of)
            .orElse(OptionalInt.empty());
    }

    /**
     * Gets the ids of roles assigned to the user upon accepting the invite.
     * @return The ids of roles assigned to the user upon accepting the invite.
     */
    public final List<Snowflake> getRoleIds() {
        return this.getData().roles().toOptional()
            .map(partialRoleDataFields -> partialRoleDataFields.stream().map(PartialRoleDataFields::id).map(Snowflake::of).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    /**
     * Request the IDs of target users associated with this invite.
     *
     * @return A {@link Flux} stream of {@link Snowflake} objects representing the IDs of the target users
     *         associated with this invite.
     */
    public final Flux<Snowflake> getTargetUserIds() {
        return this.getClient().getRestClient().getInviteService()
            .getTargetUsers(this.getCode())
            .flatMapMany(data -> Flux.fromArray(data.split(System.lineSeparator())))
            .map(String::trim)
            .skip(1) // the first element is the header of the csv response
            .map(Snowflake::of);
    }

    /**
     * Request the update of the target users associated with this invite.
     *
     * @param targetUserIds the target user ids
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the invite has been updated.
     */
    public final Mono<Void> updateTargetUserIds(List<Snowflake> targetUserIds) {
        final String dataTargetUsers = targetUserIds.stream().map(Snowflake::asString).collect(Collectors.joining(System.lineSeparator()));
        MultipartRequest<Void> inviteMultipartRequest = MultipartRequest.ofEmptyRequest("target_users_file");
        InviteCreateFields.File file = InviteCreateFields.File.of("target_users_file", new ByteArrayInputStream(dataTargetUsers.getBytes(StandardCharsets.UTF_8)));
        inviteMultipartRequest = inviteMultipartRequest.addFile(file.name(), file.inputStream());
        return this.getClient().getRestClient().getInviteService()
            .updateTargetUsers(this.getCode(), inviteMultipartRequest);
    }

    /**
     * Requests to retrieve the status of the job associated with the target users for this invite.
     *
     * @return A {@link Mono} where, upon successful completion, emits an {@link InviteTargetUsersJobStatus} object
     * representing the status of the job. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<InviteTargetUsersJobStatus> getTargetUsersJobStatus() {
        return this.getClient().getRestClient().getInviteService()
            .getTargetUsersJobStatus(this.getCode())
            .map(data -> new InviteTargetUsersJobStatus(this.getClient(), data));
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
        return this.getClient().getRestClient().getInviteService()
                .deleteInvite(getCode(), reason)
                .then();
    }

    /**
     * Gets the raw data as represented by Discord.
     *
     * @return The raw data as represented by Discord.
     */
    InviteData getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "Invite{" +
            "data=" + data +
            '}';
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

    /** Represents the guild invite flags for guild invites. */
    public enum Flag {
        /** Unknown type. */
        UNKNOWN(-1),

        /** this invite is a guest invite for a voice channel */
        IS_GUEST_INVITE(0);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * The flag value as represented by Discord.
         */
        private final int flag;

        /**
         * Constructs a {@code Invite.Flag}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Flag(final int value) {
            this.value = value;
            this.flag = 1 << value;
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
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord.
         */
        public int getFlag() {
            return flag;
        }

        /**
         * Gets the flags of invite. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<Invite.Flag> of(final int value) {
            final EnumSet<Invite.Flag> inviteFlags = EnumSet.noneOf(Invite.Flag.class);
            for (Invite.Flag flag : Invite.Flag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    inviteFlags.add(flag);
                }
            }
            return inviteFlags;
        }
    }
}
