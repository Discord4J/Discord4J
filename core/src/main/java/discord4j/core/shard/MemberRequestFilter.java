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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.shard;

import discord4j.discordjson.json.GuildCreateData;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * A filter to customize the guild member request strategy. Applied on each GUILD_CREATE returning a potentially
 * delayed {@link Mono} that, if containing {@code true}, guild members should be requested.
 * <p>
 * The following built-in factories exist:
 * <ul>
 *     <li>{@link #all()} to use a filter requesting ALL members on startup</li>
 *     <li>{@link #none()} to disable this feature</li>
 *     <li>{@link #withLargeGuilds()} to only request from large guilds (default)</li>
 *     <li>{@link #withGuilds(Snowflake...)} to request from specific guilds</li>
 * </ul>
 */
@FunctionalInterface
public interface MemberRequestFilter {

    /**
     * Request members from all large guilds.
     */
    MemberRequestFilter DEFAULT = withLargeGuilds();

    /**
     * Request members from all guilds.
     *
     * @return a {@link MemberRequestFilter} requesting members from all guilds
     */
    static MemberRequestFilter all() {
        return data -> Mono.just(true);
    }

    /**
     * Do not request guild members.
     *
     * @return a {@link MemberRequestFilter} not requesting any member
     */
    static MemberRequestFilter none() {
        return data -> Mono.just(false);
    }

    /**
     * Request members from large guilds.
     *
     * @return a {@link MemberRequestFilter} requesting members from large guilds
     */
    static MemberRequestFilter withLargeGuilds() {
        return data -> Mono.just(data.large());
    }

    /**
     * Request guild members for the given guild {@link Snowflake} IDs.
     *
     * @return a {@link MemberRequestFilter} requesting members from the given guilds
     */
    static MemberRequestFilter withGuilds(Snowflake... guildIds) {
        return data -> Flux.fromArray(guildIds).hasElement(Snowflake.of(data.id()));
    }

    /**
     * Obtain a {@link Mono} of {@link Boolean} for the given {@link GuildCreateData}. If the resulting sequence
     * contains {@code true}, then members will be requested through the Gateway for this guild.
     *
     * @param guildCreateData the guild triggering this filter
     * @return a {@link Mono} indicating if a guild should have their members requested
     */
    Mono<Boolean> apply(GuildCreateData guildCreateData);

    /**
     * Transform this current {@link MemberRequestFilter} by applying the given {@link Function} to derive a new
     * {@link Mono} of {@code boolean}.
     *
     * @param transformer the function to transform this {@link MemberRequestFilter}
     * @return a transformed {@link MemberRequestFilter}
     */
    default MemberRequestFilter as(Function<Mono<Boolean>, Mono<Boolean>> transformer) {
        return data -> apply(data).as(transformer);
    }
}
