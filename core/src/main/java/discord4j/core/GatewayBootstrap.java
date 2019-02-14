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

import discord4j.common.GitProperties;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.dispatch.DispatchContext;
import discord4j.core.event.dispatch.DispatchHandlers;
import discord4j.core.event.domain.Event;
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
import discord4j.rest.RestClient;
import discord4j.rest.json.response.GatewayResponse;
import discord4j.rest.util.RouteUtils;
import discord4j.store.api.util.StoreContext;
import discord4j.voice.VoiceClient;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.extra.processor.TopicProcessor;
import reactor.netty.http.client.HttpClient;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class GatewayBootstrap {

    private static final Logger log = Loggers.getLogger(GatewayBootstrap.class);

    public static final int RECOMMENDED_SHARD_COUNT = 0;

    private final DiscordClient client;
    private final GatewayResources resources;
    private GatewayClientFactory gatewayClientFactory;
    private int shardCount = RECOMMENDED_SHARD_COUNT;
    private Predicate<ShardInfo> shardFilter;
    private Function<ShardInfo, Presence> initialPresence;
    private Function<ShardInfo, Tuple2<String, Integer>> resumeOptions;
    private PayloadReader payloadReader;
    private PayloadWriter payloadWriter;
    private HttpClient httpClient;
    private GatewayObserver gatewayObserver;
    private Scheduler eventScheduler;

    public GatewayBootstrap(DiscordClient client, GatewayResources resources) {
        this.client = client;
        this.resources = resources;
    }

    public GatewayBootstrap setGatewayClientFactory(GatewayClientFactory gatewayClientFactory) {
        this.gatewayClientFactory = gatewayClientFactory;
        return this;
    }

    public GatewayBootstrap setShardCount(int shardCount) {
        this.shardCount = shardCount;
        return this;
    }

    public GatewayBootstrap setShardFilter(Predicate<ShardInfo> shardFilter) {
        this.shardFilter = shardFilter;
        return this;
    }

    public GatewayBootstrap setInitialPresence(Function<ShardInfo, Presence> initialPresence) {
        this.initialPresence = initialPresence;
        return this;
    }

    public GatewayBootstrap setResumeOptions(Function<ShardInfo, Tuple2<String, Integer>> resumeOptions) {
        this.resumeOptions = resumeOptions;
        return this;
    }

    public GatewayBootstrap setPayloadReader(PayloadReader payloadReader) {
        this.payloadReader = payloadReader;
        return this;
    }

    public GatewayBootstrap setPayloadWriter(PayloadWriter payloadWriter) {
        this.payloadWriter = payloadWriter;
        return this;
    }

    public GatewayBootstrap setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public GatewayBootstrap setGatewayObserver(GatewayObserver gatewayObserver) {
        this.gatewayObserver = gatewayObserver;
        return this;
    }

    public GatewayBootstrap setEventScheduler(Scheduler eventScheduler) {
        this.eventScheduler = eventScheduler;
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

    private Function<ShardInfo, Tuple2<String, Integer>> initResumeOptions() {
        if (resumeOptions != null) {
            return resumeOptions;
        }
        return shard -> null;
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

    private Scheduler initEventScheduler() {
        if (eventScheduler != null) {
            return eventScheduler;
        }
        return ForkJoinPoolScheduler.create("discord4j-events");
    }

    public Mono<Tuple2<List<GatewayConnection>, GatewayAggregate>> login() {
        return acquireConnections()
                .flatMap(t2 -> Mono.just(t2)
                        .doOnError(t -> t2.getT2().notifyOnClose())
                        .doOnCancel(t2.getT2()::notifyOnClose));
    }

    public Tuple2<List<GatewayConnection>, GatewayAggregate> loginNow() {
        return login().block();
    }

    public Mono<Void> login(Function<GatewayAggregate, Mono<Void>> scopeFunction) {
        // login and complete on disconnect
        return Mono.usingWhen(acquireConnections(),
                t2 -> scopeFunction.apply(t2.getT2())
                        .then(Mono.whenDelayError(t2.getT1().stream()
                                .map(GatewayConnection::onDisconnect)
                                .collect(Collectors.toList()))),
                cleanup(),
                cleanup());
    }

    public Mono<List<GatewayConnection>> connect(Function<GatewayAggregate, Mono<Void>> scopeFunction) {
        // login and complete on connect
        return acquireConnections()
                .flatMap(t2 -> scopeFunction.apply(t2.getT2())
                        .thenReturn(t2.getT1())
                        .doOnError(t -> t2.getT2().notifyOnClose())
                        .doOnCancel(t2.getT2()::notifyOnClose));
    }

    private Function<Tuple2<List<GatewayConnection>, GatewayAggregate>, Publisher<?>> cleanup() {
        return t2 -> Mono.whenDelayError(t2.getT1().stream()
                .map(GatewayConnection::logout)
                .collect(Collectors.toList()));
    }

    private Mono<Tuple2<List<GatewayConnection>, GatewayAggregate>> acquireConnections() {
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
        TopicProcessor<Event> eventProcessor = TopicProcessor.<Event>builder()
                .name("discord4j-events")
                .autoCancel(false)
                .share(true)
                .build();
        EventDispatcher eventDispatcher = new EventDispatcher(eventProcessor, initEventScheduler());
        MonoProcessor<Void> closeProcessor = MonoProcessor.create();
        GatewayAggregate gateway = GatewayAggregate.builder()
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
                            Tuple2<String, Integer> resume = Optional.ofNullable(resumeOptions.apply(shard))
                                    .orElse(null);
                            if (resume != null) {
                                identify.setResumeSessionId(resume.getT1());
                                identify.setResumeSequence(resume.getT2());
                            }
                            GatewayObserver observer = storeInvalidator
                                    .then((state, opts) -> {
                                        if (state.equals(GatewayObserver.CONNECTED)) {
                                            log.info("Shard {} connected", opts.getShardIndex());
                                            shardCoordinator.publishConnectedEvent(
                                                    new ShardInfo(opts.getShardIndex(), opts.getShardCount()));
                                            GatewayConnection connection = new GatewayConnection(gateway, identify);
                                            sink.success(connection);
                                        }
                                        if (state.equals(GatewayObserver.DISCONNECTED)
                                                || state.equals(GatewayObserver.DISCONNECTED_RESUME)) {
                                            log.info("Shard {} disconnected", opts.getShardIndex());
                                        }
                                    })
                                    .then(userObserver);
                            GatewayOptions options = GatewayOptions.builder()
                                    .setToken(token)
                                    .setHttpClient(httpClient)
                                    .setPayloadReader(reader)
                                    .setPayloadWriter(writer)
                                    .setReconnectOptions(resources.getReconnectOptions())
                                    .setIdentifyOptions(identify)
                                    .setInitialObserver(observer)
                                    .setIdentifyLimiter(shardCoordinator.getIdentifyLimiter())
                                    .build();
                            GatewayClient gatewayClient = initGatewayClientFactory().getGatewayClient(options);
                            VoiceClient voiceClient = new VoiceClient(
                                    resources.getVoiceConnectionScheduler(),
                                    coreResources.getJacksonResources().getObjectMapper(),
                                    guildId -> {
                                        VoiceStateUpdate voiceStateUpdate = new VoiceStateUpdate(guildId, null, false,
                                                false);
                                        // TODO: refactor into event publish
                                        gatewayClient.sender().next(GatewayPayload.voiceStateUpdate(voiceStateUpdate));
                                    });

                            gateway.getGatewayClientMap().put(shard.getIndex(), gatewayClient);
                            gateway.getVoiceClientMap().put(shard.getIndex(), voiceClient);

                            // wire gateway events to EventDispatcher
                            Logger dispatchLog = Loggers.getLogger("discord4j.dispatch." + shard.getIndex());
                            gatewayClient.dispatch()
                                    .log(dispatchLog, Level.FINE, false)
                                    .map(dispatch -> DispatchContext.of(dispatch, gateway, shard))
                                    .flatMap(context -> DispatchHandlers.handle(context)
                                            .onErrorResume(error -> {
                                                dispatchLog.error("Error dispatching {}", context.getDispatch(), error);
                                                return Mono.empty();
                                            }))
                                    .subscribeWith(eventProcessor);

                            Properties properties = GitProperties.getProperties();
                            String url = properties.getProperty(GitProperties.APPLICATION_URL, "https://discord4j.com");
                            String name = properties.getProperty(GitProperties.APPLICATION_NAME, "Discord4J");
                            String version = properties.getProperty(GitProperties.APPLICATION_VERSION, "3");
                            String gitDescribe = properties.getProperty(GitProperties.GIT_COMMIT_ID_DESCRIBE, version);
                            log.info("Shard {} with {} {} ({})", shard.getIndex(), name, gitDescribe, url);

                            Mono<Void> gatewayFuture = this.client.getCoreResources()
                                    .getRestClient()
                                    .getGatewayService()
                                    .getGateway()
                                    .transform(loginOperator(gateway, gatewayClient))
                                    .doOnError(t -> complete(closeProcessor))
                                    .doOnCancel(() -> complete(closeProcessor))
                                    .doOnTerminate(() -> complete(closeProcessor));
                            sink.onCancel(gatewayFuture.subscribe(null,
                                    t -> log.error("Gateway terminated with an error", t)));
                        })
                ))
                .doOnNext(connection -> log.info("Acquired connection to {}",
                        connection.getIdentifyOptions().getShardIndex()))
                .collectList()
                .zipWith(Mono.just(gateway));
    }

    private void complete(MonoProcessor<Void> processor) {
        processor.onComplete();
    }

    private Function<Mono<GatewayResponse>, Mono<Void>> loginOperator(GatewayAggregate root, GatewayClient client) {
        return mono -> mono
                .flatMap(response -> client.execute(
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
