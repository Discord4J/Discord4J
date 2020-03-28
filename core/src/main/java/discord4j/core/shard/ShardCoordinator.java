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

import discord4j.gateway.limiter.PayloadTransformer;
import discord4j.gateway.SessionInfo;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * A {@link ShardCoordinator} defines key operations to leverage shard coordination across boundaries.
 */
public interface ShardCoordinator {

    /**
     * Returns a transformation function for a sequence of payloads that can be held or delayed in order to successfully
     * identify multiple shards in a coordinated manner.
     *
     * @param shardInfo the shard from where to retrieve the limiter
     * @param shardingFactor the number of shards that can be concurrently identified
     * @return a {@link PayloadTransformer} allowing IDENTIFY payload coordination across shards
     */
    PayloadTransformer getIdentifyLimiter(ShardInfo shardInfo, int shardingFactor);

    /**
     * Notifies this coordinator that a given shard has connected successfully. Can be used to signal other shards
     * for authentication.
     *
     * @param shardInfo the connected shard details
     */
    Mono<Void> publishConnected(ShardInfo shardInfo);

    /**
     * Notifies this coordinator that a given shard has disconnected.
     *
     * @param shardInfo the disconnected shard details
     * @param sessionInfo the disconnected shard session details to resume, or {@code null} if resume is not
     * available.
     */
    Mono<Void> publishDisconnected(ShardInfo shardInfo, @Nullable SessionInfo sessionInfo);
}
