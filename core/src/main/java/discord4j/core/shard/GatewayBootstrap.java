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

import discord4j.common.LogUtil;
import discord4j.common.ReactorResources;
import discord4j.common.annotations.Experimental;
import discord4j.common.retry.ReconnectOptions;
import discord4j.common.store.Store;
import discord4j.common.store.action.gateway.GatewayActions;
import discord4j.common.store.legacy.LegacyStoreLayout;
import discord4j.common.util.Snowflake;
import discord4j.core.CoreResources;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.GatewayResources;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.dispatch.DispatchContext;
import discord4j.core.event.dispatch.DispatchEventMapper;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.presence.Presence;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.ActivityUpdateRequest;
import discord4j.discordjson.json.gateway.GuildMembersChunk;
import discord4j.discordjson.json.gateway.StatusUpdate;
import discord4j.gateway.*;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.gateway.limiter.PayloadTransformer;
import discord4j.gateway.limiter.RateLimitTransformer;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.GatewayStateChange;
import discord4j.gateway.state.DispatchStoreLayer;
import discord4j.gateway.state.StatefulDispatch;
import discord4j.rest.util.Multimap;
import discord4j.rest.util.RouteUtils;
import discord4j.store.jdk.JdkStoreService;
import discord4j.voice.DefaultVoiceConnectionFactory;
import discord4j.voice.VoiceConnection;
import discord4j.voice.VoiceConnectionFactory;
import discord4j.voice.VoiceReactorResources;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.*;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static discord4j.common.LogUtil.format;
import static reactor.function.TupleUtils.function;

/**
 * Builder to create a shard group connecting to Discord Gateway to produce a {@link GatewayDiscordClient}. A shard
 * group represents a set of shards for a given bot that will share some key resources like entity caching and event
 * dispatching. Defaults to creating an automatic sharding group using all shards up to the recommended amount. Refer
 * to each setter for more details about the default values for each configuration. Some of the commonly used ones are:
 * <ul>
 *     <li>Setting the number of shards to connect through the
 *     {@link #setSharding(ShardingStrategy)} method.</li>
 *     <li>Setting the initial status of the bot depending on the shard, through
 *     {@link #setInitialStatus(Function)}</li>
 *     <li>Customize the entity cache using {@link #setStore(Store)}</li>
 * </ul>
 * <p>
 * One of the following methods must be subscribed to in order to begin establishing Discord Gateway connections:
 * <ul>
 *     <li>{@link #login()} to obtain a {@link Mono} for a {@link GatewayDiscordClient} that can be externally
 *     managed.</li>
 *     <li>{@link #login(Function)} to customize the {@link GatewayClient} instances to build.</li>
 *     <li>{@link #withGateway(Function)} to work with the {@link GatewayDiscordClient} in a scoped way, providing
 *     a mapping function that will close and release all resources on disconnection.</li>
 * </ul>
 * This bootstrap emits a result depending on the configuration of {@link #setAwaitConnections(boolean)}.
 *
 * @param <O> the configuration flavor supplied to the {@link GatewayClient} instances to be built.
 */
public class GatewayBootstrap<O extends GatewayOptions> {

    private static final Logger log = Loggers.getLogger(GatewayBootstrap.class);

    private final DiscordClient client;
    private final Function<GatewayOptions, O> optionsModifier;

    private ShardingStrategy shardingStrategy = ShardingStrategy.recommended();
    private Boolean awaitConnections = null;
    private ShardCoordinator shardCoordinator = null;
    private EventDispatcher eventDispatcher = null;
    private Store store = null;
    private MemberRequestFilter memberRequestFilter = null;
    private Function<ShardInfo, StatusUpdate> initialPresence = shard -> null;
    private Function<ShardInfo, SessionInfo> resumeOptions = shard -> null;
    private IntentSet intents = IntentSet.nonPrivileged();
    private Boolean guildSubscriptions = null;
    private Function<GatewayDiscordClient, Mono<Void>> destroyHandler = shutdownDestroyHandler();
    private PayloadReader payloadReader = null;
    private PayloadWriter payloadWriter = null;
    private ReconnectOptions reconnectOptions = null;
    private ReconnectOptions voiceReconnectOptions = null;
    private GatewayObserver gatewayObserver = GatewayObserver.NOOP_LISTENER;
    private Function<ReactorResources, GatewayReactorResources> gatewayReactorResources = null;
    private Function<ReactorResources, VoiceReactorResources> voiceReactorResources = null;
    private VoiceConnectionFactory voiceConnectionFactory = defaultVoiceConnectionFactory();
    private EntityRetrievalStrategy entityRetrievalStrategy = null;
    private DispatchEventMapper dispatchEventMapper = null;
    private int maxMissedHeartbeatAck = 1;
    private Function<EventDispatcher, Publisher<?>> dispatcherFunction;

