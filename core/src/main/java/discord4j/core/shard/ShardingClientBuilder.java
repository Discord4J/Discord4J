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

import discord4j.common.JacksonResourceProvider;
import discord4j.common.SimpleBucket;
import discord4j.core.DiscordClientBuilder;
import discord4j.gateway.GatewayObserver;
import discord4j.gateway.RateLimiterTransformer;
import discord4j.rest.RestClient;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.json.response.GatewayResponse;
import discord4j.rest.request.*;
import discord4j.rest.response.ResponseFunction;
import discord4j.store.api.Store;
import discord4j.store.api.service.StoreService;
import discord4j.store.jdk.JdkStoreService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClient;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Builder suited for creating a sequence of {@link discord4j.core.DiscordClient} each dedicated to a shard, while
 * allowing resource sharing and login coordination.
 * <p>
 * Creates multiple Discord clients with many configurable options. It uses a recommended amount of shards but can be
 * overridden through {@link #setShardCount(Integer)}.
 */
public class ShardingClientBuilder {

    private static final Logger log = Loggers.getLogger(ShardingClientBuilder.class);

    private String token;

    @Nullable
    private Integer shardCount;

    @Nullable
    private RouterFactory routerFactory;

    @Nullable
    private RouterOptions routerOptions;

    @Nullable
    private ShardingStoreRegistry shardingStoreRegistry;

    @Nullable
    private Predicate<Integer> shardIndexFilter;

    @Nullable
    private StoreService storeService;

    /**
     * Initialize a new builder with the given token.
     *
     * @param token the bot token used to authenticate to Discord
     */
    public ShardingClientBuilder(final String token) {
        this.token = Objects.requireNonNull(token);
    }

    /**
     * Retrieve the token set in this builder.
     *
     * @return the current bot token
     */
    @Deprecated
    public String getToken() {
        return token;
    }

    /**
     * Change the token stored in this builder.
     *
     * @param token the new bot token
     * @return this builder
     */
    public ShardingClientBuilder setToken(final String token) {
        this.token = Objects.requireNonNull(token);
        return this;
    }

    /**
     * Returns the shard count value to set while identifying each produced shard.
     *
     * @return the shard count, can be <code>null</code>, in which case use the recommended shard count given by the
     * {@link discord4j.rest.route.Routes#GATEWAY_BOT_GET} endpoint.
     */
    @Nullable
    @Deprecated
    public Integer getShardCount() {
        return shardCount;
    }

    /**
     * Change the shard count value given to each resulting shard. Can be set to <code>null</code> to use the
     * recommended value from the {@link discord4j.rest.route.Routes#GATEWAY_BOT_GET} endpoint, that is calculated from
     * the number of guilds this token is in.
     *
     * @param shardCount the shard count, can be <code>null</code>, in which case use a recommended value
     * @return this builder
     */
    public ShardingClientBuilder setShardCount(@Nullable Integer shardCount) {
        this.shardCount = shardCount;
        return this;
    }

    /**
     * Get the current {@link discord4j.rest.request.RouterFactory} used to create a
     * {@link discord4j.rest.request.Router} that executes Discord REST API requests.
     *
     * @return the current RouterFactory used to create a Router that perform API requests
     */
    @Nullable
    @Deprecated
    public RouterFactory getRouterFactory() {
        return routerFactory;
    }

    /**
     * Set a new {@link RouterFactory} used to create a {@link Router} that executes Discord REST API requests and
     * will be shared across all sharding clients created by this builder.
     * <p>
     * Make sure the given factory is capable of coordinating rate limits across shards, otherwise use the
     * default value as it is shard aware.
     *
     * @param routerFactory a new RouterFactory to create a Router that performs API requests. Pass {@code null} to
     * use the default value
     * @return this builder
     */
    public ShardingClientBuilder setRouterFactory(@Nullable RouterFactory routerFactory) {
        this.routerFactory = routerFactory;
        return this;
    }

    /**
     * Return the current {@link RouterOptions} used to configure {@link RouterFactory} instances.
     *
     * @return the current {@code RouterOptions} used by this client
     */
    @Nullable
    @Deprecated
    public RouterOptions getRouterOptions() {
        return routerOptions;
    }

    /**
     * Sets a new {@link RouterOptions} used to configure a {@link RouterFactory}.
     * <p>
     * {@code RouterOptions} instances provide a way to override the {@link Scheduler} used for retrieving API responses
     * and scheduling rate limiting actions. It also allows changing the behavior associated with API errors through
     * {@link RouterOptions.Builder#onClientResponse(ResponseFunction)}.
     * <p>
     * If you use a default {@code RouterFactory}, it will use the supplied {@code RouterOptions} to configure itself
     * while building this client.
     *
     * @param routerOptions a new {@code RouterOptions} to configure a {@code RouterFactory}
     * @return this builder
     */
    public ShardingClientBuilder setRouterOptions(@Nullable RouterOptions routerOptions) {
        this.routerOptions = routerOptions;
        return this;
    }

    /**
     * Returns the current {@link ShardingStoreRegistry} used to coordinate stores across shards.
     *
     * @return the store registry
     */
    @Nullable
    @Deprecated
    public ShardingStoreRegistry getShardingStoreRegistry() {
        return shardingStoreRegistry;
    }

    /**
     * Set a new {@link ShardingStoreRegistry} used to coordinate stores across shards. The default value is to use
     * {@link ShardingJdkStoreRegistry}. If you wish to disable this feature, set the resulting client stores to any
     * non-coordinating value through
     * {@link DiscordClientBuilder#setStoreService(discord4j.store.api.service.StoreService)} in order to
     * override the default value that attempts to communicate with this registry.
     *
     * @param shardingStoreRegistry a new store registry to coordinate shards. Can be <code>null</code> to use the
     * default value
     * @return this builder
     */
    public ShardingClientBuilder setShardingStoreRegistry(@Nullable ShardingStoreRegistry shardingStoreRegistry) {
        this.shardingStoreRegistry = shardingStoreRegistry;
        return this;
    }

    /**
     * Returns the current {@link Predicate} used to include only a set of shards in the creation process. By default
     * all shards given by the shard count are created.
     *
     * @return the current predicate of shard index indicating which shards should be created, can be <code>null</code>
     */
    @Nullable
    @Deprecated
    public Predicate<Integer> getShardIndexFilter() {
        return shardIndexFilter;
    }

    /**
     * Set a new {@link Predicate} of shard index, indicating whether a shard should be created. Can be used in
     * scenarios when you want to partially colocate the shards given by shard count.
     *
     * @param shardIndexFilter the new predicate of shard index to use in order to filter shard creation
     * @return this builder
     */
    public ShardingClientBuilder setShardIndexFilter(@Nullable Predicate<Integer> shardIndexFilter) {
        this.shardIndexFilter = shardIndexFilter;
        return this;
    }

    /**
     * Set a new {@link StoreService} that will create a {@link Store} for each shard. The resulting stores will be
     * automatically coordinated across shards using the registry defined via
     * {@link #setShardingStoreRegistry(ShardingStoreRegistry)} (or a {@link ShardingJdkStoreRegistry} by default). If
     * you wish to use non-coordinated stores, set them via {@link DiscordClientBuilder#setStoreService(StoreService)}
     * on each client separately.
     *
     * @param storeService the {@link StoreService} that will create stores for all shards
     * @return this builder
     */
    public ShardingClientBuilder setStoreService(@Nullable StoreService storeService) {
        this.storeService = storeService;
        return this;
    }

    private RouterFactory initRouterFactory() {
        if (routerFactory != null) {
            return routerFactory;
        }
        return new DefaultRouterFactory();
    }

    private Router initRouter(RouterFactory factory, DiscordWebClient webClient) {
        if (routerOptions != null) {
            return factory.getRouter(webClient, routerOptions);
        }
        return factory.getRouter(webClient);
    }

    private Mono<Integer> initShardCount(RestClient restClient) {
        if (shardCount == null) {
            return restClient.getGatewayService().getGatewayBot()
                    .map(GatewayResponse::getShards);
        }
        if (shardCount <= 0) {
            throw new IllegalArgumentException("Invalid shard count");
        }
        return Mono.just(shardCount);
    }

    private ShardingStoreRegistry initStoreRegistry() {
        if (shardingStoreRegistry != null) {
            return shardingStoreRegistry;
        }
        return new ShardingJdkStoreRegistry();
    }

    private Predicate<Integer> initShardIndexFilter() {
        if (shardIndexFilter != null) {
            return shardIndexFilter;
        }
        return index -> true;
    }

    private StoreService initStoreService() {
        if (storeService != null) {
            return storeService;
        }
        return new JdkStoreService();
    }

    /**
     * Create a sequence of {@link DiscordClientBuilder}s each representing a shard, up to the resulting shard count,
     * filtering out values not matching the predicate given by {@link #getShardIndexFilter()}.
     * <p>
     * This sequence can be further customized on a per-shard basis until you call
     * {@link discord4j.core.DiscordClient#login()}:
     * <pre>
     * new ShardingClientBuilder(token)
     *     .build()
     * 	   .map(builder -&gt; builder.setInitialPresence(
     * 		    Presence.online(Activity.playing("Shard " + builder.getShardIndex()))))
     *     .map(DiscordClientBuilder::build)
     *     .flatMap(DiscordClient::login)
     *     .blockLast();
     * </pre>
     * <p>
     * Login will be coordinated across this sequence, that is, a shard will identify to the gateway only when the
     * previous one has connected successfully and the mandatory time between login attempt has passed (~5 seconds).
     *
     * @return a {@link discord4j.core.DiscordClient} based on this builder parameters
     */
    public Flux<DiscordClientBuilder> build() {
        final JacksonResourceProvider jackson = new JacksonResourceProvider();
        final DiscordWebClient webClient = new DiscordWebClient(HttpClient.create().compress(true),
                ExchangeStrategies.jackson(jackson.getObjectMapper()), token);
        final RouterFactory routerFactory = initRouterFactory();
        final Router router = initRouter(routerFactory, webClient);
        final RestClient restClient = new RestClient(router);

        final ShardingStoreRegistry storeRegistry = initStoreRegistry();
        final StoreService storeService = initStoreService();
        final ReplayProcessor<Integer> permits = ReplayProcessor.create();
        final FluxSink<Integer> permitSink = permits.sink();
        permitSink.next(0);

        final DiscordClientBuilder builder = new DiscordClientBuilder(token)
                .setJacksonResourceProvider(jackson)
                .setRouterFactory(new SingleRouterFactory(router))
                .setIdentifyLimiter(new RateLimiterTransformer(new SimpleBucket(1, Duration.ofSeconds(6))))
                .setEventScheduler(ForkJoinPoolScheduler.create("discord4j-events"))
                .setGatewayObserver((s, o) -> {
                    if (s.equals(GatewayObserver.CONNECTED)) {
                        log.info("Shard {} connected", o.getShardIndex());
                        permitSink.next(o.getShardIndex() + 1);
                    }
                });

        return initShardCount(restClient)
                .flatMapMany(count -> Flux.range(0, count)
                        .filter(initShardIndexFilter())
                        .zipWith(permits)
                        .map(Tuple2::getT1)
                        .map(index -> builder.setStoreService(new ShardAwareStoreService(storeRegistry, storeService))
                                .setShardIndex(index)
                                .setShardCount(count)));
    }
}
