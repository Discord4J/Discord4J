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
import discord4j.core.event.domain.Event;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.LocalShardCoordinator;
import discord4j.core.shard.ShardAwareStoreService;
import discord4j.core.shard.ShardCoordinator;
import discord4j.core.shard.ShardingJdkStoreRegistry;
import discord4j.core.state.StateView;
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
import discord4j.store.api.Store;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.service.StoreServiceLoader;
import discord4j.store.api.util.StoreContext;
import discord4j.store.jdk.JdkStoreService;
import discord4j.voice.VoiceClient;
import io.netty.buffer.ByteBuf;
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

/**
 * A builder to create a {@link GatewayDiscordClient}. The place where resources and many configurations are set before
 * connecting to Discord Gateway. Defaults are set to enable built-in sharding using the recommended amount of shards.
 * Refer to each setter for more details about the default values for each configuration.
 * <p>
 * Discord Gateway connections are not established until one of the following methods is subscribed to:
 * <ul>
 *     <li>{@link #connect()} to obtain a {@link Mono} for a {@link GatewayDiscordClient} that can be externally
 *     managed.</li>
 *     <li>{@link #connect(Function)} to customize the {@link GatewayClient} instances to build.</li>
 *     <li>{@link #withConnection(Function)} to work with the {@link GatewayDiscordClient} in a scoped way, providing
 *     a mapping function that will close and release all resources on completion.</li>
 *     <li>{@link #withConnectionUntilDisconnect(Function)} to work in a scoped way (just as above), with the
 *     difference that the mapper function will also defer completion until all Gateway connections have ended.</li>
 * </ul>
 * For all cases, the produced {@link Mono} result or mapper function call will happen after all joining shards have
 * established a connection. You can configure which shards are joining by specifying a
 * {@link #setShardFilter(Predicate)}
 * </p>
 *
 * @param <O> the configuration flavor supplied to the {@link GatewayClient} instances to be built.
 */
public class GatewayBootstrap<O extends GatewayOptions> {

    private static final Logger log = Loggers.getLogger(GatewayBootstrap.class);

    public static final int RECOMMENDED_SHARD_COUNT = 0;

    private final DiscordClient client;
    private final Function<GatewayOptions, O> optionsModifier;

    private int shardCount = RECOMMENDED_SHARD_COUNT;
    private Predicate<ShardInfo> shardFilter = shard -> true;
    private boolean awaitAllShards = false;
    private ShardCoordinator shardCoordinator = new LocalShardCoordinator();
    private EventDispatcher eventDispatcher = null;
    private StoreService storeService = null;
    private Function<ShardInfo, Presence> initialPresence = shard -> null;
    private Function<ShardInfo, SessionInfo> resumeOptions = shard -> null;
    private Function<GatewayDiscordClient, Mono<Void>> destroyHandler = shutdownDestroyHandler();

    private ReactorResources gatewayReactorResources = null;
    private ReactorResources voiceReactorResources = null;
    private PayloadReader payloadReader = null;
    private PayloadWriter payloadWriter = null;
    private ReconnectOptions reconnectOptions = ReconnectOptions.builder().build();
    private GatewayObserver gatewayObserver = GatewayObserver.NOOP_LISTENER;

    /**
     * Create a default {@link GatewayBootstrap} based off the given {@link DiscordClient} that provides an instance
     * of {@link CoreResources} used to provide defaults while building a {@link GatewayDiscordClient}.
     *
     * @param client the {@link DiscordClient} used to setup configuration
     * @return a default builder to create {@link GatewayDiscordClient}
     */
    public static GatewayBootstrap<GatewayOptions> create(DiscordClient client) {
        return new GatewayBootstrap<>(client, Function.identity());
    }

    GatewayBootstrap(DiscordClient client, Function<GatewayOptions, O> optionsModifier) {
        this.client = client;
        this.optionsModifier = optionsModifier;
    }

    GatewayBootstrap(GatewayBootstrap<?> source, Function<GatewayOptions, O> optionsModifier) {
        this.optionsModifier = optionsModifier;

        this.client = source.client;
        this.shardCount = source.shardCount;
        this.shardFilter = source.shardFilter;
        this.awaitAllShards = source.awaitAllShards;
        this.shardCoordinator = source.shardCoordinator;
        this.eventDispatcher = source.eventDispatcher;
        this.storeService = source.storeService;
        this.initialPresence = source.initialPresence;
        this.resumeOptions = source.resumeOptions;
        this.destroyHandler = source.destroyHandler;
        this.gatewayReactorResources = source.gatewayReactorResources;
        this.voiceReactorResources = source.voiceReactorResources;
        this.payloadReader = source.payloadReader;
        this.payloadWriter = source.payloadWriter;
        this.reconnectOptions = source.reconnectOptions;
        this.gatewayObserver = source.gatewayObserver;
    }

