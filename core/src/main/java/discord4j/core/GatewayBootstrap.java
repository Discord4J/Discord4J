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

import discord4j.common.LogUtil;
import discord4j.common.ReactorResources;
import discord4j.core.event.EmitterEventDispatcher;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.dispatch.DispatchContext;
import discord4j.core.event.dispatch.DispatchHandlers;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.LocalShardCoordinator;
import discord4j.core.shard.ShardAwareStoreService;
import discord4j.core.shard.ShardCoordinator;
import discord4j.core.shard.ShardingJdkStoreRegistry;
import discord4j.gateway.*;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.StatusUpdate;
import discord4j.gateway.json.VoiceStateUpdate;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.GatewayStateChange;
import discord4j.gateway.retry.ReconnectOptions;
import discord4j.rest.RestClient;
import discord4j.rest.json.response.GatewayResponse;
import discord4j.rest.util.RouteUtils;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.service.StoreServiceLoader;
import discord4j.store.api.util.StoreContext;
import discord4j.store.jdk.JdkStoreService;
import discord4j.voice.VoiceClient;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static discord4j.common.LogUtil.format;

public class GatewayBootstrap<O extends GatewayOptions> {

    private static final Logger log = Loggers.getLogger(GatewayBootstrap.class);

    public static final int RECOMMENDED_SHARD_COUNT = 0;

    private final DiscordClient client;
    private final Function<GatewayOptions, O> optionsModifier;

    private int shardCount = RECOMMENDED_SHARD_COUNT;
    private Predicate<ShardInfo> shardFilter = shard -> true;
    private ShardCoordinator shardCoordinator = new LocalShardCoordinator();
    private EventDispatcher eventDispatcher = null;
    private StoreService storeService = null;
    private Function<O, GatewayClient> clientFactory = DefaultGatewayClient::new;
    private Function<ShardInfo, Presence> initialPresence = shard -> null;
    private Function<ShardInfo, SessionInfo> resumeOptions = shard -> null;
    private boolean completeEventDispatcherOnDisconnect = true;
    private boolean disposeStoreServiceOnDisconnect = true;

    private ReactorResources gatewayReactorResources = null;
    private ReactorResources voiceReactorResources = null;
    private PayloadReader payloadReader = null;
    private PayloadWriter payloadWriter = null;
    private ReconnectOptions reconnectOptions = ReconnectOptions.builder().build();
    private GatewayObserver gatewayObserver = GatewayObserver.NOOP_LISTENER;

    public static GatewayBootstrap<GatewayOptions> create(DiscordClient client) {
        return new GatewayBootstrap<>(client, Function.identity());
    }

    GatewayBootstrap(DiscordClient client, Function<GatewayOptions, O> optionsModifier) {
        this.client = client;
        this.optionsModifier = optionsModifier;
    }

    GatewayBootstrap(GatewayBootstrap<?> source, Function<GatewayOptions, O> optionsModifier) {
        this.client = source.client;
        this.optionsModifier = optionsModifier;
    }

    public <O2 extends GatewayOptions> GatewayBootstrap<O2> extraOptions(Function<? super O, O2> optionsModifier) {
        return new GatewayBootstrap<>(this, this.optionsModifier.andThen(optionsModifier));
    }

    public GatewayBootstrap<O> setShardCount(int shardCount) {
        this.shardCount = shardCount;
        return this;
    }

    public GatewayBootstrap<O> setShardFilter(Predicate<ShardInfo> shardFilter) {
        this.shardFilter = Objects.requireNonNull(shardFilter);
        return this;
    }

    public GatewayBootstrap<O> setShardCoordinator(ShardCoordinator shardCoordinator) {
        this.shardCoordinator = Objects.requireNonNull(shardCoordinator);
        return this;
    }

    public GatewayBootstrap<O> setEventDispatcher(@Nullable EventDispatcher eventDispatcher) {
        this.eventDispatcher = null;
        return this;
    }

    public GatewayBootstrap<O> setStoreService(@Nullable StoreService storeService) {
        this.storeService = storeService;
        return this;
    }

    public GatewayBootstrap<O> setGatewayClientFactory(Function<O, GatewayClient> clientFactory) {
        this.clientFactory = Objects.requireNonNull(clientFactory);
        return this;
    }