    /**
     * Create a default {@link GatewayBootstrap} based off the given {@link DiscordClient} that provides an instance
     * of {@link CoreResources} used to provide defaults while building a {@link GatewayDiscordClient}.
     *
     * @param client the {@link DiscordClient} used to set up configuration
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
        this.shardingStrategy = source.shardingStrategy;
        this.awaitConnections = source.awaitConnections;
        this.shardCoordinator = source.shardCoordinator;
        this.eventDispatcher = source.eventDispatcher;
        this.store = source.store;
        this.memberRequestFilter = source.memberRequestFilter;
        this.initialPresence = source.initialPresence;
        this.resumeOptions = source.resumeOptions;
        this.intents = source.intents;
        this.guildSubscriptions = source.guildSubscriptions;
        this.destroyHandler = source.destroyHandler;
        this.payloadReader = source.payloadReader;
        this.payloadWriter = source.payloadWriter;
        this.reconnectOptions = source.reconnectOptions;
        this.voiceReconnectOptions = source.voiceReconnectOptions;
        this.gatewayObserver = source.gatewayObserver;
        this.gatewayReactorResources = source.gatewayReactorResources;
        this.voiceReactorResources = source.voiceReactorResources;
        this.voiceConnectionFactory = source.voiceConnectionFactory;
        this.entityRetrievalStrategy = source.entityRetrievalStrategy;
        this.dispatchEventMapper = source.dispatchEventMapper;
        this.maxMissedHeartbeatAck = source.maxMissedHeartbeatAck;
        this.dispatcherFunction = source.dispatcherFunction;
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
     * Set the sharding method to use while building a {@link GatewayDiscordClient}. Defaults to creating all shards
     * given by the recommended amount from Discord, which is equivalent to {@link ShardingStrategy#recommended()}.
     * Built-in factories like {@link ShardingStrategy#fixed(int)} to use a predefined number of shards, or customize
     * the strategy using {@link ShardingStrategy#builder()}.
     * <p>
     * For example, it is possible to define the {@code shardCount} parameter independently from the number of shards
     * to create and connect to Gateway by using:
     * <pre>
     * .setSharding(ShardingStrategy.builder()
     *                 .indices(0, 2, 4)
     *                 .count(6)
     *                 .build())
     * </pre>
     * Would only connect shards 0, 2 and 4 while still indicating that your bot guilds are split across 6 shards.
     *
     * @param shardingStrategy a strategy to use while sharding the connections to Discord Gateway
     * @return this builder
     */
    public GatewayBootstrap<O> setSharding(ShardingStrategy shardingStrategy) {
        this.shardingStrategy = shardingStrategy;
        return this;
    }

