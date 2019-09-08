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

import discord4j.core.event.EmitterEventDispatcher;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.dispatch.DispatchContext;
import discord4j.core.event.dispatch.DispatchHandlers;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.presence.Presence;
import discord4j.gateway.*;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.StatusUpdate;
import discord4j.gateway.json.VoiceStateUpdate;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.ReconnectOptions;
import discord4j.rest.RestClient;
import discord4j.rest.json.response.GatewayResponse;
import discord4j.rest.util.RouteUtils;
import discord4j.store.api.util.StoreContext;
import discord4j.voice.VoiceClient;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.netty.http.client.HttpClient;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class GatewayBootstrap<O extends GatewayOptions> {

    private static final Logger log = Loggers.getLogger(GatewayBootstrap.class);

    public static final int RECOMMENDED_SHARD_COUNT = 0;

    private final DiscordClient client;
    private final GatewayResources resources;
    private final Function<GatewayOptions, O> optionsModifier;

    private Function<O, GatewayClient> clientFactory = DefaultGatewayClient::new;
    private int shardCount = RECOMMENDED_SHARD_COUNT;
    private Predicate<ShardInfo> shardFilter;
    private Function<ShardInfo, Presence> initialPresence;
    private Function<ShardInfo, SessionInfo> resumeOptions;
    private PayloadReader payloadReader;
    private PayloadWriter payloadWriter;
    private HttpClient httpClient;
    private GatewayObserver gatewayObserver;
    private EventDispatcher eventDispatcher;

    public static GatewayBootstrap<GatewayOptions> create(DiscordClient client, GatewayResources resources) {
        return new GatewayBootstrap<>(client, resources, Function.identity());
    }

    GatewayBootstrap(DiscordClient client, GatewayResources resources, Function<GatewayOptions, O> optionsModifier) {
        this.client = client;
        this.resources = resources;
        this.optionsModifier = optionsModifier;
    }

    GatewayBootstrap(GatewayBootstrap<?> source, Function<GatewayOptions, O> optionsModifier) {
        this.client = source.client;
        this.resources = source.resources;
        this.optionsModifier = optionsModifier;
    }

    public GatewayBootstrap<O> mutate(Function<GatewayResources.Builder, GatewayResources> resourcesModifier) {
        GatewayResources modifiedResources = resourcesModifier.apply(this.resources.mutate());
        return new GatewayBootstrap<>(this.client, modifiedResources, this.optionsModifier);
    }

    public <O2 extends GatewayOptions> GatewayBootstrap<O2> extraOptions(Function<? super O, O2> optionsModifier) {
        return new GatewayBootstrap<>(this, this.optionsModifier.andThen(optionsModifier));
    }

    public GatewayBootstrap<O> setGatewayClientFactory(Function<O, GatewayClient> clientFactory) {
        this.clientFactory = clientFactory;
        return this;
    }

    public GatewayBootstrap<O> setShardCount(int shardCount) {
        this.shardCount = shardCount;
        return this;
    }

    public GatewayBootstrap<O> setShardFilter(Predicate<ShardInfo> shardFilter) {
        this.shardFilter = shardFilter;
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

    public GatewayBootstrap<O> setPayloadReader(PayloadReader payloadReader) {
        this.payloadReader = payloadReader;
        return this;
    }

    public GatewayBootstrap<O> setPayloadWriter(PayloadWriter payloadWriter) {
        this.payloadWriter = payloadWriter;
        return this;
    }

    public GatewayBootstrap<O> setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public GatewayBootstrap<O> setGatewayObserver(GatewayObserver gatewayObserver) {
        this.gatewayObserver = gatewayObserver;
        return this;
    }

    public GatewayBootstrap<O> setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        return this;
    }

    private Mono<Integer> initShardCount(RestClient restClient) {
        if (shardCount <= RECOMMENDED_SHARD_COUNT) {
            return restClient.getGatewayService().getGatewayBot()
                    .map(GatewayResponse::getShards);
        }
        return Mono.just(shardCount);
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

    private HttpClient initHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        return client.getCoreResources().getReactorResources().getHttpClient();
    }

    private Predicate<ShardInfo> initShardFilter() {
        if (shardFilter != null) {
            return shardFilter;
        }
        return index -> true;
    }

    private Function<ShardInfo, Presence> initInitialPresence() {
        if (initialPresence != null) {
            return initialPresence;
        }
        return shard -> null;
    }

    private Function<ShardInfo, SessionInfo> initResumeOptions() {
        if (resumeOptions != null) {
            return resumeOptions;
        }
        return shard -> null;
    }

    private GatewayObserver initGatewayObserver() {
        if (gatewayObserver != null) {
            return gatewayObserver;
        }
        return GatewayObserver.NOOP_LISTENER;
    }

    private EventDispatcher initEventDispatcher() {
        if (eventDispatcher != null) {
            return eventDispatcher;
        }
        return new EmitterEventDispatcher(
                EmitterProcessor.create(256, false),
                ForkJoinPoolScheduler.create("discord4j-events"));
    }

    public Mono<Void> connectAndWait(Function<Gateway, Mono<Void>> scopeFunction) {
        // login and complete on disconnect
        return Mono.usingWhen(acquire(),
                agg -> scopeFunction.apply(agg.getGateway())
                        .then(Mono.whenDelayError(agg.getConnections().stream()
                                .map(GatewayConnection::onDisconnect)
                                .collect(Collectors.toList()))),
                closeConnections(),
                closeConnections());
    }

    private Function<GatewayAggregate, Publisher<?>> closeConnections() {
        return agg -> Mono.whenDelayError(agg.getConnections().stream()
                .map(GatewayConnection::logout)
                .collect(Collectors.toList()));
    }

    public Mono<GatewayAggregate> connect(Function<Gateway, Mono<Void>> scopeFunction) {
        // login and complete on connect
        return acquire().flatMap(agg -> scopeFunction.apply(agg.getGateway())
                .thenReturn(agg)
                .doOnError(t -> agg.getGateway().notifyOnClose())
                .doOnCancel(agg.getGateway()::notifyOnClose));
    }

    private Mono<GatewayAggregate> acquire() {
        return acquire(clientFactory == null ? DefaultGatewayClient::new : clientFactory);
    }

    private Mono<GatewayAggregate> acquire(Function<O, GatewayClient> clientFactory) {
        CoreResources coreResources = client.getCoreResources();
        Mono<Integer> shardCount = initShardCount(coreResources.getRestClient());
        HttpClient httpClient = initHttpClient();
        PayloadReader reader = initPayloadReader();
        PayloadWriter writer = initPayloadWriter();
        String token = coreResources.getToken();
        StateHolder stateHolder = new StateHolder(resources.getStoreService(),
                new StoreContext(0, MessageBean.class));
        StoreInvalidator storeInvalidator = new StoreInvalidator(stateHolder);

        // get shard coordinator
        ShardCoordinator shardCoordinator = resources.getShardCoordinator();
        GatewayObserver userObserver = initGatewayObserver();

        // Prepare event dispatcher
        Function<ShardInfo, Presence> initialPresence = initInitialPresence();
        EventDispatcher eventDispatcher = initEventDispatcher();
        MonoProcessor<Void> closeProcessor = MonoProcessor.create();
        Gateway gateway = Gateway.builder()
                .setDiscordClient(client)
                .setCoreResources(coreResources)
                .setGatewayResources(resources)
                .setEventDispatcher(eventDispatcher)
                .setCloseProcessor(closeProcessor)
                .setStateHolder(stateHolder)
                .build();

        return shardCount.flatMapMany(count -> Flux.range(0, count)
                .map(index -> new ShardInfo(index, count))
                .filter(initShardFilter())
                .transform(shardCoordinator.getIdentifyOperator())
                .concatMap(shard -> Mono.<GatewayConnection>create(sink -> {
                            StatusUpdate initial = Optional.ofNullable(initialPresence.apply(shard))
                                    .map(Presence::asStatusUpdate)
                                    .orElse(null);
                            IdentifyOptions identify = new IdentifyOptions(shard.getIndex(), shard.getCount(), initial);
                            SessionInfo resume = initResumeOptions().apply(shard);
                            if (resume != null) {
                                identify.setResumeSessionId(resume.getSessionId());
                                identify.setResumeSequence(resume.getSequence());
                            }
                            Disposable.Composite forCleanup = Disposables.composite();
                            GatewayObserver observer = storeInvalidator
                                    .then((state, opts) -> {
                                        if (state.equals(GatewayObserver.CONNECTED)) {
                                            log.info("Shard {} connected", opts.getShardIndex());
                                            shardCoordinator.publishConnected(
                                                    new ShardInfo(opts.getShardIndex(), opts.getShardCount()));
                                            GatewayConnection connection = new GatewayConnection(gateway, identify);
                                            sink.success(connection);
                                        }
                                        boolean canResume = state.equals(GatewayObserver.DISCONNECTED_RESUME);
                                        if (state.equals(GatewayObserver.DISCONNECTED) || canResume) {
                                            log.info("Shard {} disconnected", opts.getShardIndex());
                                            shardCoordinator.publishDisconnected(
                                                    new ShardInfo(opts.getShardIndex(), opts.getShardCount()),
                                                    canResume ? new SessionInfo(opts.getResumeSessionId(),
                                                            opts.getResumeSequence()) : null);
                                            gateway.getGatewayClientMap().remove(opts.getShardIndex());
                                            gateway.getVoiceClientMap().remove(opts.getShardIndex());
                                            if (gateway.getGatewayClientMap().isEmpty()) {
                                                closeProcessor.onComplete();
                                                eventDispatcher.complete();
                                                forCleanup.dispose();
                                            }
                                        }
                                    })
                                    .then(userObserver);
                            O options = buildOptions(token, httpClient, reader, writer, resources.getReconnectOptions(),
                                    identify, observer, shardCoordinator.getIdentifyLimiter());
                            GatewayClient gatewayClient = clientFactory.apply(options);
                            VoiceClient voiceClient = new VoiceClient(
                                    resources.getVoiceConnectionScheduler(),
                                    coreResources.getJacksonResources().getObjectMapper(),
                                    guildId -> {
                                        VoiceStateUpdate voiceStateUpdate = new VoiceStateUpdate(
                                                guildId, null, false, false);
                                        gatewayClient.sender().next(GatewayPayload.voiceStateUpdate(voiceStateUpdate));
                                    });

                            gateway.getGatewayClientMap().put(shard.getIndex(), gatewayClient);
                            gateway.getVoiceClientMap().put(shard.getIndex(), voiceClient);

                            // wire gateway events to EventDispatcher
                            Logger dispatchLog = Loggers.getLogger("discord4j.dispatch." + shard.getIndex());
                            forCleanup.add(gatewayClient.dispatch()
                                    .log(dispatchLog, Level.FINE, false)
                                    .map(dispatch -> DispatchContext.of(dispatch, gateway, shard))
                                    .flatMap(context -> DispatchHandlers.handle(context)
                                            .onErrorResume(error -> {
                                                dispatchLog.error("Error dispatching event", error);
                                                return Mono.empty();
                                            }))
                                    .doOnNext(eventDispatcher::publish)
                                    .subscribe());

                            sink.onCancel(this.client.getCoreResources()
                                    .getRestClient()
                                    .getGatewayService()
                                    .getGateway()
                                    .transform(loginOperator(gateway, gatewayClient))
                                    .subscribe(null,
                                            t -> log.error("Gateway terminated with an error", t)));
                        })
                ))
                .collectList()
                .zipWith(Mono.just(gateway), GatewayAggregate::new);
    }

    private O buildOptions(String token, HttpClient httpClient, PayloadReader reader, PayloadWriter writer,
                           ReconnectOptions reconnectOptions, IdentifyOptions identify, GatewayObserver observer,
                           PayloadTransformer identifyLimiter) {
        GatewayOptions options = GatewayOptions.builder()
                .setToken(token)
                .setHttpClient(httpClient)
                .setPayloadReader(reader)
                .setPayloadWriter(writer)
                .setReconnectOptions(reconnectOptions)
                .setIdentifyOptions(identify)
                .setInitialObserver(observer)
                .setIdentifyLimiter(identifyLimiter)
                .build();
        return this.optionsModifier.apply(options);
    }

    private Function<Mono<GatewayResponse>, Mono<Void>> loginOperator(Gateway root, GatewayClient client) {
        return mono -> mono.flatMap(response -> client.execute(
                RouteUtils.expandQuery(response.getUrl(), getGatewayParameters()))
                .then(root.getStateHolder().invalidateStores())
                .then(resources.getStoreService().dispose()));
    }

    private Map<String, Object> getGatewayParameters() {
        final Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("compress", "zlib-stream");
        parameters.put("encoding", "json");
        parameters.put("v", 6);
        return parameters;
    }
}
