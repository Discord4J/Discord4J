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
import discord4j.core.object.data.InviteBean;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.CategorizableChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A Discord invite.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/invite">Invite Resource</a>
 */
public class Invite implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final InviteBean data;

    /**
     * Constructs a {@code Invite} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Invite(final GatewayDiscordClient gateway, final InviteBean data) {
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
        return data.getCode();
    }

    /**
     * Gets the ID of the guild this invite is associated to.
     *
     * @return The ID of the guild this invite is associated to.
     */
    public final Snowflake getGuildId() {
        return Snowflake.of(data.getGuildId());
    }

    /**
     * Requests to retrieve the guild this invite is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this invite is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Guild> getGuild() {
        return gateway.getGuildById(getGuildId());
    }

    /**
     * Gets the ID of the channel this invite is associated to.
     *
     * @return The ID of the channel this invite is associated to.
     */
    public final Snowflake getChannelId() {
        return Snowflake.of(data.getChannelId());
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
     * Gets the ID of the user who created the invite, if present.
     *
     * @return The ID of the user who created the invite, if present.
     */
    public final Optional<Snowflake> getInviterId() {
        return Optional.ofNullable(getData().getInviterId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the user who created the invite.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User user} who created the invite. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<User> getInviter() {
        return getInviterId().map(getClient()::getUserById).orElse(Mono.empty());
    }

    /**
     * Gets the ID of the target user this invite is associated to, if present.
     *
     * @return The ID of the target user this invite is associated to, if present.
     */
    public final Optional<Snowflake> getTargetUserId() {
        return Optional.ofNullable(data.getTargetUserId())
            .map(Snowflake::of);
    }

    /**
     * Requests to retrieve the target user this invite is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User target user} this invite is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<User> getTargetUser() {
        return getTargetUserId().map(gateway::getUserById).orElse(Mono.empty());
    }

    /**
     * Gets an approximate count of online members (only present when the target user is set) of the guild this invite
     * is associated to, if present.
     *
     * @return An approximate count of online members (only present when the target user is set) of the guild this
     * invite is associated to, if present.
     */
    public final OptionalInt getApproximatePresenceCount() {
        return Optional.ofNullable(data.getApproximatePresenceCount())
            .map(OptionalInt::of)
            .orElse(OptionalInt.empty());
    }

    /**
     * Gets approximate count of total members of the guild this invite is associated to, if present.
     *
     * @return An approximate count of total members of the guild this invite is associated to, if present.
     */
    public final OptionalInt getApproximateMemberCount() {
        return Optional.ofNullable(data.getApproximateMemberCount())
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
    InviteBean getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Invite{" +
                "data=" + data +
                '}';
    }
}