    /**
     * Add a configuration for {@link GatewayClient} implementation-specific cases, changing the type of the current
     * {@link GatewayOptions} object passed to the {@link GatewayClient} factory in connect methods.
     *
     * @param optionsModifier {@link Function} to transform the {@link GatewayOptions} type to provide custom
     * {@link GatewayClient} implementations a proper configuration object.
     * @param <O2> new type for the options
     * @return a new {@link GatewayBootstrap} that will now work with the new options type.
     */
    public <O2 extends GatewayOptions> GatewayBootstrap<O2> setExtraOptions(Function<? super O, O2> optionsModifier) {
        return new GatewayBootstrap<>(this, this.optionsModifier.andThen(optionsModifier));
    }

    /**
     * Define the shard count value used when identifying to the Gateway. Note that this number is unrelated to the
     * actual number of shards joining while connecting. To further configure the actual shards to join refer to
     * {@link #setShardFilter(Predicate)}.
     * <p>
     * The default is to use the recommended shard count given by Discord.
     * </p>
     *
     * @param shardCount the shard count property to use when connecting to Gateway
     * @return this builder
     */
    public GatewayBootstrap<O> setShardCount(int shardCount) {
        this.shardCount = shardCount;
        return this;
    }

    /**
     * Set a {@link Predicate} to determine whether a shard should join the produced {@link GatewayDiscordClient}.
     * Defaults to connecting to all shards given by shard count.
     *
     * @param shardFilter a {@link Predicate} for {@link ShardInfo} objects. Called for each shard determined by
     * {@link #setShardCount(int)} and schedules it for connection if returning {@code true}.
     * @return this builder
     */
    public GatewayBootstrap<O> setShardFilter(Predicate<ShardInfo> shardFilter) {
        this.shardFilter = Objects.requireNonNull(shardFilter);
        return this;
    }

    /**
     * Set if the connect {@link Mono} should defer completion until all joining shards have connected. Defaults to
     * {@code false}.
     *
     * @param awaitAllShards {@code true} if connect should wait until all joining shards have connected before
     * completing, or {@code false} to complete immediately
     * @return this builder
     */
    public GatewayBootstrap<O> setAwaitAllShards(boolean awaitAllShards) {
        this.awaitAllShards = awaitAllShards;
        return this;
    }

    /**
     * Set a custom {@link ShardCoordinator} to manage multiple {@link GatewayDiscordClient} instances, even across
     * boundaries. Defaults to using {@link LocalShardCoordinator}.
     *
     * @param shardCoordinator an externally managed {@link ShardCoordinator} to coordinate multiple
     * {@link GatewayDiscordClient} instances.
     * @return this builder
     */
    public GatewayBootstrap<O> setShardCoordinator(ShardCoordinator shardCoordinator) {
        this.shardCoordinator = Objects.requireNonNull(shardCoordinator);
        return this;
    }

    /**
     * Set a custom {@link EventDispatcher} to receive {@link Event Events} from all joining shards and publish them to
     * all subscribers. Defaults to using {@link EmitterEventDispatcher#buffering()} that will buffer all events
     * until all joining shards have connected.
     *
     * @param eventDispatcher an externally managed {@link EventDispatcher} to publish events
     * @return this builder
     */
    public GatewayBootstrap<O> setEventDispatcher(@Nullable EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        return this;
    }

    /**
     * Set a custom {@link StoreService}, an abstract factory to create {@link Store} instances, to cache Gateway
     * updates. Defaults to using {@link JdkStoreService} unless another factory of higher priority is discovered.
     *
     * @param storeService an externally managed {@link StoreService} to receive Gateway updates
     * @return this builder
     */
    public GatewayBootstrap<O> setStoreService(@Nullable StoreService storeService) {
        this.storeService = storeService;
        return this;
    }

    /**
     * Set a custom {@link Function handler} that generate a destroy sequence to be run once all joining shards have
     * disconnected, after all internal resources have been released. The destroy procedure is applied asynchronously
     * and errors are logged and swallowed. Defaults to {@link GatewayBootstrap#shutdownDestroyHandler()} that will
     * release the set {@link EventDispatcher} and {@link StoreService}.
     *
     * @param destroyHandler the {@link Function} supplying a {@link Mono} to reset state
     * @return this builder
     */
    public GatewayBootstrap<O> setDestroyHandler(Function<GatewayDiscordClient, Mono<Void>> destroyHandler) {
        this.destroyHandler = Objects.requireNonNull(destroyHandler, "destroyHandler");
        return this;
    }

