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

package discord4j.core;

import discord4j.core.shard.ShardAwareStoreService;
import discord4j.core.shard.ShardingJdkStoreRegistry;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.retry.ReconnectOptions;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.service.StoreServiceLoader;
import discord4j.store.jdk.JdkStoreService;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A set of dependencies required to build and coordinate multiple {@link GatewayClient} instances.
 */
public class GatewayResources {

    private final StoreService storeService;
    private final ReconnectOptions reconnectOptions;
    private final ShardCoordinator shardCoordinator;
    private final Scheduler voiceConnectionScheduler;

    protected GatewayResources(Builder builder) {
        StoreService storeService = builder.storeService;

        if (storeService == null) {
            Map<Class<? extends StoreService>, Integer> priority = new HashMap<>();
            // We want almost minimum priority, so that jdk can beat no-op but most implementations will beat jdk
            priority.put(JdkStoreService.class, Integer.MAX_VALUE - 1);
            StoreServiceLoader storeServiceLoader = new StoreServiceLoader(priority);
            storeService = new ShardAwareStoreService(new ShardingJdkStoreRegistry(),
                    storeServiceLoader.getStoreService());
        }

        this.storeService = Objects.requireNonNull(storeService);
        this.reconnectOptions = Objects.requireNonNull(builder.reconnectOptions);
        this.shardCoordinator = Objects.requireNonNull(builder.shardCoordinator);
        this.voiceConnectionScheduler = Objects.requireNonNull(builder.voiceConnectionScheduler);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder mutate() {
        Builder builder = new Builder();

        builder.setStoreService(getStoreService())
                .setReconnectOptions(getReconnectOptions())
                .setShardCoordinator(getShardCoordinator())
                .setVoiceConnectionScheduler(getVoiceConnectionScheduler());

        return builder;
    }

    public static class Builder {

        private StoreService storeService;
        private ReconnectOptions reconnectOptions = ReconnectOptions.builder().build();
        private ShardCoordinator shardCoordinator = new LocalShardCoordinator();
        private Scheduler voiceConnectionScheduler = Schedulers.elastic();

        protected Builder() {
        }

        public Builder setStoreService(StoreService storeService) {
            this.storeService = storeService;
            return this;
        }

        public Builder setReconnectOptions(ReconnectOptions reconnectOptions) {
            this.reconnectOptions = reconnectOptions;
            return this;
        }

        public Builder setShardCoordinator(ShardCoordinator shardCoordinator) {
            this.shardCoordinator = shardCoordinator;
            return this;
        }

        public Builder setVoiceConnectionScheduler(Scheduler voiceConnectionScheduler) {
            this.voiceConnectionScheduler = voiceConnectionScheduler;
            return this;
        }

        public GatewayResources build() {
            return new GatewayResources(this);
        }
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public ReconnectOptions getReconnectOptions() {
        return reconnectOptions;
    }

    public ShardCoordinator getShardCoordinator() {
        return shardCoordinator;
    }

    public Scheduler getVoiceConnectionScheduler() {
        return voiceConnectionScheduler;
    }
}
