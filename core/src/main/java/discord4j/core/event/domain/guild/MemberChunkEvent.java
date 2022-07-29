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
package discord4j.core.event.domain.guild;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.core.shard.MemberRequestFilter;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Dispatched as members are streamed to the client from Discord.
 * <p>
 * By default, all members in large connected guilds are requested on startup, but this behavior can be configured using
 * {@link GatewayBootstrap#setMemberRequestFilter(MemberRequestFilter)} and ultimately depending on your {@link Intent}
 * configuration at {@link GatewayBootstrap#setEnabledIntents(IntentSet)}.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-members-chunk">Guild Members Chunk</a>
 */
public class MemberChunkEvent extends GuildEvent {

    private final long guildId;
    private final Set<Member> members;
    private final int chunkIndex;
    private final int chunkCount;
    private final /*~~>*/List<Snowflake> notFound;
    @Nullable
    private final String nonce;

    public MemberChunkEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId, Set<Member> members,
                            int chunkIndex, int chunkCount, /*~~>*/List<Snowflake> notFound, @Nullable String nonce) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.members = members;
        this.chunkIndex = chunkIndex;
        this.chunkCount = chunkCount;
        this.notFound = notFound;
        this.nonce = nonce;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in this event.
     *
     * @return The ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} involved in the event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets a list of {@code Members} that have been streamed to the client in this event.
     * This may not contain all {@code Members} of the {@link Guild}.
     *
     * @return The list of {@code Members} streamed to the client in this event.
     */
    public Set<Member> getMembers() {
        return members;
    }

    /**
     * Gets the chunk index in the expected chunks for this response (0 &lt;= chunk_index &lt; chunk_count).
     *
     * @return The chunk index in the expected chunks for this response (0 &lt;= chunk_index &lt; chunk_count).
     */
    public int getChunkIndex() {
        return chunkIndex;
    }

    /**
     * Gets the total number of expected chunks for this response.
     *
     * @return The total number of expected chunks for this response.
     */
    public int getChunkCount() {
        return chunkCount;
    }

    /**
     * Gets invalid id passed to `REQUEST_GUILD_MEMBERS`, if any.
     *
     * @return Gets invalid id passed to `REQUEST_GUILD_MEMBERS`, if any.
     */
    public /*~~>*/List<Snowflake> getNotFound() {
        return notFound;
    }

    /**
     * Gets the nonce used in the Guild Members Request, if present.
     *
     * @return The nonce used in the Guild Members Request, if present.
     */
    public Optional<String> getNonce() {
        return Optional.ofNullable(nonce);
    }

    @Override
    public String toString() {
        return "MemberChunkEvent{" +
                "guildId=" + guildId +
                ", members=" + members +
                ", chunkIndex=" + chunkIndex +
                ", chunkCount=" + chunkCount +
                ", notFound=" + notFound +
                ", nonce='" + nonce + '\'' +
                '}';
    }
}
