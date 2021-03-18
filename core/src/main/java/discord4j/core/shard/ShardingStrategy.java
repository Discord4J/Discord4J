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

import discord4j.discordjson.json.GatewayData;
import discord4j.discordjson.json.SessionStartLimitData;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Strategy to build sharding {@link GatewayClient} instances.
 */
public interface ShardingStrategy {

    /**
     * Return the shard count used to create a group of sharded clients.
     *
     * @param restClient a handle to consume REST API resources, typically to retrieve the number of recommended shards
     * @return a shard count as a {@link Mono} to obtain this number asynchronously.
     */
    Mono<Integer> getShardCount(RestClient restClient);

    /**
     * Return the shard factory used to create a group of sharded clients.
     *
     * @param shardCount the total number of shards
     * @return a shard factory as a sequence of {@link ShardInfo} items.
     */
    default Flux<ShardInfo> getShards(int shardCount) {
        return Flux.range(0, shardCount).map(index -> ShardInfo.create(index, shardCount));
    }

    /**
     * Return the {@link GatewayClientGroupManager} to maintain each gateway client in the created group.
     *
     * @param shardCount the total number of shards
     * @return a {@link GatewayClientGroupManager} used by this strategy
     */
    GatewayClientGroupManager getGroupManager(int shardCount);

    /**
     * Return the number of shards that can be identified concurrently. Must be 1 unless your application is authorized
     * to use the large bot sharding system.
     *
     * @return a value determining the sharding factor this strategy has
     * @see <a href="https://discord.com/developers/docs/topics/gateway#sharding-for-very-large-bots">
     * Sharding for very large bots</a>
     * @deprecated use {@link #getMaxConcurrency(RestClient)} instead
     */
    @Deprecated
    int getMaxConcurrency();

    /**
     * Return the number of shards that can be identified concurrently. Must be 1 unless your application is authorized
     * to use the large bot sharding system.
     *
     * @param restClient a handle to consume REST API resources, typically to retrieve the recommended concurrency
     * @return a value determining the sharding factor this strategy has
     * @see <a href="https://discord.com/developers/docs/topics/gateway#sharding-for-very-large-bots">
     * Sharding for very large bots</a>
     */
    default Mono<Integer> getMaxConcurrency(RestClient restClient) {
        return Mono.just(getMaxConcurrency());
    }

    /**
     * Sharding strategy that retrieves the recommended shard count and concurrency, and creates as many
     * {@link GatewayClient} instances as indexes given by that count.
     *
     * @return a recommended {@link ShardingStrategy}
     */
    static ShardingStrategy recommended() {
        return new ShardingStrategy() {

            @Override
            public Mono<Integer> getShardCount(RestClient restClient) {
                return restClient.getGatewayService().getGatewayBot()
                        .map(GatewayData::shards)
                        .map(Possible::get);
            }

            @Override
            public GatewayClientGroupManager getGroupManager(int shardCount) {
                return new ShardingGatewayClientGroup(shardCount);
            }

            @Override
            public int getMaxConcurrency() {
                return 1;
            }

            @Override
            public Mono<Integer> getMaxConcurrency(RestClient restClient) {
                return restClient.getGatewayService().getGatewayBot()
                    .map(GatewayData::sessionStartLimit)
                    .map(sessionStartLimit -> sessionStartLimit.toOptional()
                        .map(SessionStartLimitData::maxConcurrency)
                        .flatMap(Possible::toOptional)
                        .orElseGet(this::getMaxConcurrency)
                    );

            }
        };
    }

    /**
     * Sharding strategy that creates a fixed number of {@link GatewayClient} instances, using the given {@code count}.
     *
     * @param count the number of {@link GatewayClient} instances to create, each representing a Discord shard
     * @return a fixed-count {@link ShardingStrategy}
     */
    static ShardingStrategy fixed(int count) {
        return new ShardingStrategy() {

            @Override
            public Mono<Integer> getShardCount(RestClient restClient) {
                return Mono.just(count);
            }

            @Override
            public GatewayClientGroupManager getGroupManager(int shardCount) {
                return new ShardingGatewayClientGroup(shardCount);
            }

            @Override
            public int getMaxConcurrency() {
                return 1;
            }
        };
    }

    /**
     * Sharding strategy that creates a single {@link GatewayClient}. Useful for basic bots or for advanced worker
     * {@link GatewayClient} that do not directly perform authentication to the Discord Gateway.
     *
     * @return a simple non-sharded {@link ShardingStrategy}
     */
    static ShardingStrategy single() {
        return new ShardingStrategy() {

            @Override
            public Mono<Integer> getShardCount(RestClient restClient) {
                return Mono.just(1);
            }

            @Override
            public GatewayClientGroupManager getGroupManager(int shardCount) {
                return new SingleGatewayClientGroup();
            }

            @Override
            public int getMaxConcurrency() {
                return 1;
            }
        };
    }

    /**
     * Return a builder to customize the {@link ShardingStrategy} using commonly used parameters.
     *
     * @return a {@link DefaultShardingStrategy.Builder} to create a custom {@link ShardingStrategy}
     */
    static DefaultShardingStrategy.Builder builder() {
        return new DefaultShardingStrategy.Builder();
    }

}