    /**
     * Set if the connect {@link Mono} should defer completion until all joining shards have connected. Defaults to
     * {@code true} if running a single shard, otherwise {@code false}.
     *
     * @param awaitConnections {@code true} if connect should wait until all joining shards have connected before
     * completing, or {@code false} to complete immediately
     * @return this builder
     */
    public GatewayBootstrap<O> setAwaitConnections(boolean awaitConnections) {
        this.awaitConnections = awaitConnections;
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
     * all subscribers. Defaults to using {@link EventDispatcher#buffering()} which buffers all events until the
     * first subscriber subscribes to the dispatcher.
     *
     * @param eventDispatcher an externally managed {@link EventDispatcher} to publish events
     * @return this builder
     */
    public GatewayBootstrap<O> setEventDispatcher(@Nullable EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        return this;
    }

    /**
     * Set a custom {@link Store} to cache Gateway updates.
     *
     * @param store an externally managed {@link Store} to receive Gateway updates
     * @return this builder
     */
    public GatewayBootstrap<O> setStore(@Nullable Store store) {
        this.store = store;
        return this;
    }

    /**
     * Set a {@link MemberRequestFilter} to determine how this shard group should request guild members. The provided
     * filter is applied on each GUILD_CREATE payload and if returns {@code true}, members will be requested for the
     * given guild. Defaults to loading members from all large guilds immediately after a GUILD_CREATE.
     *
     * @param memberRequestFilter the filter indicating how to load guild members
     * @return this builder
     * @see <a href="https://discord.com/developers/docs/topics/gateway#request-guild-members">Request Guild Members</a>
     */
    public GatewayBootstrap<O> setMemberRequestFilter(MemberRequestFilter memberRequestFilter) {
        this.memberRequestFilter = memberRequestFilter;
        return this;
    }

    /**
     * Set a custom {@link Function handler} that generate a destroy sequence to be run once all joining shards have
     * disconnected, after all internal resources have been released. The destroy procedure is applied asynchronously
     * and errors are logged and swallowed. Defaults to {@link GatewayBootstrap#shutdownDestroyHandler()} that will
     * release the set {@link EventDispatcher}.
     *
     * @param destroyHandler the {@link Function} supplying a {@link Mono} to reset state
     * @return this builder
     */
    public GatewayBootstrap<O> setDestroyHandler(Function<GatewayDiscordClient, Mono<Void>> destroyHandler) {
        this.destroyHandler = Objects.requireNonNull(destroyHandler, "destroyHandler");
        return this;
    }

    /**
     * Set a {@link Function} to determine the {@link StatusUpdate} that each joining shard should use when identifying
     * to the Gateway. Defaults to no status given.
     * <p>
     * {@link StatusUpdate} instances can be built through factories in {@link Presence}:
     * <ul>
     *     <li>{@link Presence#online()} and {@link Presence#online(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#idle()} and {@link Presence#idle(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#doNotDisturb()} and {@link Presence#doNotDisturb(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#invisible()}</li>
     * </ul>
     *
     * @param initialPresence a {@link Function} that supplies {@link StatusUpdate} instances from a given
     * {@link ShardInfo}
     * @return this builder
     * @deprecated use {@link #setInitialStatus(Function)}
     */
    @Deprecated
    public GatewayBootstrap<O> setInitialPresence(Function<ShardInfo, StatusUpdate> initialPresence) {
        this.initialPresence = Objects.requireNonNull(initialPresence, "initialPresence");
        return this;
    }

    /**
     * Set a {@link Function} to determine the {@link StatusUpdate} that each joining shard should use when identifying
     * to the Gateway. Defaults to no status given.
     * <p>
     * {@link StatusUpdate} instances can be built through factories in {@link Presence}:
     * <ul>
     *     <li>{@link Presence#online()} and {@link Presence#online(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#idle()} and {@link Presence#idle(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#doNotDisturb()} and {@link Presence#doNotDisturb(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#invisible()}</li>
     * </ul>
     *
     * @param initialStatus a {@link Function} that supplies {@link StatusUpdate} instances from a given
     * {@link ShardInfo}
     * @return this builder
     */
    public GatewayBootstrap<O> setInitialStatus(Function<ShardInfo, StatusUpdate> initialStatus) {
        this.initialPresence = Objects.requireNonNull(initialStatus, "initialStatus");
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
     * Set the intents to subscribe from the gateway for this shard. Using this method is mutually exclusive from
     * {@link #setDisabledIntents(IntentSet)}. Defaults to {@link IntentSet#nonPrivileged()}.
     * <p>
     * When using intents, make sure you understand their effect on your bot, to avoid issues specially when working
     * around members. Depend on {@link GatewayDiscordClient#requestMembers(Snowflake, Set)} and
     * {@link Guild#requestMembers(Set)} to fetch members directly. Methods that return {@link Member} can lazily fetch
     * entities from the Gateway if missing, according to the configured
     * {@link #setEntityRetrievalStrategy(EntityRetrievalStrategy)}.
     * <p>
     * Members can also be requested eagerly on startup, using {@link #setMemberRequestFilter(MemberRequestFilter)},
     * either for select guilds or all of them, provided you are able to request the entire list of members in a guild.
     * <p>
     * The following scenarios are affected by this feature:
     * <ul>
     *     <li><strong>Fetching guild members:</strong> if you don't enable {@link Intent#GUILD_MEMBERS} you will not
     *     be able to request the entire list of members in a guild.</li>
     *     <li><strong>Fetching guild presences:</strong> if you don't enable {@link Intent#GUILD_PRESENCES} you will
     *     not receive user activity and status information. In addition, you will not receive the initial guild
     *     member list, which includes all members for small guilds and online members + members without roles for
     *     larger guilds.</li>
     *     <li><strong>Establishing voice connections:</strong> if you don't enable {@link Intent#GUILD_VOICE_STATES}
     *     you will not be able to connect to a voice channel.</li>
     * </ul>
     *
     * @param intents set of intents to subscribe
     * @return this builder
     */
    public GatewayBootstrap<O> setEnabledIntents(IntentSet intents) {
        this.intents = Objects.requireNonNull(intents);
        return this;
    }

    /**
     * Set the intents which should not be subscribed from the gateway for this shard. This method computes by
     * {@link IntentSet#all()} minus the provided intents. Using this method is mutually exclusive from
     * {@link #setEnabledIntents(IntentSet)}.
     * <p>
     * When using intents, make sure you understand their effect on your bot, to avoid issues specially when working
     * around members. Depend on {@link GatewayDiscordClient#requestMembers(Snowflake, Set)} and
     * {@link Guild#requestMembers(Set)} to fetch members directly. Methods that return {@link Member} can lazily fetch
     * entities from the Gateway if missing, according to the configured
     * {@link #setEntityRetrievalStrategy(EntityRetrievalStrategy)}.
     * <p>
     * Members can also be requested eagerly on startup, using {@link #setMemberRequestFilter(MemberRequestFilter)},
     * either for select guilds or all of them, provided you are able to request the entire list of members in a guild.
     * <p>
     * The following scenarios are affected by this feature:
     * <ul>
     *     <li><strong>Fetching guild members:</strong> if you disable {@link Intent#GUILD_MEMBERS} you will not
     *     be able to request the entire list of members in a guild.</li>
     *     <li><strong>Fetching guild presences:</strong> if you disable {@link Intent#GUILD_PRESENCES} you will
     *     not receive user activity and status information. In addition, you will not receive the initial guild
     *     member list, which includes all members for small guilds and online members + members without roles for
     *     larger guilds.</li>
     *     <li><strong>Establishing voice connections:</strong> if you disable {@link Intent#GUILD_VOICE_STATES}
     *     you will not be able to connect to a voice channel.</li>
     * </ul>
     *
     * @param intents set of intents which should not be subscribed
     * @return this builder
     */
    public GatewayBootstrap<O> setDisabledIntents(IntentSet intents) {
        this.intents = IntentSet.all().andNot(Objects.requireNonNull(intents));
        return this;
    }

    /**
     * Set if this shard group will subscribe to presence and typing events. Defaults to {@code true}.
     *
     * @param guildSubscriptions whether to enable or disable guild subscriptions
     * @return this builder
     * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-subscriptions">Guild Subscriptions</a>
     * @deprecated Discord recommends you migrate to Gateway Intents as they supersede this setting
     */
    public GatewayBootstrap<O> setGuildSubscriptions(boolean guildSubscriptions) {
        this.guildSubscriptions = guildSubscriptions;
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
     * time a websocket session is closed unexpectedly.
     *
     * @param reconnectOptions a {@link ReconnectOptions} policy to use in Gateway connections
     * @return this builder
     */
    public GatewayBootstrap<O> setReconnectOptions(ReconnectOptions reconnectOptions) {
        this.reconnectOptions = Objects.requireNonNull(reconnectOptions);
        return this;
    }

    /**
     * Set a custom {@link ReconnectOptions} to configure how Voice Gateway connections will attempt to reconnect every
     * time a websocket session is closed unexpectedly.
     *
     * @param voiceReconnectOptions a {@link ReconnectOptions} policy to use in Voice Gateway connections
     * @return this builder
     */
    public GatewayBootstrap<O> setVoiceReconnectOptions(ReconnectOptions voiceReconnectOptions) {
        this.voiceReconnectOptions = Objects.requireNonNull(voiceReconnectOptions);
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
     * Customize the {@link ReactorResources} used exclusively for Gateway-related operations, such as maintaining
     * the websocket connections and scheduling Gateway tasks. Defaults to using the parent {@link ReactorResources}
     * inherited from {@link DiscordClient}.
     *
     * @param gatewayReactorResources a {@link ReactorResources} object for Gateway operations
     * @return this builder
     */
    public GatewayBootstrap<O> setGatewayReactorResources(Function<ReactorResources, GatewayReactorResources> gatewayReactorResources) {
        this.gatewayReactorResources = Objects.requireNonNull(gatewayReactorResources);
        return this;
    }

    /**
     * Customize the {@link ReactorResources} used exclusively for voice-related operations, such as maintaining
     * the Voice Gateway websocket connections, Voice UDP socket connections and scheduling Gateway tasks. Defaults
     * to using the parent {@link ReactorResources} inherited from {@link DiscordClient}.
     *
     * @param voiceReactorResources a {@link ReactorResources} object for voice operations
     * @return this builder
     */
    public GatewayBootstrap<O> setVoiceReactorResources(Function<ReactorResources, VoiceReactorResources> voiceReactorResources) {
        this.voiceReactorResources = Objects.requireNonNull(voiceReactorResources);
        return this;
    }

    /**
     * Customize the {@link VoiceConnectionFactory} used to establish and maintain {@link VoiceConnection} instances to
     * perform voice-related operations. Defaults to {@link #defaultVoiceConnectionFactory()}.
     *
     * @param voiceConnectionFactory a factory that can create {@link VoiceConnection} instances.
     * @return this builder
     */
    public GatewayBootstrap<O> setVoiceConnectionFactory(VoiceConnectionFactory voiceConnectionFactory) {
        this.voiceConnectionFactory = Objects.requireNonNull(voiceConnectionFactory);
        return this;
    }

    /**
     * Customize the {@link EntityRetrievalStrategy} to use by default in order to retrieve Discord entities. Defaults
     * to {@link EntityRetrievalStrategy#STORE_FALLBACK_REST}.
     *
     * @param entityRetrievalStrategy a strategy to use to retrieve entities
     * @return this builder
     */
    public GatewayBootstrap<O> setEntityRetrievalStrategy(@Nullable EntityRetrievalStrategy entityRetrievalStrategy) {
        this.entityRetrievalStrategy = entityRetrievalStrategy;
        return this;
    }

    /**
     * Customize the {@link DispatchEventMapper} used to convert Gateway Dispatch into {@link Event} instances.
     * Defaults to using {@link DispatchEventMapper#emitEvents()} that will process payloads and save its updates to
     * the appropriate {@link Store}, then generate the right {@link Event} instance.
     *
     * @param dispatchEventMapper a factory to derive {@link Event Events} from Gateway
     * @return this builder
     */
    public GatewayBootstrap<O> setDispatchEventMapper(DispatchEventMapper dispatchEventMapper) {
        this.dispatchEventMapper = Objects.requireNonNull(dispatchEventMapper);
        return this;
    }

    /**
     * Set the maximum number of missed heartbeat acknowledge payloads each connection to Gateway will allow before
     * triggering an automatic reconnect. A missed acknowledge is counted if a client does not receive a heartbeat
     * ACK between its attempts at sending heartbeats. Defaults to 1.
     *
     * @param maxMissedHeartbeatAck a non-negative number representing the maximum number of allowed
     * @return this builder
     */
    public GatewayBootstrap<O> setMaxMissedHeartbeatAck(int maxMissedHeartbeatAck) {
        this.maxMissedHeartbeatAck = Math.max(0, maxMissedHeartbeatAck);
        return this;
    }

    /**
     * Set an initial subscriber to the bootstrapped {@link EventDispatcher} to gain access to early startup events. The
     * subscriber is derived from the given {@link Function} which returns a {@link Publisher} that is subscribed early
     * in the Gateway connection process.
     * <p>
     * Errors emitted from the given {@code dispatcherFunction} will instruct a logout procedure to disconnect from the
     * Gateway.
     *
     * @param dispatcherFunction an {@link EventDispatcher} mapper that derives an asynchronous listener
     * @return this builder
     */
    @Experimental
    public GatewayBootstrap<O> withEventDispatcher(Function<EventDispatcher, Publisher<?>> dispatcherFunction) {
        this.dispatcherFunction = Objects.requireNonNull(dispatcherFunction);
        return this;
    }

    /**
     * Connect to the Discord Gateway upon subscription to acquire a {@link GatewayDiscordClient} instance and use it
     * in a declarative way, releasing the object once the derived usage {@link Function} completes, and the underlying
     * shard group disconnects, according to {@link GatewayDiscordClient#onDisconnect()}.
     * <p>
     * The timing of acquiring a {@link GatewayDiscordClient} depends on the {@link #setAwaitConnections(boolean)}
     * setting: if {@code true}, when all joining shards have connected; if {@code false}, as soon as it is possible to
     * establish a connection to the Gateway.
     * <p>
     * Calling this method is useful when you operate on the {@link GatewayDiscordClient} object using reactive API you
     * can compose within the scope of the given {@link Function}.
     *
     * @param whileConnectedFunction the {@link Function} to apply the <strong>connected</strong>
     * {@link GatewayDiscordClient} and trigger a processing pipeline from it.
     * @return an empty {@link Mono} completing after all resources have released
     */
    public Mono<Void> withGateway(Function<GatewayDiscordClient, Publisher<?>> whileConnectedFunction) {
        return usingConnection(gateway -> Flux.from(whileConnectedFunction.apply(gateway)).then(gateway.onDisconnect()));
    }

    private <T> Mono<T> usingConnection(Function<GatewayDiscordClient, Mono<T>> onConnectedFunction) {
        return Mono.usingWhen(login(), onConnectedFunction, GatewayDiscordClient::logout);
    }

    /**
     * Connect to the Discord Gateway upon subscription to build a {@link GatewayClient} from the set of options
     * configured by this builder. The resulting {@link GatewayDiscordClient} can be externally managed, leaving you
     * in charge of properly releasing its resources by calling {@link GatewayDiscordClient#logout()}.
     * <p>
     * The timing of acquiring a {@link GatewayDiscordClient} depends on the {@link #setAwaitConnections(boolean)}
     * setting: if {@code true}, when all joining shards have connected; if {@code false}, as soon as it is possible
     * to establish a connection to the Gateway.
     * <p>
     * All joining shards will attempt to serially connect to Discord Gateway, coordinated by the current
     * {@link ShardCoordinator}. If one of the shards fail to connect due to a retryable problem like invalid session
     * it will retry before continuing to the next one.
     *
     * @return a {@link Mono} that upon subscription and depending on the configuration of
     * {@link #setAwaitConnections(boolean)}, emits a {@link GatewayDiscordClient}. If an error occurs during the setup
     * sequence, it will be emitted through the {@link Mono}.
     */
    public Mono<GatewayDiscordClient> login() {
        return login(DefaultGatewayClient::new);
    }

    /**
     * Connect to the Discord Gateway upon subscription using a custom {@link Function factory} to build a
     * {@link GatewayClient} from the set of options configured by this builder. See {@link #login()} for more details
     * about how the returned {@link Mono} operates.
     *
     * @return a {@link Mono} that upon subscription and depending on the configuration of
     * {@link #setAwaitConnections(boolean)}, emits a {@link GatewayDiscordClient}. If an error occurs during the setup
     * sequence, it will be emitted through the {@link Mono}.
     */
    public Mono<GatewayDiscordClient> login(Function<O, GatewayClient> clientFactory) {
        return Mono.fromCallable(() -> new GatewayBootstrap<>(this, this.optionsModifier))
                .zipWhen(b -> b.shardingStrategy.getShardCount(b.client))
                .flatMap(function((b, count) -> {
                    Store store = b.initStore();
                    EventDispatcher eventDispatcher = b.initEventDispatcher();
                    GatewayReactorResources gatewayReactorResources = b.initGatewayReactorResources();
                    ShardCoordinator shardCoordinator = b.initShardCoordinator(gatewayReactorResources);

                    VoiceReactorResources voiceReactorResources = b.initVoiceReactorResources();
                    GatewayResources resources = new GatewayResources(store, eventDispatcher, shardCoordinator,
                            b.initMemberRequestFilter(b.intents), gatewayReactorResources,
                            b.initVoiceReactorResources(),
                            b.initReconnectOptions(voiceReactorResources), b.intents);
                    Sinks.Empty<Void> onCloseSink = Sinks.empty();
                    AtomicReference<Throwable> dispatcherFunctionError = new AtomicReference<>();
                    EntityRetrievalStrategy entityRetrievalStrategy = b.initEntityRetrievalStrategy();
                    DispatchEventMapper dispatchMapper = b.initDispatchEventMapper();
                    Set<String> completingChunkNonces = ConcurrentHashMap.newKeySet();

                    GatewayClientGroupManager clientGroup = b.shardingStrategy.getGroupManager(count);
                    GatewayDiscordClient gateway = new GatewayDiscordClient(b.client, resources, onCloseSink.asMono(),
                            clientGroup, b.voiceConnectionFactory, entityRetrievalStrategy, completingChunkNonces);
                    Mono<Void> destroySequence = Mono.deferContextual(ctx -> b.destroyHandler.apply(gateway)
                            .doFinally(s -> {
                                log.info(format(ctx, "All shards disconnected"));
                                Throwable t = dispatcherFunctionError.get();
                                if (t != null) {
                                    onCloseSink.emitError(t, Sinks.EmitFailureHandler.FAIL_FAST);
                                } else {
                                    onCloseSink.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST);
                                }
                            }))
                            .cache();

                    Flux<ShardInfo> connections = b.shardingStrategy.getMaxConcurrency(b.client)
                            .flatMapMany(maxConcurrency -> b.shardingStrategy.getShards(count)
                                .groupBy(shard -> shard.getIndex() % maxConcurrency)
                                .flatMap(group -> group.concatMap(shard -> acquireConnection(b, shard, clientFactory,
                                        gateway, shardCoordinator, store, eventDispatcher, clientGroup,
                                        onCloseSink, dispatchMapper, completingChunkNonces,
                                        destroySequence.contextWrite(buildContext(gateway, shard)), maxConcurrency))));

                    Supplier<Mono<Void>> withEventDispatcherFunction = () ->
                            Flux.from(b.dispatcherFunction.apply(eventDispatcher))
                                    .then()
                                    .subscribeOn(gatewayReactorResources.getBlockingTaskScheduler())
                                    .onErrorResume(t -> {
                                        log.warn("Error in specified withEventDispatcher function. " +
                                                "Handle this error to avoid terminating this connection.", t);
                                        dispatcherFunctionError.set(t);
                                        return gateway.logout();
                                    });

                    Function<MonoSink<GatewayDiscordClient>, Flux<ShardInfo>> onFirstConnection =
                            sink -> connections.switchOnFirst((first, flux) -> {
                                if (first.hasValue()) {
                                    sink.success(gateway);
                                } else if (first.hasError()) {
                                    sink.error(Objects.requireNonNull(first.getThrowable()));
                                }
                                return flux;
                            });

                    if (b.awaitConnections == null ? count == 1 : b.awaitConnections) {
                        if (b.dispatcherFunction != null) {
                            return Mono.create(sink -> {
                                Disposable.Composite cleanup = Disposables.composite();
                                // subscribe to the dispatcher function
                                cleanup.add(withEventDispatcherFunction.get()
                                        .subscribe(null, t -> log.warn("Error terminating Gateway connection", t)));
                                // tie the connections Flux completion to the completion/error of this MonoSink
                                cleanup.add(connections.then(Mono.just(gateway))
                                        .subscribe(sink::success, sink::error));
                                sink.onCancel(cleanup);
                            });
                        }
                        // tie the connections Flux completion to the completion/error of this MonoSink
                        return connections.then(Mono.just(gateway));
                    } else {
                        if (b.dispatcherFunction != null) {
                            return Mono.create(sink -> {
                                Disposable.Composite cleanup = Disposables.composite();
                                // subscribe to the dispatcher function
                                cleanup.add(withEventDispatcherFunction.get()
                                        .subscribe(null, t -> log.warn("Error terminating Gateway connection", t)));
                                // tie the connections Flux first signal to the completion/error of this MonoSink
                                cleanup.add(onFirstConnection.apply(sink)
                                        .subscribe(null, t -> log.warn("Error in connections function", t)));
                                sink.onCancel(cleanup);
                            });
                        }
                        return Mono.create(sink ->
                                // tie the connections Flux first signal to the completion/error of this MonoSink
                                sink.onCancel(onFirstConnection.apply(sink)
                                        .subscribe(null, t -> log.warn("Error in connections function", t)))
                        );
                    }
                }));
    }

    private Mono<ShardInfo> acquireConnection(GatewayBootstrap<O> b,
                                              ShardInfo shard,
                                              Function<O, GatewayClient> clientFactory,
                                              GatewayDiscordClient gateway,
                                              ShardCoordinator shardCoordinator,
                                              Store store,
                                              EventDispatcher eventDispatcher,
                                              GatewayClientGroupManager clientGroup,
                                              Sinks.Empty<Void> onCloseSink,
                                              DispatchEventMapper dispatchMapper,
                                              Set<String> completingChunkNonces,
                                              Mono<Void> destroySequence,
                                              int maxConcurrency) {
        return Mono.deferContextual(ctx ->
                Mono.<ShardInfo>create(sink -> {
                    StatusUpdate initial = Optional.ofNullable(b.initialPresence.apply(shard)).orElse(null);
                    IdentifyOptions identify = IdentifyOptions.builder(shard)
                            .initialStatus(initial)
                            .intents(b.intents)
                            .guildSubscriptions(b.guildSubscriptions)
                            .resumeSession(b.resumeOptions.apply(shard))
                            .build();
                    PayloadTransformer limiter = shardCoordinator.getIdentifyLimiter(shard, maxConcurrency);
                    GatewayReactorResources resources = gateway.getGatewayResources().getGatewayReactorResources();
                    ReconnectOptions reconnectOptions = initReconnectOptions(resources);
                    GatewayOptions options = new GatewayOptions(client.getCoreResources().getToken(),
                            resources, initPayloadReader(), initPayloadWriter(), reconnectOptions,
                            identify, gatewayObserver, limiter, maxMissedHeartbeatAck);
                    GatewayClient gatewayClient = clientFactory.apply(this.optionsModifier.apply(options));
                    clientGroup.add(shard.getIndex(), gatewayClient);
                    DispatchStoreLayer dispatchStoreLayer = DispatchStoreLayer.create(store, shard);

                    // wire gateway events to EventDispatcher
                    Disposable.Composite forCleanup = Disposables.composite();
                    forCleanup.add(gatewayClient.dispatch()
                            .takeUntilOther(onCloseSink.asMono())
                            .checkpoint("Read payload from gateway")
                            .flatMap(dispatchStoreLayer::store)
                            .checkpoint("Write gateway update to the store")
                            .flatMap(statefulDispatch -> {
                                if (!(statefulDispatch.getDispatch() instanceof GuildMembersChunk)) {
                                    return Mono.just(statefulDispatch);
                                }
                                GuildMembersChunk chunk = (GuildMembersChunk) statefulDispatch.getDispatch();
                                return Mono.justOrEmpty(chunk.nonce().toOptional())
                                        .filter(nonce -> chunk.chunkIndex() + 1 == chunk.chunkCount()
                                                && completingChunkNonces.remove(nonce))
                                        .flatMap(nonce -> Mono.from(store.execute(GatewayActions
                                                .completeGuildMembers(Snowflake.asLong(chunk.guildId())))))
                                        .<StatefulDispatch<?, ?>>thenReturn(statefulDispatch)
                                        .onErrorResume(t -> {
                                            log.warn(format(ctx, "Error sending completeGuildMembers to the store"), t);
                                            return Mono.just(statefulDispatch);
                                        });
                            })
                            .flatMap(statefulDispatch -> {
                                DispatchContext<?, ?> context = DispatchContext.of(statefulDispatch, gateway);
                                return dispatchMapper.handle(context)
                                        .contextWrite(c -> c.put(LogUtil.KEY_SHARD_ID,
                                                context.getShardInfo().getIndex()))
                                        .onErrorResume(error -> {
                                            log.error(format(ctx, "Error dispatching event"), error);
                                            return Mono.empty();
                                        });
                            })
                            .doOnNext(eventDispatcher::publish)
                            .subscribe(null,
                                    t -> log.error(format(ctx, "Event mapper terminated with an error"), t),
                                    () -> log.debug(format(ctx, "Event mapper completed"))));

                    // wire internal shard coordinator events
                    // TODO: migrate to GatewayClient::stateEvents
                    forCleanup.add(gatewayClient.dispatch()
                            .ofType(GatewayStateChange.class)
                            .takeUntilOther(onCloseSink.asMono())
                            .flatMap(event -> {
                                SessionInfo session = null;
                                switch (event.getState()) {
                                    case CONNECTED:
                                    case RETRY_SUCCEEDED:
                                        // TODO: ensure this sink is only pushed to once and avoid dropping signals
                                        return shardCoordinator.publishConnected(shard)
                                                .doFinally(__ -> sink.success(shard));
                                    case DISCONNECTED_RESUME:
                                        session = SessionInfo.create(gatewayClient.getSessionId(),
                                                gatewayClient.getSequence());
                                    case DISCONNECTED:
                                        return shardCoordinator.publishDisconnected(shard, session)
                                                .then(Mono.fromRunnable(() -> clientGroup.remove(shard.getIndex())))
                                                .then(shardCoordinator.getConnectedCount()
                                                        .filter(count -> count == 0)
                                                        .flatMap(__ -> destroySequence))
                                                .onErrorResume(t -> {
                                                    log.warn(format(ctx, "Error while releasing resources"), t);
                                                    return Mono.empty();
                                                });
                                    case RETRY_FAILED:
                                        log.debug(format(ctx, "Invalidating stores for shard"));
                                }
                                return Mono.empty();
                            })
                            .contextWrite(buildContext(gateway, shard))
                            .subscribe(null,
                                    t -> log.error(format(ctx, "Lifecycle listener terminated with an error"), t),
                                    () -> log.debug(format(ctx, "Lifecycle listener completed"))));

                    forCleanup.add(b.client.getGatewayService()
                            .getGateway()
                            .doOnSubscribe(s -> log.debug(format(ctx, "Acquiring gateway endpoint")))
                            .retryWhen(Retry.backoff(
                                    reconnectOptions.getMaxRetries(), reconnectOptions.getFirstBackoff())
                                    .maxBackoff(reconnectOptions.getMaxBackoffInterval()))
                            .flatMap(response -> gatewayClient.execute(
                                    RouteUtils.expandQuery(response.url(), getGatewayParameters())))
                            .doOnError(sink::error) // only useful for startup errors
                            .doFinally(__ -> {
                                sink.success(); // no-op if we completed it before
                                onCloseSink.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST);
                            })
                            .contextWrite(buildContext(gateway, shard))
                            .subscribe(null,
                                    t -> log.debug(format(ctx, "Gateway terminated with an error: {}"), t.toString()),
                                    () -> log.debug(format(ctx, "Gateway completed"))));

                    sink.onCancel(forCleanup);
                }))
                .contextWrite(buildContext(gateway, shard));
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

    private ReconnectOptions initReconnectOptions(GatewayReactorResources resources) {
        if (reconnectOptions != null) {
            return reconnectOptions;
        }
        return ReconnectOptions.builder()
                .setBackoffScheduler(resources.getTimerTaskScheduler())
                .build();
    }

    private ReconnectOptions initReconnectOptions(VoiceReactorResources resources) {
        if (reconnectOptions != null) {
            return reconnectOptions;
        }
        return ReconnectOptions.builder()
                .setBackoffScheduler(resources.getTimerTaskScheduler())
                .build();
    }

    private GatewayReactorResources initGatewayReactorResources() {
        if (gatewayReactorResources == null) {
            gatewayReactorResources = GatewayReactorResources::new;
        }
        return gatewayReactorResources.apply(client.getCoreResources().getReactorResources());
    }

    private VoiceReactorResources initVoiceReactorResources() {
        if (voiceReactorResources == null) {
            voiceReactorResources = VoiceReactorResources::new;
        }
        return voiceReactorResources.apply(client.getCoreResources().getReactorResources());
    }

    private EventDispatcher initEventDispatcher() {
        if (eventDispatcher != null) {
            return eventDispatcher;
        }
        return EventDispatcher.buffering();
    }

    private ShardCoordinator initShardCoordinator(ReactorResources reactorResources) {
        if (shardCoordinator != null) {
            return shardCoordinator;
        }
        return LocalShardCoordinator.create(() ->
                new RateLimitTransformer(1, Duration.ofSeconds(6), reactorResources.getTimerTaskScheduler()));
    }

    private EntityRetrievalStrategy initEntityRetrievalStrategy() {
        if (entityRetrievalStrategy != null) {
            return entityRetrievalStrategy;
        }
        return EntityRetrievalStrategy.STORE_FALLBACK_REST;
    }

    private DispatchEventMapper initDispatchEventMapper() {
        if (dispatchEventMapper != null) {
            return dispatchEventMapper;
        }
        return DispatchEventMapper.emitEvents();
    }

    private Store initStore() {
        if (store != null) {
            return store;
        }
        return Store.fromLayout(LegacyStoreLayout.of(new JdkStoreService()));
    }

    private MemberRequestFilter initMemberRequestFilter(IntentSet intents) {
        if (memberRequestFilter != null) {
            return memberRequestFilter;
        } else if (intents.contains(Intent.GUILD_MEMBERS)) {
            return MemberRequestFilter.withLargeGuilds();
        } else {
            return MemberRequestFilter.none();
        }
    }

    private Multimap<String, Object> getGatewayParameters() {
        final Multimap<String, Object> parameters = new Multimap<>(3);
        parameters.add("compress", "zlib-stream");
        parameters.add("encoding", "json");
        parameters.add("v", 8);
        return parameters;
    }

    /**
     * Destroy handler that doesn't perform any cleanup task.
     *
     * @return a noop destroy handler
     */
    public static Function<GatewayDiscordClient, Mono<Void>> noopDestroyHandler() {
        return gateway -> Mono.empty();
    }


    /**
     * Destroy handler that calls {@link EventDispatcher#shutdown()} asynchronously.
     *
     * @return a shutdown destroy handler
     */
    public static Function<GatewayDiscordClient, Mono<Void>> shutdownDestroyHandler() {
        return gateway -> {
            gateway.getEventDispatcher().shutdown();
            return Mono.empty();
        };
    }

    /**
     * Create a {@link VoiceConnectionFactory} with reconnecting capabilities.
     *
     * @return a default {@link VoiceConnectionFactory}
     */
    public static VoiceConnectionFactory defaultVoiceConnectionFactory() {
        return new DefaultVoiceConnectionFactory();
    }

}
