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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.dispatch.DispatchContext;
import discord4j.core.event.dispatch.DispatchHandlers;
import discord4j.core.event.dispatch.StoreInvalidator;
import discord4j.core.event.domain.Event;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.presence.Presence;
import discord4j.core.util.VersionUtil;
import discord4j.gateway.*;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.VoiceStateUpdate;
import discord4j.gateway.json.dispatch.Dispatch;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import discord4j.rest.RestClient;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.service.StoreServiceLoader;
import discord4j.store.api.util.StoreContext;
import discord4j.store.jdk.JdkStoreService;
import discord4j.voice.VoiceClient;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.util.Logger;
import reactor.util.Loggers;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;

public final class DiscordClientBuilder {

    private static final Logger log = Loggers.getLogger(DiscordClientBuilder.class);

    private String token;

    @Nullable
    private Integer shardIndex;

    @Nullable
    private Integer shardCount;

    @Nullable
    private StoreService storeService = null;

    private FluxProcessor<Event, Event> eventProcessor;

    private Scheduler eventScheduler;

    private Scheduler routerScheduler;

    private Presence initialPresence;

    @Nullable
    private IdentifyOptions identifyOptions;

    private RetryOptions retryOptions;

    private boolean ignoreUnknownJsonKeys = true;

    @Nullable
    private GatewayObserver gatewayObserver;

    @Nullable
    private GatewayLimiter gatewayLimiter;

    private Scheduler voiceConnectionScheduler = Schedulers.elastic();

    public DiscordClientBuilder(final String token) {
        this.token = Objects.requireNonNull(token);
    }

    public String getToken() {
        return token;
    }

    public DiscordClientBuilder setToken(final String token) {
        this.token = Objects.requireNonNull(token);
        return this;
    }

    @Nullable
    public Integer getShardIndex() {
        return shardIndex;
    }

    public DiscordClientBuilder setShardIndex(@Nullable Integer shardIndex) {
        this.shardIndex = shardIndex;
        return this;
    }

    @Nullable
    public Integer getShardCount() {
        return shardCount;
    }

    public DiscordClientBuilder setShardCount(@Nullable Integer shardCount) {
        this.shardCount = shardCount;
        return this;
    }

    @Nullable
    public StoreService getStoreService() {
        return storeService;
    }

    public DiscordClientBuilder setStoreService(final StoreService storeService) {
        this.storeService = Objects.requireNonNull(storeService);
        return this;
    }

    public FluxProcessor<Event, Event> getEventProcessor() {
        return eventProcessor;
    }

    public DiscordClientBuilder setEventProcessor(final FluxProcessor<Event, Event> eventProcessor) {
        this.eventProcessor = Objects.requireNonNull(eventProcessor);
        return this;
    }

    public Scheduler getEventScheduler() {
        return eventScheduler;
    }

    public DiscordClientBuilder setEventScheduler(final Scheduler eventScheduler) {
        this.eventScheduler = Objects.requireNonNull(eventScheduler);
        return this;
    }

    public Scheduler getRouterScheduler() {
        return routerScheduler;
    }

    public DiscordClientBuilder setRouterScheduler(Scheduler routerScheduler) {
        this.routerScheduler = Objects.requireNonNull(routerScheduler);
        return this;
    }

    public Presence getInitialPresence() {
        return initialPresence;
    }

    public DiscordClientBuilder setInitialPresence(@Nullable Presence initialPresence) {
        this.initialPresence = initialPresence;
        return this;
    }

    @Nullable
    public IdentifyOptions getIdentifyOptions() {
        return identifyOptions;
    }

    public DiscordClientBuilder setIdentifyOptions(IdentifyOptions identifyOptions) {
        this.identifyOptions = identifyOptions;
        return this;
    }

    public RetryOptions getRetryOptions() {
        return retryOptions;
    }