    public GatewayBootstrap<O> setInitialPresence(Function<ShardInfo, Presence> initialPresence) {
        this.initialPresence = initialPresence;
        return this;
    }

    public GatewayBootstrap<O> setResumeOptions(Function<ShardInfo, SessionInfo> resumeOptions) {
        this.resumeOptions = resumeOptions;
        return this;
    }

    public GatewayBootstrap<O> setGatewayReactorResources(ReactorResources gatewayReactorResources) {
        this.gatewayReactorResources = gatewayReactorResources;
        return this;
    }

    public GatewayBootstrap<O> setVoiceReactorResources(ReactorResources voiceReactorResources) {
        this.voiceReactorResources = voiceReactorResources;
        return this;
    }

    public GatewayBootstrap<O> setPayloadReader(PayloadReader payloadReader) {
        this.payloadReader = payloadReader;
        return this;
    }

    public GatewayBootstrap<O> setPayloadWriter(PayloadWriter payloadWriter) {
        this.payloadWriter = payloadWriter;
        return this;
    }

    public GatewayBootstrap<O> setReconnectOptions(ReconnectOptions reconnectOptions) {
        this.reconnectOptions = Objects.requireNonNull(reconnectOptions);
        return this;
    }

    public GatewayBootstrap<O> setGatewayObserver(GatewayObserver gatewayObserver) {
        this.gatewayObserver = Objects.requireNonNull(gatewayObserver);
        return this;
    }

    public Mono<Void> withConnectionUntilDisconnect(Function<GatewayDiscordClient, Mono<Void>> whileConnectedFunction) {
        return withConnection(gateway -> whileConnectedFunction.apply(gateway).then(gateway.onDisconnect()));
    }

    public <T> Mono<T> withConnection(Function<GatewayDiscordClient, Mono<T>> whileConnectedFunction) {
        return Mono.usingWhen(connect(), whileConnectedFunction, closeConnections());
    }

    private Function<GatewayDiscordClient, Publisher<?>> closeConnections() {
        return gateway -> Mono.whenDelayError(gateway.getGatewayClientMap().values()
                .stream()
                .filter(GatewayClient::isConnected)
                .map(gc -> gc.close(false))
                .collect(Collectors.toList()));
    }

    public GatewayDiscordClient connectNow() {
        return connect().block();
    }

    public Mono<GatewayDiscordClient> connect() {
        return connect(clientFactory == null ? DefaultGatewayClient::new : clientFactory);
    }

    public Mono<GatewayDiscordClient> connect(Function<O, GatewayClient> clientFactory) {
        StateHolder stateHolder = new StateHolder(initStoreService(), new StoreContext(0, MessageBean.class));
        StateView stateView = new StateView(stateHolder);
        EventDispatcher eventDispatcher = initEventDispatcher();
        GatewayResources resources = new GatewayResources(stateView, eventDispatcher, shardCoordinator);
        MonoProcessor<Void> closeProcessor = MonoProcessor.create();
        Map<Integer, GatewayClient> gatewayClients = new ConcurrentHashMap<>();
        Map<Integer, VoiceClient> voiceClients = new ConcurrentHashMap<>();
        GatewayDiscordClient gateway = new GatewayDiscordClient(client, resources, closeProcessor,
                gatewayClients, voiceClients);

        return computeShardCount(client.getCoreResources().getRestClient())
                .flatMapMany(count -> Flux.range(0, count)
                        .map(index -> new ShardInfo(index, count))
                        .filter(initShardFilter())
                        .transform(shardCoordinator.getConnectOperator())
                        .concatMap(shard -> acquireConnection(shard, clientFactory, gateway, stateHolder,
                                eventDispatcher, gatewayClients, voiceClients, closeProcessor)))
                .collectList()
                .thenReturn(gateway);
    }