    /**
     * Set a {@link Function} to determine the {@link Presence} that each joining shard should use when identifying
     * to the Gateway. Defaults to no presence given.
     *
     * @param initialPresence a {@link Function} that supplies {@link Presence} instances from a given {@link ShardInfo}
     * @return this builder
     */
    public GatewayBootstrap<O> setInitialPresence(Function<ShardInfo, Presence> initialPresence) {
        this.initialPresence = Objects.requireNonNull(initialPresence, "initialPresence");
        return this;
    }

    /**
     * Set a {@link Function} to determine the details to resume a session that each joining shard should use when
     * identifying for the first time to the Gateway. Defaults to returning {@code null} to begin a fresh session on
     * startup.
     *
     * @param resumeOptions a {@link Function} that supplies {@link SessionInfo} instances from a given
     * {@link ShardInfo}
     * @return this builder
     */
    public GatewayBootstrap<O> setResumeOptions(Function<ShardInfo, SessionInfo> resumeOptions) {
        this.resumeOptions = Objects.requireNonNull(resumeOptions, "resumeOptions");
        return this;
    }

    /**
     * Customize the {@link ReactorResources} used exclusively for Gateway-related operations, such as maintaining
     * the websocket connections and scheduling Gateway tasks. Defaults to using the parent {@link ReactorResources}
     * inherited from {@link DiscordClient}, which is equivalent to supplying {@code null} as argument.
     *
     * @param gatewayReactorResources a {@link ReactorResources} object for Gateway operations
     * @return this builder
     */
    public GatewayBootstrap<O> setGatewayReactorResources(@Nullable ReactorResources gatewayReactorResources) {
        this.gatewayReactorResources = gatewayReactorResources;
        return this;
    }

    /**
     * Customize how inbound Gateway payloads are decoded from {@link ByteBuf}.
     *
     * @param payloadReader a Gateway payload decoder
     * @return this builder
     */
    public GatewayBootstrap<O> setPayloadReader(@Nullable PayloadReader payloadReader) {
        this.payloadReader = payloadReader;
        return this;
    }

    /**
     * Customize how outbound Gateway payloads are encoded into {@link ByteBuf}.
     *
     * @param payloadWriter a Gateway payload encoder
     * @return this builder
     */
    public GatewayBootstrap<O> setPayloadWriter(@Nullable PayloadWriter payloadWriter) {
        this.payloadWriter = payloadWriter;
        return this;
    }

    /**
     * Set a custom {@link ReconnectOptions} to configure how Gateway connections will attempt to reconnect every
     * time a websocket session is closed.
     *
     * @param reconnectOptions a {@link ReconnectOptions} policy to use in Gateway connections
     * @return this builder
     */
    public GatewayBootstrap<O> setReconnectOptions(ReconnectOptions reconnectOptions) {
        this.reconnectOptions = Objects.requireNonNull(reconnectOptions);
        return this;
    }

    /**
     * Set a custom {@link GatewayObserver} to be notified of Gateway lifecycle events across all joining shards.
     *
     * @param gatewayObserver a {@link GatewayObserver} to install on all joining shards
     * @return this builder
     */
    public GatewayBootstrap<O> setGatewayObserver(GatewayObserver gatewayObserver) {
        this.gatewayObserver = Objects.requireNonNull(gatewayObserver);
        return this;
    }

    /**
     * Customize the {@link ReactorResources} used exclusively for voice-related operations, such as maintaining
     * the Voice Gateway websocket connections, Voice UDP socket connections and scheduling Gateway tasks. Defaults
     * to using the parent {@link ReactorResources} inherited from {@link DiscordClient}, which is equivalent to
     * supplying {@code null} as argument.
     *
     * @param voiceReactorResources a {@link ReactorResources} object for voice operations
     * @return this builder
     */
    public GatewayBootstrap<O> setVoiceReactorResources(@Nullable ReactorResources voiceReactorResources) {
        this.voiceReactorResources = voiceReactorResources;
        return this;
    }

    /**
     * Connect to the Discord Gateway upon subscription to acquire a {@link GatewayDiscordClient} instance and use it
     * in a declarative manner, releasing the object once the derived usage {@link Function} terminates or is
     * cancelled and also the {@link GatewayDiscordClient} has disconnected from the Gateway.
     * <p>
     * The timing of acquiring a {@link GatewayDiscordClient} depends on the {@link #setAwaitAllShards(boolean)}
     * setting: if {@code true}, when all joining shards have connected; if {@code false}, as soon as it is possible
     * to establish a connection to the Gateway.
     * <p>
     * Calling this method is useful when you operate on the {@link GatewayDiscordClient} object using reactive API you
     * can compose within the scope of the given {@link Function}.
     *
     * @param whileConnectedFunction the {@link Function} to apply the <strong>connected</strong>
     * {@link GatewayDiscordClient} and trigger a processing pipeline from it.
     * @return the {@link Mono} result of processing the given {@link Function} after all resources have released
     */
    public Mono<Void> withConnectionUntilDisconnect(Function<GatewayDiscordClient, Mono<Void>> whileConnectedFunction) {
        return withConnection(gateway -> whileConnectedFunction.apply(gateway).then(gateway.onDisconnect()));
    }

