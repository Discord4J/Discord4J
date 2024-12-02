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
package discord4j.core.object.entity.channel;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

/** A Discord channel associated to a {@link Guild}. */
public interface GuildChannel extends Channel {

    /**
     * Gets the ID of the guild this channel is associated to.
     *
     * @return The ID of the guild this channel is associated to.
     */
    default Snowflake getGuildId() {
        return getData().guildId().toOptional()
                .map(Snowflake::of)
                .orElseThrow(IllegalStateException::new); // TODO
    }

    /**
     * Requests to retrieve the guild this channel is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this channel is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Requests to retrieve the guild this channel is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this channel is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    /**
     * Gets the permissions for the given member, taking into account permission overwrites in this channel.
     *
     * @param memberId The ID of the member to get permissions for.
     * @return The permissions for the given member.
     */
    Mono<PermissionSet> getEffectivePermissions(Snowflake memberId);

    /**
     * Gets the permissions for the given member, taking into account permission overwrites in this channel.
     *
     * @param member The member to get permissions for.
     * @return The permissions for the given member.
     */
    Mono<PermissionSet> getEffectivePermissions(Member member);

    /**
     * Gets the name of the channel.
     *
     * @return The name of the channel.
     */
    default String getName() {
        return getData().name().toOptional()
                .orElseThrow(IllegalStateException::new);
    }
}