    private Mono<GatewayConnection> acquireConnection(ShardInfo shard,
                                                      Function<O, GatewayClient> clientFactory,
                                                      GatewayDiscordClient gateway,
                                                      StateHolder stateHolder,
                                                      EventDispatcher eventDispatcher,
                                                      Map<Integer, GatewayClient> gatewayClients,
                                                      Map<Integer, VoiceClient> voiceClients,
                                                      MonoProcessor<Void> closeProcessor) {
        return Mono.subscriberContext()
                .flatMap(ctx -> Mono.<GatewayConnection>create(sink -> {
                    StatusUpdate initial = Optional.ofNullable(initialPresence.apply(shard))
                            .map(Presence::asStatusUpdate)
                            .orElse(null);
                    IdentifyOptions identify = new IdentifyOptions(shard, initial);
                    SessionInfo resume = resumeOptions.apply(shard);
                    if (resume != null) {
                        identify.setResumeSessionId(resume.getSessionId());
                        identify.setResumeSequence(resume.getSequence());
                    }
                    Disposable.Composite forCleanup = Disposables.composite();
                    GatewayClient gatewayClient = clientFactory.apply(buildOptions(identify));
                    // TODO use ReactorResources to build VoiceClient instances (custom HttpClient, UdpClient)
                    VoiceClient voiceClient = new VoiceClient(
                            initVoiceReactorResources().getTimerTaskScheduler(),
                            client.getCoreResources().getJacksonResources().getObjectMapper(),
                            guildId -> {
                                VoiceStateUpdate voiceStateUpdate = new VoiceStateUpdate(
                                        guildId, null, false, false);
                                gatewayClient.sender().next(GatewayPayload.voiceStateUpdate(voiceStateUpdate));
                            });

                    gatewayClients.put(shard.getIndex(), gatewayClient);
                    voiceClients.put(shard.getIndex(), voiceClient);

                    // wire gateway events to EventDispatcher
                    forCleanup.add(gatewayClient.dispatch()
                            .takeUntilOther(closeProcessor)
                            .map(dispatch -> DispatchContext.of(dispatch, gateway, stateHolder, shard))
                            .flatMap(dispatchContext -> DispatchHandlers.handle(dispatchContext)
                                    .subscriberContext(c -> c.put(LogUtil.KEY_SHARD_ID, shard.getIndex()))
                                    .onErrorResume(error -> {
                                        log.error(format(ctx, "Error dispatching event"), error);
                                        return Mono.empty();
                                    }))
                            .doOnNext(eventDispatcher::publish)
                            .subscribe(null,
                                    t -> log.error(format(ctx, "Event mapper terminated with an error"), t),
                                    () -> log.info(format(ctx, "Event mapper completed"))));

                    // wire internal shard coordinator events
                    // TODO: transition into separate lifecycleSink for these events
                    MonoProcessor<Void> shardCloseSignal = MonoProcessor.create();
                    forCleanup.add(gatewayClient.dispatch()
                            .takeUntilOther(closeProcessor)
                            .map(dispatch -> DispatchContext.of(dispatch, gateway, stateHolder, shard))
                            .filter(context -> context.getDispatch().getClass() == GatewayStateChange.class)
                            .flatMap(context -> {
                                GatewayStateChange event = (GatewayStateChange) context.getDispatch();
                                switch (event.getState()) {
                                    case CONNECTED:
                                        log.info(format(ctx, "Shard connected"));
                                        return shardCoordinator.publishConnected(shard)
                                                .doOnTerminate(() -> {
                                                    GatewayConnection connection = new GatewayConnection(
                                                            gateway, identify, shardCloseSignal);
                                                    sink.success(connection);
                                                });
                                    case DISCONNECTED:
                                    case DISCONNECTED_RESUME:
                                        log.info(format(ctx, "Shard disconnected"));
                                        boolean allowResume = event.getState() != GatewayStateChange.State.DISCONNECTED;
                                        SessionInfo session = allowResume ?
                                                new SessionInfo(gatewayClient.getSessionId(),
                                                        gatewayClient.getSequence()) : null;
                                        return shardCoordinator.publishDisconnected(shard, session)
                                                .then(stateHolder.invalidateStores())
                                                .then(Mono.fromRunnable(() -> {
                                                    shardCloseSignal.onComplete();
                                                    gatewayClients.remove(shard.getIndex());
                                                    voiceClients.remove(shard.getIndex());
                                                }))
                                                .then(Mono.defer(() -> {
                                                    if (gatewayClients.isEmpty()) {
                                                        log.debug(format(ctx, "All shards disconnected"));
                                                        closeProcessor.onComplete();
                                                        if (completeEventDispatcherOnDisconnect) {
                                                            eventDispatcher.complete();
                                                        }
                                                        if (disposeStoreServiceOnDisconnect) {
                                                            return storeService.dispose();
                                                        }
                                                    }
                                                    return Mono.empty();
                                                }));
                                    case RETRY_STARTED:
                                    case RETRY_FAILED:
                                        log.debug(format(ctx, "Invalidating stores for shard"));
                                        return stateHolder.invalidateStores();
                                }
                                return Mono.empty();
                            })
                            .subscriberContext(buildContext(gateway, shard))
                            .subscribe(null,
                                    t -> log.error(format(ctx, "Lifecycle listener terminated with an error"), t),
                                    () -> log.debug(format(ctx, "Lifecycle listener completed"))));

                    forCleanup.add(this.client.getCoreResources()
                            .getRestClient()
                            .getGatewayService()
                            .getGateway()
                            .flatMap(response -> gatewayClient.execute(
                                    RouteUtils.expandQuery(response.getUrl(), getGatewayParameters())))
                            .then(stateHolder.invalidateStores())
                            .subscriberContext(buildContext(gateway, shard))
                            .subscribe(null,
                                    t -> log.error(format(ctx, "Gateway terminated with an error"), t),
                                    () -> log.debug(format(ctx, "Gateway completed"))));

                    sink.onCancel(forCleanup);
                }))
                .subscriberContext(buildContext(gateway, shard));
    }

