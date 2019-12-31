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

import discord4j.gateway.GatewayClient;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import discord4j.rest.json.response.GatewayResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultShardingStrategy implements ShardingStrategy {

    private final int count;
    private final Function<Integer, Publisher<Integer>> indexSource;
    private final Predicate<ShardInfo> filter;
    private final int factor;

    public DefaultShardingStrategy(Builder builder) {
        this.count = builder.shardCount;
        this.indexSource = builder.shardIndexSource;
        this.filter = builder.shardFilter;
        this.factor = builder.factor;
    }

    @Override
    public Flux<ShardInfo> getShards(RestClient restClient) {
        return computeShardCount(restClient)
                .flatMapMany(count -> Flux.from(indexSource.apply(count))
                        .filter(index -> index >= 0 && index < count) // sanitize
                        .map(index -> new ShardInfo(index, count))
                        .filter(filter));
    }

    private Mono<Integer> computeShardCount(RestClient restClient) {
        if (count > 0) {
            return Mono.just(count);
        } else if (count == 0) {
            return restClient.getGatewayService().getGatewayBot()
                    .map(GatewayResponse::getShards);
        }
        return Mono.error(new RuntimeException("Invalid shard count: " + count));
    }

    @Override
    public GatewayClientGroupManager getGroupManager() {
        return new ShardingGatewayClientGroup();
    }

    @Override
    public int getShardingFactor() {
        return factor;
    }

    /**
     * A {@link ShardingStrategy} builder.
     */
    public static class Builder {

        /**
         * Value representing the use of a recommended amount of shards.
         */
        static int RECOMMENDED_SHARD_COUNT = 0;

        private int shardCount = RECOMMENDED_SHARD_COUNT;
        private Function<Integer, Publisher<Integer>> shardIndexSource = count -> Flux.range(0, count);
        private Predicate<ShardInfo> shardFilter = shard -> true;
        private int factor = 1;

        /**
         * Set the shard count parameter. Defaults to {@link #RECOMMENDED_SHARD_COUNT}. Must not be negative.
         *
         * @param shardCount the shard count sent to Discord while identifying each {@link GatewayClient}
         * @return this builder
         */
        public Builder count(int shardCount) {
            if (shardCount < 0) {
                throw new IllegalArgumentException("shardCount < 0");
            }
            this.shardCount = shardCount;
            return this;
        }

        /**
         * Set the list of shard indexes to identify to Discord Gateway. Defaults to identifying all shards. Any
         * invocation of this method will also replace the previously set value at {@link #indexes(Function)}.
         *
         * @param shardIndexes the list of shard indexes to identify
         * @return this builder
         */
        public Builder indexes(int... shardIndexes) {
            this.shardIndexSource = count -> Flux.fromStream(Arrays.stream(shardIndexes).boxed());
            return this;
        }

        /**
         * Set a generator function to derive a {@link Publisher} of shard indexes to identify to Discord Gateway.
         * Defaults to identify all shards. Any invocation of this method will also replace the previously set value
         * at {@link #indexes(int...)}.
         *
         * @param shardIndexSource the generator function to determine the shards to identify
         * @return this builder
         */
        public Builder indexes(Function<Integer, Publisher<Integer>> shardIndexSource) {
            this.shardIndexSource = Objects.requireNonNull(shardIndexSource);
            return this;
        }

        /**
         * Set a filter to determine which shards should be accepted for identifying to Discord Gateway. Defaults to
         * connecting to all shards given by shard count.
         *
         * @param shardFilter a {@link Predicate} for {@link ShardInfo} objects. Called for each shard determined by
         * {@link #count(int)} and schedules it for connection if returning {@code true}.
         * @return this builder
         */
        public Builder filter(Predicate<ShardInfo> shardFilter) {
            this.shardFilter = Objects.requireNonNull(shardFilter);
            return this;
        }

        /**
         * Set the sharding factor to use when identifying to the Discord Gateway, determining the amount of shards that
         * will be concurrently identified. Defaults to 1. You should only change this value if your bot is
         * authorized to use the very large bot sharding system, otherwise you will hit a rate limit on identifying.
         *
         * @param factor a positive number indicating the amount of shards that can be identified concurrently.
         * @return this builder
         */
        public Builder factor(int factor) {
            if (factor < 1) {
                throw new IllegalArgumentException("factor < 1");
            }
            this.factor = factor;
            return this;
        }

        /**
         * Create the {@link ShardingStrategy}.
         *
         * @return a custom {@link ShardingStrategy}
         */
        public ShardingStrategy build() {
            return new DefaultShardingStrategy(this);
        }
    }
}