    public void setRetryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
    }

    public boolean getIgnoreUnknownJsonKeys() {
        return ignoreUnknownJsonKeys;
    }

    public DiscordClientBuilder setIgnoreUnknownJsonKeys(boolean ignoreUnknownJsonKeys) {
        this.ignoreUnknownJsonKeys = ignoreUnknownJsonKeys;
        return this;
    }

    @Nullable
    public GatewayObserver getGatewayObserver() {
        return gatewayObserver;
    }

    public DiscordClientBuilder setGatewayObserver(@Nullable GatewayObserver gatewayObserver) {
        this.gatewayObserver = gatewayObserver;
        return this;
    }

    @Nullable
    public GatewayLimiter getGatewayLimiter() {
        return gatewayLimiter;
    }

    public DiscordClientBuilder setGatewayLimiter(@Nullable GatewayLimiter gatewayLimiter) {
        this.gatewayLimiter = gatewayLimiter;
        return this;
    }

    public Scheduler getVoiceConnectionScheduler() {
        return voiceConnectionScheduler;
    }

    public DiscordClientBuilder setVoiceConnectionScheduler(Scheduler voiceConnectionScheduler) {
        this.voiceConnectionScheduler = voiceConnectionScheduler;
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
        return new RetryOptions(Duration.ofSeconds(2), Duration.ofSeconds(120), Integer.MAX_VALUE);
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
        return Schedulers.fromExecutor(Executors.newWorkStealingPool());
    }

    private Scheduler initRouterScheduler() {
        if (routerScheduler != null) {
            return routerScheduler;
        }
        return Schedulers.elastic();
    }

    private GatewayObserver initGatewayObserver() {
        if (gatewayObserver != null) {
            return gatewayObserver;
        }
        return GatewayObserver.NOOP_LISTENER;
    }

    private GatewayLimiter initGatewayLimiter() {
        if (gatewayLimiter != null) {
            return gatewayLimiter;
        }
        return new SimpleBucket(1, Duration.ofSeconds(6));
    }

    public DiscordClient build() {
        Hooks.onOperatorDebug();

        final ObjectMapper mapper = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .addHandler(new UnknownPropertyHandler(ignoreUnknownJsonKeys))
                .registerModules(new PossibleModule(), new Jdk8Module());

        // Prepare identify options
        final IdentifyOptions identifyOptions = initIdentifyOptions();
        if (identifyOptions.getShardIndex() < 0 || identifyOptions.getShardIndex() >= identifyOptions.getShardCount()) {
            throw new IllegalArgumentException("0 <= shardIndex < shardCount");
        }

        // Retrieve version properties
        final Properties properties = VersionUtil.getProperties();
        final String version = properties.getProperty(VersionUtil.APPLICATION_VERSION, "3");
        final String url = properties.getProperty(VersionUtil.APPLICATION_URL, "https://discord4j.com");
        final String name = properties.getProperty(VersionUtil.APPLICATION_NAME, "Discord4J");
        final String gitDescribe = properties.getProperty(VersionUtil.GIT_COMMIT_ID_DESCRIBE, version);

        // Prepare RestClient
        final HttpHeaders defaultHeaders = new DefaultHttpHeaders();
        defaultHeaders.add(HttpHeaderNames.CONTENT_TYPE, "application/json");
        defaultHeaders.add(HttpHeaderNames.AUTHORIZATION, "Bot " + token);
        defaultHeaders.add(HttpHeaderNames.USER_AGENT, "DiscordBot(" + url + ", " + version + ")");
        final HttpClient httpClient = HttpClient.create().baseUrl(Routes.BASE_URL).compress(true);
        final DiscordWebClient webClient = new DiscordWebClient(httpClient, defaultHeaders,
                ExchangeStrategies.withJacksonDefaults(mapper));
        final RestClient restClient = new RestClient(new Router(webClient, initRouterScheduler()));

        // Prepare identify parameters
        final ClientConfig config = new ClientConfig(token, identifyOptions.getShardIndex(),
                identifyOptions.getShardCount());

        // Prepare Stores
        final StoreService storeService = initStoreService();
        final StateHolder stateHolder = new StateHolder(storeService, new StoreContext(config.getShardIndex(),
                MessageBean.class));

        // Prepare GatewayClient
        final RetryOptions retryOptions = initRetryOptions();
        final StoreInvalidator storeInvalidator = new StoreInvalidator(stateHolder);
        final GatewayClient gatewayClient = new GatewayClient(
                new JacksonPayloadReader(mapper), new JacksonPayloadWriter(mapper),
                retryOptions, token, identifyOptions, storeInvalidator.then(initGatewayObserver()), initGatewayLimiter());

        // Prepare EventDispatcher
        final FluxProcessor<Event, Event> eventProcessor = initEventProcessor();
        final EventDispatcher eventDispatcher = new EventDispatcher(eventProcessor, initEventScheduler());

        final VoiceClient voiceClient = new VoiceClient(voiceConnectionScheduler, mapper, guildId -> {
            VoiceStateUpdate voiceStateUpdate = new VoiceStateUpdate(guildId, null, false, false);
            gatewayClient.sender().next(GatewayPayload.voiceStateUpdate(voiceStateUpdate));
        });

        // Prepare mediator and wire gateway events to EventDispatcher
        final ServiceMediator serviceMediator = new ServiceMediator(gatewayClient, restClient, storeService,
                stateHolder, eventDispatcher, config, voiceClient);
        serviceMediator.getGatewayClient().dispatch()
                .map(dispatch -> DispatchContext.of(dispatch, serviceMediator))
                .flatMap(DispatchHandlers::<Dispatch, Event>handle)
                .subscribeWith(eventProcessor);

        log.info("Shard {} with {} {} ({})", identifyOptions.getShardIndex(), name, gitDescribe, url);
        return serviceMediator.getClient();
    }
}