    private Function<Context, Context> buildContext(GatewayDiscordClient gateway, ShardInfo shard) {
        return ctx -> ctx.put(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(gateway.hashCode()))
                .put(LogUtil.KEY_SHARD_ID, shard.getIndex());
    }

    private PayloadReader initPayloadReader() {
        if (payloadReader != null) {
            return payloadReader;
        }
        return new JacksonPayloadReader(client.getCoreResources().getJacksonResources().getObjectMapper());
    }

    private PayloadWriter initPayloadWriter() {
        if (payloadWriter != null) {
            return payloadWriter;
        }
        return new JacksonPayloadWriter(client.getCoreResources().getJacksonResources().getObjectMapper());
    }

    private ReactorResources initGatewayReactorResources() {
        if (gatewayReactorResources != null) {
            return gatewayReactorResources;
        }
        return client.getCoreResources().getReactorResources();
    }

    private ReactorResources initVoiceReactorResources() {
        if (voiceReactorResources != null) {
            return voiceReactorResources;
        }
        return client.getCoreResources().getReactorResources();
    }

    private Predicate<ShardInfo> initShardFilter() {
        if (shardFilter != null) {
            return shardFilter;
        }
        return index -> true;
    }

    private EventDispatcher initEventDispatcher() {
        if (eventDispatcher != null) {
            return eventDispatcher;
        }
        return EmitterEventDispatcher.buffering();
    }

    private StoreService initStoreService() {
        if (storeService == null) {
            Map<Class<? extends StoreService>, Integer> priority = new HashMap<>();
            // We want almost minimum priority, so that jdk can beat no-op but most implementations will beat jdk
            priority.put(JdkStoreService.class, Integer.MAX_VALUE - 1);
            StoreServiceLoader storeServiceLoader = new StoreServiceLoader(priority);
            storeService = new ShardAwareStoreService(new ShardingJdkStoreRegistry(),
                    storeServiceLoader.getStoreService());
        }
        return storeService;
    }

    private Mono<Integer> computeShardCount(RestClient restClient) {
        if (shardCount <= RECOMMENDED_SHARD_COUNT) {
            return restClient.getGatewayService().getGatewayBot()
                    .map(GatewayResponse::getShards);
        }
        return Mono.just(shardCount);
    }

    private O buildOptions(IdentifyOptions identify) {
        GatewayOptions options = GatewayOptions.builder()
                .setToken(client.getCoreResources().getToken())
                .setReactorResources(initGatewayReactorResources())
                .setPayloadReader(initPayloadReader())
                .setPayloadWriter(initPayloadWriter())
                .setReconnectOptions(reconnectOptions)
                .setIdentifyOptions(identify)
                .setInitialObserver(gatewayObserver)
                .setIdentifyLimiter(shardCoordinator.getIdentifyLimiter())
                .build();
        return this.optionsModifier.apply(options);
    }

    private Map<String, Object> getGatewayParameters() {
        final Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("compress", "zlib-stream");
        parameters.put("encoding", "json");
        parameters.put("v", 6);
        return parameters;
    }
}
