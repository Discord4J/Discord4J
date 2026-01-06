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
package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Invite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

/**
 * Dispatched when a new invite to a channel is created.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#invite-create">Invite Create</a>
 */
public class InviteCreateEvent extends Event {

    private final @Nullable Long guildId;
    private final long channelId;
    private final String code;
    private final @Nullable User inviter;
    private final Instant createdAt;
    private final @Nullable Instant expiresAt;
    private final int uses;
    private final int maxUses;
    private final int maxAge;
    private final boolean temporary;

    public InviteCreateEvent(GatewayDiscordClient client, ShardInfo shardInfo, @Nullable Long guildId, long channelId,
                             String code, @Nullable User inviter, Instant createdAt, @Nullable Instant expiresAt, int uses, int maxUses, int maxAge,
                             boolean temporary) {
        super(client, shardInfo);
        this.guildId = guildId;
        this.channelId = channelId;
        this.code = code;
        this.inviter = inviter;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.uses = uses;
        this.maxAge = maxAge;
        this.maxUses = maxUses;
        this.temporary = temporary;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event, if present.
     *
     * @return The ID of the guild involved, if present.
     */
    public Optional<@Nullable Snowflake> getGuildId() {
        return Optional.ofNullable(this.guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Guild} that had an invite created in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(this.getGuildId()).flatMap(this.getClient()::getGuildById);
    }

    /**
     * Gets the {@link Snowflake} ID of the channel the invite is for.
     *
     * @return The ID of the channel involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(this.channelId);
    }

    /**
     * Gets the invite code (unique ID).
     *
     * @return The invite code (unique ID).
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Gets the user that created the invite, if present.
     *
     * @return The user that created the invite, if present.
     */
    public Optional<User> getInviter() {
        return Optional.ofNullable(this.inviter);
    }

    /**
     * Gets the number of times this invite has been used (always will be 0).
     *
     * @return The number of times this invite has been used (always will be 0).
     */
    public int getUses() {
        return this.uses;
    }

    /**
     * Gets the max number of times this invite can be used.
     *
     * @return The max number of times this invite can be used.
     */
    public int getMaxUses() {
        return this.maxUses;
    }

    /**
     * Gets how long the invite is valid for (in seconds).
     *
     * @return How long the invite is valid for (in seconds).
     */
    public int getMaxAge() {
        return this.maxAge;
    }

    /**
     * Gets whether this invite only grants temporary membership.
     *
     * @return {@code true} if this invite only grants temporary membership
     */
    public boolean isTemporary() {
        return this.temporary;
    }

    /**
     * Gets when this invite was created.
     *
     * @return When this invite was created.
     */
    public Instant getCreation() {
        return this.createdAt;
    }

    /**
     * Gets the instant this invite expires, if possible.
     *
     * @return The instant this invite expires, if possible.
     */
    public Optional<Instant> getExpiration() {
        return Optional.ofNullable(this.expiresAt);
    }

    /**
     * Requests to retrieve the invite Created.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Invite}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Invite> getInvite() {
        return getClient().getInvite(this.code);
    }

    @Override
    public String toString() {
        return "InviteCreateEvent{" +
            "code='" + this.code + '\'' +
            ", guildId=" + this.guildId +
            ", channelId=" + this.channelId +
            ", inviter=" + this.inviter +
            ", uses=" + this.uses +
            ", maxUses=" + this.maxUses +
            ", maxAge=" + this.maxAge +
            ", temporary=" + this.temporary +
            ", expiresAt=" + this.expiresAt +
            ", createdAt='" + this.createdAt + '\'' +
            '}';
    }
}
