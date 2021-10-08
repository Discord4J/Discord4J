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
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Dispatched when a new invite to a channel is created.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#invite-create">Invite Create</a>
 */
public class InviteCreateEvent extends Event {

    private final long guildId;
    private final long channelId;
    private final String code;
    @Nullable
    private final User inviter;
    private final Instant createdAt;
    private final int uses;
    private final int maxUses;
    private final int maxAge;
    private final boolean temporary;

    public InviteCreateEvent(GatewayDiscordClient client, ShardInfo shardInfo, long guildId, long channelId,
                             String code, @Nullable User inviter, Instant createdAt, int uses, int maxUses, int maxAge,
                             boolean temporary) {
        super(client, shardInfo);
        this.guildId = guildId;
        this.channelId = channelId;
        this.code = code;
        this.inviter = inviter;
        this.createdAt = createdAt;
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
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Guild} that had an invite created in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * Gets the {@link Snowflake} ID of the channel the invite is for.
     *
     * @return The ID of the channel involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Gets the invite code (unique ID).
     *
     * @return The invite code (unique ID).
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the user that created the invite, if present.
     *
     * @return The user that created the invite, if present.
     */
    public Optional<User> getInviter() {
        return Optional.ofNullable(inviter);
    }

    /**
     * Gets the number of times this invite has been used (always will be 0).
     *
     * @return The number of times this invite has been used (always will be 0).
     */
    public int getUses() {
        return uses;
    }

    /**
     * Gets the max number of times this invite can be used.
     *
     * @return The max number of times this invite can be used.
     */
    public int getMaxUses() {
        return maxUses;
    }

    /**
     * Gets how long the invite is valid for (in seconds).
     *
     * @return How long the invite is valid for (in seconds).
     */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Gets whether or not the invite is temporary (invited users will be kicked on disconnect unless they're assigned a role).
     *
     * @return Whether or not the invite is temporary.
     */
    public boolean isTemporary() {
        return temporary;
    }

    /**
     * Gets when this invite was created.
     *
     * @return When this invite was created.
     */
    public Instant getCreation() {
        return createdAt;
    }

    /**
     * Gets the instant this invite expires, if possible.
     *
     * @return The instant this invite expires, if possible.
     */
    public Optional<Instant> getExpiration() {
        final boolean temporary = isTemporary();
        final int maxAge = getMaxAge();

        return temporary ? Optional.of(getCreation().plus(maxAge, ChronoUnit.SECONDS)) : Optional.empty();
    }

    /**
     * Requests to retrieve the invite Created.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Invite}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Invite> getInvite() {
        return getClient().getInvite(code);
    }

    @Override
    public String toString() {
        return "InviteCreateEvent{" +
            "code='" + code + '\'' +
            ", guildId=" + guildId +
            ", channelId=" + channelId +
            ", inviter=" + inviter +
            ", uses=" + uses +
            ", maxUses=" + maxUses +
            ", maxAge=" + maxAge +
            ", temporary=" + temporary +
            ", createdAt='" + createdAt + '\'' +
            '}';
    }
}
