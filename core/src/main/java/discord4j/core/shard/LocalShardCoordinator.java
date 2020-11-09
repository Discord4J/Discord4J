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

import discord4j.gateway.SessionInfo;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.limiter.PayloadTransformer;
import discord4j.gateway.limiter.RateLimitTransformer;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static discord4j.common.LogUtil.format;

/**
 * A centralized local {@link ShardCoordinator} that can operate on a single JVM instance to coordinate Gateway
 * connection and identifying attempts across multiple shards.
 */
public class LocalShardCoordinator implements ShardCoordinator {

    private static final Logger log = Loggers.getLogger(LocalShardCoordinator.class);

    public static final Supplier<PayloadTransformer> DEFAULT_IDENTIFY_LIMITER_FACTORY =
            () -> new RateLimitTransformer(1, Duration.ofSeconds(6));

    private final Map<Integer, PayloadTransformer> limiters = new ConcurrentHashMap<>(1);
    private final Supplier<PayloadTransformer> identifyLimiterFactory;
    private final Set<Integer> shards = ConcurrentHashMap.newKeySet();

    private LocalShardCoordinator(Supplier<PayloadTransformer> identifyLimiterFactory) {
        this.identifyLimiterFactory = identifyLimiterFactory;
    }

    /**
     * Create a new {@link LocalShardCoordinator} that is able to locally coordinate multiple shards under a single
     * JVM instance.
     */
    public static LocalShardCoordinator create() {
        return new LocalShardCoordinator(DEFAULT_IDENTIFY_LIMITER_FACTORY);
    }

    /**
     * Create a new {@link LocalShardCoordinator} that is able to locally coordinate multiple shards under a single
     * JVM instance.
     *
     * @param identifyLimiterFactory a supplier of {@link PayloadTransformer} instances for limiting IDENTIFY access
     * across buckets.
     */
    public static LocalShardCoordinator create(Supplier<PayloadTransformer> identifyLimiterFactory) {
        return new LocalShardCoordinator(identifyLimiterFactory);
    }

    @Override
    public Mono<Void> publishConnected(ShardInfo shardInfo) {
        return Mono.deferContextual(ctx -> {
            boolean isNew = shards.add(shardInfo.getIndex());
            if (isNew) {
                log.info(format(ctx, "Shard connected"));
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> publishDisconnected(ShardInfo shardInfo, SessionInfo sessionInfo) {
        return Mono.deferContextual(ctx -> {
            boolean wasRemoved = shards.remove(shardInfo.getIndex());
            if (wasRemoved) {
                log.info(format(ctx, "Shard disconnected"));
            }
            return Mono.empty();
        });
    }

    @Override
    public PayloadTransformer getIdentifyLimiter(ShardInfo shardInfo, int maxConcurrency) {
        return limiters.computeIfAbsent(shardInfo.getIndex() % maxConcurrency, k -> identifyLimiterFactory.get());
    }

    @Override
    public Mono<Integer> getConnectedCount() {
        return Mono.fromCallable(shards::size);
    }
}
