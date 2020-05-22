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

package discord4j.gateway;

import discord4j.common.util.Snowflake;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.ShardGatewayPayload;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * An aggregation for arbitrary group of {@link GatewayClient} instances.
 */
public interface GatewayClientGroup {

    /**
     * Return a {@link GatewayClient} given by a {@code shardIndex}, if present.
     *
     * @param shardIndex a shard index to locate a particular client
     * @return a {@link GatewayClient} for a shard index, if present
     */
    Optional<GatewayClient> find(int shardIndex);

    /**
     * Return the current value of the {@code shardCount} parameter.
     *
     * @return the current shard count
     */
    int getShardCount();

    /**
     * Send a single {@link GatewayPayload} to all {@link GatewayClient} instances represented by this group and
     * returns a {@link Mono} that signals completion when it has been sent.
     *
     * @param payload a single outbound payload
     * @return a {@link Mono} completing when the payload is sent
     */
    Mono<Void> multicast(GatewayPayload<?> payload);

    /**
     * Send a single {@link ShardGatewayPayload} to a specific {@link GatewayClient} instance, given by the
     * routing information in {@link ShardGatewayPayload#getShardIndex()} and returns a {@link Mono} that
     * signals completion when it has been sent.
     *
     * @param payload a single outbound payload
     * @return a {@link Mono} completing when the payload is sent
     */
    Mono<Void> unicast(ShardGatewayPayload<?> payload);

    /**
     * Instructs that on subscription this group should log out from Discord Gateway.
     *
     * @return a {@link Mono} indicating completion when the logout has succeeded. If an error occurs it is forwarded
     * through this {@link Mono}.
     */
    Mono<Void> logout();

    /**
     * Return the shard index according to the shard count given by this {@link GatewayClientGroup}.
     * @param guildId the input guild ID to compute the shard index
     * @return the shard index for a given guild ID
     */
    default int computeShardIndex(Snowflake guildId) {
        return (int) ((guildId.asLong() >> 22) % getShardCount());
    }

}
