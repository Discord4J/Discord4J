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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.GitProperties;
import discord4j.common.JacksonResourceProvider;
import discord4j.common.SimpleBucket;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.dispatch.DispatchContext;
import discord4j.core.event.dispatch.DispatchHandlers;
import discord4j.core.event.domain.Event;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.ShardingClientBuilder;
import discord4j.gateway.*;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.VoiceStateUpdate;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import discord4j.rest.RestClient;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.request.DefaultRouterFactory;
import discord4j.rest.request.Router;
import discord4j.rest.request.RouterFactory;
import discord4j.rest.request.RouterOptions;
import discord4j.rest.response.ResponseFunction;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.service.StoreServiceLoader;
import discord4j.store.api.util.StoreContext;
import discord4j.store.jdk.JdkStoreService;
import discord4j.voice.VoiceClient;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Builder suited for creating a {@link DiscordClient}.
 * <p>
 * Allows creating a Discord client for a single shard with many configurable options. You can reuse this builder for
 * sharding as long as you set the {@link #setShardIndex(Integer)} and {@link #setShardCount(Integer)}
 * options, or {@link #setIdentifyOptions(IdentifyOptions)}.
 */
public final class DiscordClientBuilder {

    private static final Logger log = Loggers.getLogger(DiscordClientBuilder.class);

    private String token;

    @Nullable
    private Integer shardIndex;

    @Nullable
    private Integer shardCount;

    @Nullable
    private StoreService storeService;

    @Nullable
    private FluxProcessor<Event, Event> eventProcessor;

    @Nullable
    private Scheduler eventScheduler;

    @Nullable
    private JacksonResourceProvider jacksonResourceProvider;

    @Nullable
    private RouterFactory routerFactory;

    @Nullable
    private RouterOptions routerOptions;

    @Nullable
    private GatewayClientFactory gatewayClientFactory;

    @Nullable
    private Presence initialPresence;

    @Nullable
    private IdentifyOptions identifyOptions;

    @Nullable
    private RetryOptions retryOptions;

    @Nullable
    private GatewayObserver gatewayObserver;

    @Nullable
    private PayloadTransformer identifyLimiter;

    private Scheduler voiceConnectionScheduler = Schedulers.elastic();

    /**
     * Initialize a new builder with the given token.
     *
     * @param token the bot token used to authenticate to Discord
     */
    public DiscordClientBuilder(final String token) {
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
    public DiscordClientBuilder setToken(final String token) {
        this.token = Objects.requireNonNull(token);
        return this;
    }

    /**
     * Retrieve the current shard index.
     *
     * @return the current shard index, can be {@code null}
     */
    @Nullable
    @Deprecated
    public Integer getShardIndex() {
        return shardIndex;
    }

    /**
     * Change the shard index. Can override shard index set in {@link #setIdentifyOptions(IdentifyOptions)}.
     * <p>
     * Validation is only performed during {@link #build()}. Make sure the following holds:
     * <blockquote><pre>0 &lt;= shardIndex &lt; shardCount</pre></blockquote>
     *
     * @param shardIndex the new shard index, can be set to {@code null} to use the value under
     * {@link #setIdentifyOptions(IdentifyOptions)} if set, or fallback to a default
     * @return this builder
     */
    public DiscordClientBuilder setShardIndex(@Nullable Integer shardIndex) {
        this.shardIndex = shardIndex;
        return this;
    }

    /**
     * Retrieve the current shard count, can be {@code null}
     *
     * @return the current shard count
     */
    @Nullable
    @Deprecated
    public Integer getShardCount() {
        return shardCount;
    }

    /**
     * Change the shard count.
     * <p>
     * Validation is only performed during {@link #build()}. Make sure the following holds:
     * <blockquote><pre>0 &lt;= shardIndex &lt; shardCount</pre></blockquote>
     *
     * @param shardCount the new shard count, can be set to {@code null} to use a default value
     * @return this builder
     */
    public DiscordClientBuilder setShardCount(@Nullable Integer shardCount) {
        this.shardCount = shardCount;
        return this;
    }

    /**
     * Get the current {@link StoreService} factory, used to create {@link discord4j.store.api.Store} instances on
     * login to cache entities.
     * <p>
     * Can be {@code null} if automatic discovery is to be used.
     *
     * @return the current StoreService factory, {@code null} if automatic discovery should be used
     */
    @Nullable
    @Deprecated
    public StoreService getStoreService() {
        return storeService;
    }

    /**
     * Set a new {@link StoreService} to this builder, used to create {@link discord4j.store.api.Store} instances on
     * login to cache entities.
     *
     * @param storeService a new StoreService factory, can be {@code null} to enable automatic discovery
     * @return this builder
     */
    public DiscordClientBuilder setStoreService(@Nullable StoreService storeService) {
        this.storeService = storeService;
        return this;
    }

    /**
     * Get the current {@link FluxProcessor} used to queue Discord events in conjunction with {@link EventDispatcher}.
     * Can be {@code null} if a default {@link EmitterProcessor} is used.
     *
     * @return a FluxProcessor dedicated to queue events, can be {@code null} when using a default value
     */
    @Nullable
    @Deprecated
    public FluxProcessor<Event, Event> getEventProcessor() {
        return eventProcessor;
    }

    /**
     * Set a new {@link FluxProcessor} used to queue Discord events in conjunction with {@link EventDispatcher}.
     *
     * @param eventProcessor a new FluxProcessor used in this builder. Can be left {@code null} if a default
     * {@link EmitterProcessor} is used
     * @return this builder
     */
    public DiscordClientBuilder setEventProcessor(@Nullable FluxProcessor<Event, Event> eventProcessor) {
        this.eventProcessor = eventProcessor;
        return this;
    }

    /**
     * Get the current {@link Scheduler} used to publish events through {@link EventDispatcher}.
     *
     * @return the current Scheduler for event dispatching, can be {@code null} when using a default value
     */
    @Nullable
    @Deprecated
    public Scheduler getEventScheduler() {
        return eventScheduler;
    }

    /**
     * Set a new {@link Scheduler} used to publish events through {@link EventDispatcher}.
     *
     * @param eventScheduler a new Scheduler for event dispatching. Can be {@code null} to use a default value
     * @return this builder
     */
    public DiscordClientBuilder setEventScheduler(@Nullable Scheduler eventScheduler) {
        this.eventScheduler = eventScheduler;
        return this;
    }

    /**
     * Get the current {@link JacksonResourceProvider}, providing an {@link ObjectMapper} for serialization and
     * deserialization of data.
     *
     * @return the current resource provider for object serialization and deserialization, can be {@code null} when
     * using a default value
     */
    @Nullable
    @Deprecated
    public JacksonResourceProvider getJacksonResourceProvider() {
        return jacksonResourceProvider;
    }

    /**
     * Set a new {@link JacksonResourceProvider} to this builder, dedicated to provide an {@link ObjectMapper} for
     * serialization and deserialization of data.
     *
     * @param jacksonResourceProvider the new resource provider for serialization and deserialization, use {@code null}
     * to use a default one
     * @return this builder
     */
    public DiscordClientBuilder setJacksonResourceProvider(@Nullable JacksonResourceProvider jacksonResourceProvider) {
        this.jacksonResourceProvider = jacksonResourceProvider;
        return this;
    }

    /**
     * Get the current {@link RouterFactory} used to create a {@link discord4j.rest.request.Router} that executes
     * Discord REST API requests.
     *
     * @return the current RouterFactory used to create a Router that perform API requests
     */
    @Nullable
    @Deprecated
    public RouterFactory getRouterFactory() {
        return routerFactory;
    }

    /**
     * Set a new {@link RouterFactory} used to create a {@link discord4j.rest.request.Router} that executes Discord
     * REST API requests.
     * <p>
     * The resulting client will utilize the produced Router for every request. When performing sharding operations, it
     * is expected that the underlying {@link discord4j.rest.request.Router} instance to be shared across shards in
     * order to properly coordinate rate-limits on global endpoints. For those cases, use
     * {@link discord4j.rest.request.SingleRouterFactory}.
     *
     * @param routerFactory a new RouterFactory to create a Router that performs API requests. Pass {@code null} to
     * use a default value
     * @return this builder
     */
    public DiscordClientBuilder setRouterFactory(@Nullable RouterFactory routerFactory) {
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
     * <p>
     * Calling this method while sharding clients will not apply the supplied options as requests on the coordinated
     * {@link Router} implementation have already begun. Use
     * {@link ShardingClientBuilder#setRouterOptions(RouterOptions)} instead.
     *
     * @param routerOptions a new {@code RouterOptions} to configure a {@code RouterFactory}
     * @return this builder
     */
    public DiscordClientBuilder setRouterOptions(@Nullable RouterOptions routerOptions) {
        this.routerOptions = routerOptions;
        return this;
    }

    /**
     * Get the current {@link GatewayClientFactory} to use with the resulting clients.
     *
     * @return the currently installed factory, or {@code null} if using a {@link DefaultGatewayClientFactory}
     */
    @Nullable
    @Deprecated
    public GatewayClientFactory getGatewayClientFactory() {
        return gatewayClientFactory;
    }

    /**
     * Set a new {@link GatewayClientFactory} to this builder, allowing you to configure the underlying
     * {@link GatewayClient} used when logging in with the resulting clients.
     *
     * @param gatewayClientFactory the new factory, or {@code null} to use a {@link DefaultGatewayClientFactory}
     * @return this builder
     */
    public DiscordClientBuilder setGatewayClientFactory(@Nullable GatewayClientFactory gatewayClientFactory) {
        this.gatewayClientFactory = gatewayClientFactory;
        return this;
    }

    /**
     * Get the current {@link Presence} object used when identifying to the Gateway.
     *
     * @return the current presence status used for login. Can be {@code null} to use the value under
     * {@link #setIdentifyOptions(IdentifyOptions)} if set, or fallback to a default
     */
    @Nullable
    @Deprecated
    public Presence getInitialPresence() {
        return initialPresence;
    }

    /**
     * Set a new {@link Presence} object used when identifying to the Gateway.
     *
     * @param initialPresence the new presence status used for login. Can be {@code null} to use the value under
     * {@link #setIdentifyOptions(IdentifyOptions)} if set, or fallback to a default
     * @return this builder
     */
    public DiscordClientBuilder setInitialPresence(@Nullable Presence initialPresence) {
        this.initialPresence = initialPresence;
        return this;
    }

    /**
     * Get the current {@link IdentifyOptions} set in this builder. IdentifyOptions is a container
     * object targeted to group all parameters applied on bot login, like shard information, initial status and even
     * session identifier and sequence required to resume a session.
     * <p>
     * Options under {@link #getShardIndex()}, {@link #getShardCount()} and {@link #getInitialPresence()} override the
     * values in this object.
     *
     * @return the current set of bot identification options
     */
    @Nullable
    @Deprecated
    public IdentifyOptions getIdentifyOptions() {
        return identifyOptions;
    }

    /**
     * Set a new {@link IdentifyOptions} to this builder. IdentifyOptions is a container object
     * targeted to group all parameters applied on bot login, like shard information, initial status and even session
     * identifier and sequence required to resume a session.
     * <p>
     * Options under {@link #setShardIndex(Integer)}, {@link #setShardCount(Integer)} and
     * {@link #setInitialPresence(Presence)} override the values in this object.
     *
     * @param identifyOptions the new bot identification options. Can be set to {@code null} to use a default
     * @return this builder
     */
    public DiscordClientBuilder setIdentifyOptions(@Nullable IdentifyOptions identifyOptions) {
        this.identifyOptions = identifyOptions;
        return this;
    }

    /**
     * Get the current {@link RetryOptions} set in this builder. RetryOptions is an advanced container object to
     * parameterize the retry policy used in the gateway operations.
     *
     * @return the current retry policy, can be {@code null} if default is used
     */
    @Nullable
    @Deprecated
    public RetryOptions getRetryOptions() {
        return retryOptions;
    }

    /**
     * Set a new {@link RetryOptions} to this builder. RetryOptions is an advanced container object to parameterize
     * the retry policy used in the gateway operations.
     *
     * @param retryOptions the new retry policy. Can be set to {@code null} to use a default
     * @return this builder
     */
    public DiscordClientBuilder setRetryOptions(@Nullable RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Get the current {@link GatewayObserver} set in this builder. GatewayObserver is used as a simple event
     * listener for gateway connection lifecycle. User can be notified of broad lifecycle events like connections,
     * resumes, reconnects and disconnects but also very specific ones like session sequence updates.
     *
     * @return an event listener for gateway lifecycle, can be {@code null} if default is used
     * @see StoreInvalidator
     */
    @Nullable
    @Deprecated
    public GatewayObserver getGatewayObserver() {
        return gatewayObserver;
    }

    /**
     * Set a new {@link GatewayObserver} to this builder. GatewayObserver is used as a simple event listener for
     * gateway connection lifecycle. User can be notified of broad lifecycle events like connections, resumes,
     * reconnects and disconnects but also very specific ones like session sequence updates.
     *
     * @param gatewayObserver a new event listener for gateway lifecycle. Can be chained using
     * {@link GatewayObserver#then(GatewayObserver)} to create a composite of an arbitrary number of listeners. Can
     * be set to {@code null} to use a default
     * @return this builder
     */
    public DiscordClientBuilder setGatewayObserver(@Nullable GatewayObserver gatewayObserver) {
        this.gatewayObserver = gatewayObserver;
        return this;
    }

    /**
     * Get the current {@link PayloadTransformer} set in this builder. The transformer will be used as a rate
     * limiting strategy dedicated to coordinate identify requests between shards.
     *
     * @return the current gateway limiter, for shard coordination, can be {@code null} if default is used
     */
    @Nullable
    @Deprecated
    public PayloadTransformer getIdentifyLimiter() {
        return identifyLimiter;
    }

    /**
     * Set a new {@link PayloadTransformer} to this builder. The transformer will be used as a rate limiting strategy
     * dedicated to coordinate identify requests between shards.
     *
     * @param identifyLimiter the current limiter strategy, for shard coordination, can be {@code null} for a default
     * @return this builder
     */
    public DiscordClientBuilder setIdentifyLimiter(@Nullable PayloadTransformer identifyLimiter) {
        this.identifyLimiter = identifyLimiter;
        return this;
    }

    /**
     * Get the current {@link Scheduler} for voice sending tasks.
     *
     * @return the scheduler for voice sending tasks
     */
    @Deprecated
    public Scheduler getVoiceConnectionScheduler() {
        return voiceConnectionScheduler;
    }

    /**
     * Set a new {@link Scheduler} for voice sending tasks.
     *
     * @param voiceConnectionScheduler the new scheduler for voice sending tasks, must not be {@code null}
     * @return this builder
     */
    public DiscordClientBuilder setVoiceConnectionScheduler(Scheduler voiceConnectionScheduler) {
        this.voiceConnectionScheduler = Objects.requireNonNull(voiceConnectionScheduler);
        return this;
    }

    private IdentifyOptions initIdentifyOptions() {
        if (identifyOptions != null) {
            IdentifyOptions opts = new IdentifyOptions(
                    shardIndex != null ? shardIndex : identifyOptions.getShardIndex(),
                    shardCount != null ? shardCount : identifyOptions.getShardCount(),
                    initialPresence != null ? initialPresence.asStatusUpdate() : null);
            opts.setResumeSequence(identifyOptions.getResumeSequence());
            opts.setResumeSessionId(identifyOptions.getResumeSessionId());
            return opts;
        }
        return new IdentifyOptions(
                shardIndex != null ? shardIndex : 0,
                shardCount != null ? shardCount : 1,
                initialPresence != null ? initialPresence.asStatusUpdate() : null);
    }

    private RetryOptions initRetryOptions() {
        if (retryOptions != null) {
            return retryOptions;
        }
        return new RetryOptions(Duration.ofSeconds(2), Duration.ofSeconds(120),
                Integer.MAX_VALUE, Schedulers.parallel());
    }

    private StoreService initStoreService() {
        if (storeService != null) {
            return storeService;
        }
        Map<Class<? extends StoreService>, Integer> priority = new HashMap<>();
        // We want almost minimum priority, so that jdk can beat no-op but most implementations will beat jdk
        priority.put(JdkStoreService.class, Integer.MAX_VALUE - 1);
        StoreServiceLoader storeServiceLoader = new StoreServiceLoader(priority);
        return storeServiceLoader.getStoreService();
    }

    private FluxProcessor<Event, Event> initEventProcessor() {
        if (eventProcessor != null) {
            return eventProcessor;
        }
        return EmitterProcessor.create(false);
    }

    private Scheduler initEventScheduler() {
        if (eventScheduler != null) {
            return eventScheduler;
        }
        return ForkJoinPoolScheduler.create("discord4j-events");
    }

    private JacksonResourceProvider initJacksonResources() {
        if (jacksonResourceProvider != null) {
            return jacksonResourceProvider;
        }
        return new JacksonResourceProvider(mapper -> mapper.addHandler(new UnknownPropertyHandler(true)));
    }

    private HttpClient initHttpClient() {
        return HttpClient.create().compress(true);
    }

    private DiscordWebClient initWebClient(HttpClient httpClient, ObjectMapper mapper) {
        return new DiscordWebClient(httpClient, ExchangeStrategies.jackson(mapper), token);
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

    private GatewayClientFactory initGatewayClientFactory() {
        if (gatewayClientFactory != null) {
            return gatewayClientFactory;
        }
        return new DefaultGatewayClientFactory();
    }

    private GatewayObserver initGatewayObserver() {
        if (gatewayObserver != null) {
            return gatewayObserver;
        }
        return GatewayObserver.NOOP_LISTENER;
    }

    private PayloadTransformer initIdentifyLimiter() {
        if (identifyLimiter != null) {
            return identifyLimiter;
        }
        return new RateLimiterTransformer(new SimpleBucket(1, Duration.ofSeconds(6)));
    }

    /**
     * Create a client ready to connect to Discord.
     *
     * @return a {@link DiscordClient} based on this builder parameters
     */
    public DiscordClient build() {
        Hooks.onOperatorDebug();

        // Prepare identify options
        final IdentifyOptions identifyOptions = initIdentifyOptions();
        if (identifyOptions.getShardIndex() < 0 || identifyOptions.getShardIndex() >= identifyOptions.getShardCount()) {
            throw new IllegalArgumentException("0 <= shardIndex < shardCount");
        }
        final int shardId = identifyOptions.getShardIndex();
        final ClientConfig config = new ClientConfig(token, shardId, identifyOptions.getShardCount());

        // Prepare REST client
        final JacksonResourceProvider jackson = initJacksonResources();
        final HttpClient httpClient = initHttpClient();
        final DiscordWebClient webClient = initWebClient(httpClient, jackson.getObjectMapper());
        final RouterFactory routerFactory = initRouterFactory();
        final RestClient restClient = new RestClient(initRouter(routerFactory, webClient));

        // Prepare Stores
        final StoreService storeService = initStoreService();
        final StateHolder stateHolder = new StateHolder(storeService, new StoreContext(config.getShardIndex(),
                MessageBean.class));

        // Prepare gateway client
        final RetryOptions retryOptions = initRetryOptions();
        final StoreInvalidator storeInvalidator = new StoreInvalidator(stateHolder);
        final GatewayClient gatewayClient = initGatewayClientFactory().getGatewayClient(httpClient,
                new JacksonPayloadReader(jackson.getObjectMapper()),
                new JacksonPayloadWriter(jackson.getObjectMapper()),
                retryOptions, token, identifyOptions, storeInvalidator.then(initGatewayObserver()),
                initIdentifyLimiter());

        // Prepare event dispatcher
        final FluxProcessor<Event, Event> eventProcessor = initEventProcessor();
        final EventDispatcher eventDispatcher = new EventDispatcher(eventProcessor, initEventScheduler());

        final VoiceClient voiceClient = new VoiceClient(voiceConnectionScheduler, jackson.getObjectMapper(),
                guildId -> {
                    VoiceStateUpdate voiceStateUpdate = new VoiceStateUpdate(guildId, null, false, false);
                    gatewayClient.sender().next(GatewayPayload.voiceStateUpdate(voiceStateUpdate));
                });

        // Prepare mediator and wire gateway events to EventDispatcher
        final ServiceMediator serviceMediator = new ServiceMediator(gatewayClient, restClient, storeService,
                stateHolder, eventDispatcher, config, voiceClient);
        Logger dispatchLog = Loggers.getLogger("discord4j.dispatch." + config.getShardIndex());
        serviceMediator.getGatewayClient().dispatch()
                .log(dispatchLog, Level.FINE, false)
                .map(dispatch -> DispatchContext.of(dispatch, serviceMediator))
                .flatMap(context -> DispatchHandlers.handle(context)
                        .onErrorResume(error -> {
                            dispatchLog.error("Error dispatching {}", context.getDispatch().getClass().getSimpleName(), error);
                            return Mono.empty();
                        }))
                .subscribeWith(eventProcessor);

        final Properties properties = GitProperties.getProperties();
        final String url = properties.getProperty(GitProperties.APPLICATION_URL, "https://discord4j.com");
        final String name = properties.getProperty(GitProperties.APPLICATION_NAME, "Discord4J");
        final String version = properties.getProperty(GitProperties.APPLICATION_VERSION, "3");
        final String gitDescribe = properties.getProperty(GitProperties.GIT_COMMIT_ID_DESCRIBE, version);
        log.info("Shard {} with {} {} ({})", shardId, name, gitDescribe, url);
        return serviceMediator.getClient();
    }
}
