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

import com.darichey.discordjson.json.GatewayData;
import com.darichey.discordjson.possible.Possible;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import reactor.core.publisher.Flux;

/**
 * Strategy to build sharding {@link GatewayClient} instances.
 */
public interface ShardingStrategy {

    /**
     * Return the shard factory used to create a group of sharded clients.
     *
     * @param restClient a handle to consume REST API resources, typically to retrieve the number of recommended shards
     * @return a shard factory as a sequence of {@link ShardInfo} items.
     */
    Flux<ShardInfo> getShards(RestClient restClient);

    /**
     * Return the {@link GatewayClientGroupManager} to maintain each gateway client in the created group.
     *
     * @return a {@link GatewayClientGroupManager} used by this strategy
     */
    GatewayClientGroupManager getGroupManager();

    /**
     * Return the number of shards that can be identified concurrently. Must be 1 unless your application is authorized
     * to use the large bot sharding system.
     *
     * @return a value determining the sharding factor this strategy has
     * @see <a href="https://discordapp.com/developers/docs/topics/gateway#sharding-for-very-large-bots">
     * Sharding for very large bots</a>
     */
    int getShardingFactor();

    /**
     * Sharding strategy that retrieves the recommended shard count and creates as many {@link GatewayClient}
     * instances as indexes given by that count.
     *
     * @return a recommended {@link ShardingStrategy}
     */
    static ShardingStrategy recommended() {
        return new ShardingStrategy() {
            @Override
            public Flux<ShardInfo> getShards(RestClient restClient) {
                return restClient.getGatewayService().getGatewayBot()
                    .map(GatewayData::shards)
                    .map(Possible::get)
                    .flatMapMany(count -> Flux.range(0, count)
                        .map(index -> new ShardInfo(index, count)));
            }

            @Override
            public GatewayClientGroupManager getGroupManager() {
                return new ShardingGatewayClientGroup();
            }

            @Override
            public int getShardingFactor() {
                return 1;
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
            public Flux<ShardInfo> getShards(RestClient restClient) {
                return Flux.range(0, count).map(index -> new ShardInfo(index, count));
            }

            @Override
            public GatewayClientGroupManager getGroupManager() {
                return new ShardingGatewayClientGroup();
            }

            @Override
            public int getShardingFactor() {
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
            public Flux<ShardInfo> getShards(RestClient restClient) {
                return Flux.just(new ShardInfo(0, 1));
            }

            @Override
            public GatewayClientGroupManager getGroupManager() {
                return new SingleGatewayClientGroup();
            }

            @Override
            public int getShardingFactor() {
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
