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

import discord4j.common.retry.ReconnectOptions;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.shard.MemberRequestFilter;
import discord4j.core.shard.ShardCoordinator;
import discord4j.core.state.StateHolder;
import discord4j.core.state.StateView;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.GatewayReactorResources;
import discord4j.gateway.intent.IntentSet;
import discord4j.store.api.Store;
import discord4j.voice.VoiceReactorResources;

import java.time.Duration;

/**
 * A set of dependencies required to build and coordinate multiple {@link GatewayClient} instances.
 */
public class GatewayResources {

    private final StateView stateView;
    private final EventDispatcher eventDispatcher;
    private final ShardCoordinator shardCoordinator;
    private final MemberRequestFilter memberRequestFilter;
    private final GatewayReactorResources gatewayReactorResources;
    private final VoiceReactorResources voiceReactorResources;
    private final ReconnectOptions voiceReconnectOptions;
    private final Possible<IntentSet> intents;
    private final Duration memberRequestTimeout;

    /**
     * Create a new {@link GatewayResources} with the given parameters.
     *
     * @param stateView a read-only facade for an entity cache based off {@link StateHolder}
     * @param eventDispatcher an event bus dedicated to distribute {@link Event} instances
     * @param shardCoordinator a middleware component to coordinate multiple shard-connecting efforts
     * @param memberRequestFilter a strategy to determine whether guild members should be requested
     * @param gatewayReactorResources a custom set of Reactor resources targeting Gateway operations
     * @param voiceReactorResources a set of Reactor resources targeting Voice Gateway operations
     * @param voiceReconnectOptions a reconnection policy for Voice Gateway connections
     * @param intents an optional set of events to subscribe when connecting to the Gateway
     * @param memberRequestTimeout a {@link Duration} to limit the time member list requests take
     */
    public GatewayResources(StateView stateView,
                            EventDispatcher eventDispatcher,
                            ShardCoordinator shardCoordinator,
                            MemberRequestFilter memberRequestFilter,
                            GatewayReactorResources gatewayReactorResources,
                            VoiceReactorResources voiceReactorResources,
                            ReconnectOptions voiceReconnectOptions,
                            Possible<IntentSet> intents,
                            Duration memberRequestTimeout) {
        this.stateView = stateView;
        this.eventDispatcher = eventDispatcher;
        this.shardCoordinator = shardCoordinator;
        this.memberRequestFilter = memberRequestFilter;
        this.gatewayReactorResources = gatewayReactorResources;
        this.voiceReactorResources = voiceReactorResources;
        this.voiceReconnectOptions = voiceReconnectOptions;
        this.intents = intents;
        this.memberRequestTimeout = memberRequestTimeout;
    }

    /**
     * Returns an {@link IntentSet} containing the {@link discord4j.gateway.intent.Intent}s declared by the user
     *
     * @return The {@link IntentSet} tied to this {@link GatewayResources}
     */
    public Possible<IntentSet> getIntents() {
        return intents;
    }

    /**
     * Repository aggregate view of all caching related operations. Discord Gateway mandates its clients to cache its
     * updates through events coming from the real-time websocket. The {@link StateView} is a read-only facade for
     * {@link StateHolder} which is the mediator for the underlying {@link Store} instances for each cached entity.
     *
     * @return the {@link StateView} tied to this {@link GatewayResources}
     * @deprecated v3.2.0 will introduce a new way of working with stores, see
     * <a href="https://github.com/Discord4J/Discord4J/pull/788">this pull request</a> for details
     */
    public StateView getStateView() {
        return stateView;
    }

    /**
     * Distributes events to subscribers. Starting from v3.1, the {@link EventDispatcher} is capable of distributing
     * events from all {@link GatewayClient} connections (shards) that were specified when this
     * {@link GatewayDiscordClient} was created.
     *
     * @return the {@link EventDispatcher} tied to this {@link GatewayResources}
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Returns the {@link ShardCoordinator} that is capable of coordinating certain shard actions.
     *
     * @return the {@link ShardCoordinator} tied to this {@link GatewayResources}
     */
    public ShardCoordinator getShardCoordinator() {
        return shardCoordinator;
    }

    /**
     * Return a {@link MemberRequestFilter} indicating whether this shard group should be requesting guild members.
     *
     * @return the {@link MemberRequestFilter} configured in this {@link GatewayResources}
     */
    public MemberRequestFilter getMemberRequestFilter() {
        return memberRequestFilter;
    }

    /**
     * Return the {@link GatewayReactorResources} used to perform Gateway-related operations.
     *
     * @return the Gateway Reactor resources
     */
    public GatewayReactorResources getGatewayReactorResources() {
        return gatewayReactorResources;
    }

    /**
     * Return the {@link VoiceReactorResources} used to perform Voice Gateway-related operations.
     *
     * @return the Voice Gateway Reactor resources
     */
    public VoiceReactorResources getVoiceReactorResources() {
        return voiceReactorResources;
    }

    /**
     * Return the reconnect policy used to retry a connection to the Voice Gateway.
     *
     * @return a reconnection policy
     */
    public ReconnectOptions getVoiceReconnectOptions() {
        return voiceReconnectOptions;
    }

    /**
     * Return a {@link Duration} to be used when the target client requests a complete list of guild members through
     * the Gateway. Such requests might never be fulfilled if Gateway Intents are not used and privileged guild
     * members intent is not enabled in the developer panel.
     *
     * @return the default timeout to be applied on complete member list requests
     * @deprecated to be removed in v3.2, as Gateway Intents are mandatory and client-side validations can be
     * reliably performed
     */
    @Deprecated
    public Duration getMemberRequestTimeout() {
        return memberRequestTimeout;
    }
}
