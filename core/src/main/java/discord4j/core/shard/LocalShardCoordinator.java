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

import discord4j.gateway.PayloadTransformer;
import discord4j.gateway.PoolingTransformer;
import discord4j.gateway.SessionInfo;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A centralized local {@link ShardCoordinator} that can operate on a single JVM instance to coordinate Gateway
 * connection and identifying attempts across multiple shards.
 */
public class LocalShardCoordinator implements ShardCoordinator {

    private final Map<Integer, PayloadTransformer> limiters = new ConcurrentHashMap<>(1);

    /**
     * Create a new {@link LocalShardCoordinator} that is able to locally coordinate multiple shards under a single
     * JVM instance.
     */
    public LocalShardCoordinator() {}

    @Override
    public Mono<Void> publishConnected(ShardInfo shardInfo) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> publishDisconnected(ShardInfo shardInfo, SessionInfo sessionInfo) {
        return Mono.empty();
    }

    @Override
    public PayloadTransformer getIdentifyLimiter(ShardInfo shardInfo, int shardingFactor) {
        return limiters.computeIfAbsent(shardInfo.getIndex() % shardingFactor,
                k -> new PoolingTransformer(1, Duration.ofSeconds(6)));
    }
}