    /**
     * Connect to the Discord Gateway upon subscription to acquire a {@link GatewayDiscordClient} instance and use it
     * in a declarative manner, releasing the object once the derived usage {@link Function} terminates or is cancelled.
     * <p>
     * The timing of acquiring a {@link GatewayDiscordClient} depends on the {@link #setAwaitAllShards(boolean)}
     * setting: if {@code true}, when all joining shards have connected; if {@code false}, as soon as it is possible to
     * establish a connection to the Gateway.
     * <p>
     * Calling this method is useful when you operate on the {@link GatewayDiscordClient} object using reactive API you
     * can compose within the scope of the given {@link Function}. Using {@link GatewayDiscordClient#onDisconnect()}
     * within the scope will await for disconnection before releasing resources.
     *
     * @param whileConnectedFunction the {@link Function} to apply the <strong>connected</strong>
     * {@link GatewayDiscordClient} and trigger a processing pipeline from it.
     * @param <T> type of the given {@link Function} output
     * @return the {@link Mono} result of processing the given {@link Function} after all resources have released
     */
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

    /**
     * Connect to the Discord Gateway upon subscription to build a {@link GatewayClient} from the set of options
     * configured by this builder. The resulting {@link GatewayDiscordClient} can be externally managed, leaving you
     * in charge of properly releasing its resources by calling {@link GatewayDiscordClient#logout()}.
     * <p>
     * The timing of acquiring a {@link GatewayDiscordClient} depends on the {@link #setAwaitAllShards(boolean)}
     * setting: if {@code true}, when all joining shards have connected; if {@code false}, as soon as it is possible
     * to establish a connection to the Gateway.
     * <p>
     * All joining shards, determined by a combination of {@link #setShardCount(int)} and
     * {@link #setShardFilter(Predicate)}, will attempt to serially connect to Discord Gateway, coordinated by the
     * current {@link ShardCoordinator}. If one of the shards fail to connect due to a retryable problem like invalid
     * session it will retry before continuing to the next one.
     *
     * @return a {@link Mono} that upon subscription and depending on the configuration of
     * {@link #setAwaitAllShards(boolean)}, emits a {@link GatewayDiscordClient}. If an error occurs during the setup
     * sequence, it will be emitted through the {@link Mono}.
     */
    public Mono<GatewayDiscordClient> connect() {
        return connect(DefaultGatewayClient::new);
    }

    /**
     * Connect to the Discord Gateway upon subscription using a custom {@link Function factory} to build a
     * {@link GatewayClient} from the set of options configured by this builder. See {@link #connect()} for more details
     * about how the returned {@link Mono} operates.
     *
     * @return a {@link Mono} that upon subscription and depending on the configuration of
     * {@link #setAwaitAllShards(boolean)}, emits a {@link GatewayDiscordClient}. If an error occurs during the setup
     * sequence, it will be emitted through the {@link Mono}.
     */
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

        Flux<GatewayConnection> connections = computeShardCount(client.getCoreResources().getRestClient())
                .flatMapMany(count -> Flux.range(0, count)
                        .map(index -> new ShardInfo(index, count))
                        .filter(initShardFilter())
                        .transform(shardCoordinator.getConnectOperator())
                        .concatMap(shard -> acquireConnection(shard, clientFactory, gateway, stateHolder,
                                eventDispatcher, gatewayClients, voiceClients, closeProcessor)));

        if (awaitAllShards) {
            return connections.collectList().thenReturn(gateway);
        } else {
            return Mono.create(sink -> {
                sink.onCancel(connections.subscribe(null, sink::error));
                sink.success(gateway);
            });
        }
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
                                    () -> log.debug(format(ctx, "Event mapper completed"))));

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
                                                        log.info(format(ctx, "All shards disconnected"));
                                                        return destroyHandler.apply(gateway)
                                                                .doOnTerminate(closeProcessor::onComplete);
                                                    }
                                                    return Mono.empty();
                                                }))
                                                .onErrorResume(t -> {
                                                    log.warn(format(ctx, "Error while releasing resources"), t);
                                                    return Mono.empty();
                                                });
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

    public static Function<GatewayDiscordClient, Mono<Void>> noopDestroyHandler() {
        return gateway -> Mono.empty();
    }

    public static Function<GatewayDiscordClient, Mono<Void>> shutdownDestroyHandler() {
        return gateway -> {
            gateway.getEventDispatcher().shutdown();
            return gateway.getGatewayResources().getStateView().getStoreService().dispose();
        };
    }
}
