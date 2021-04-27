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

import discord4j.common.store.Store;
import discord4j.core.event.EventDispatcher;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.gateway.GatewayOptions;
import discord4j.rest.RestClient;
import discord4j.rest.RestClientBuilder.Resources;
import discord4j.rest.request.RouterOptions;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * A high-level abstraction of common Discord4J operations such as entity retrieval through Discord API or the creation
 * of real-time bot clients through Discord Gateway.
 * <p>
 * Instances can be obtained by using {@link #create(String)} or through {@link #builder(String)}. A default instance is
 * capable of performing REST API operations locally and connecting to the Gateway by using {@link #login()},
 * {@link #withGateway(Function)} or {@link #gateway()}.
 */
public final class DiscordClient extends RestClient {

    private final CoreResources coreResources;

    /**
     * Constructs a {@code DiscordClient} with an associated {@link CoreResources}.
     *
     * @param coreResources The {@link CoreResources} associated to this object.
     */
    DiscordClient(CoreResources coreResources) {
        super(coreResources);
        this.coreResources = coreResources;
    }

    /**
     * Create a {@link DiscordClient} with default options, using the given token for authentication.
     *
     * @param token the bot token used for authentication
     * @return a {@link DiscordClient} configured with the default options
     */
    public static DiscordClient create(String token) {
        return builder(token).build();
    }

    /**
     * Obtain a {@link DiscordClientBuilder} able to create {@link DiscordClient} instances, using the given token
     * for authentication.
     *
     * @param token the bot token used for authentication
     * @return a {@link DiscordClientBuilder}
     */
    public static <B extends DiscordClientBuilder<DiscordClient, Resources, RouterOptions, B>> DiscordClientBuilder<DiscordClient, Resources, RouterOptions, B> builder(String token) {
        return DiscordClientBuilder.create(token);
    }

    /**
     * Obtain the {@link CoreResources} associated with this {@link DiscordClient}.
     *
     * @return the current {@link CoreResources} for this client
     */
    public CoreResources getCoreResources() {
        return coreResources;
    }

    /**
     * Login the client to the gateway, using the recommended amount of shards, locally coordinated. The derived
     * {@link GatewayDiscordClient} is capable of managing these shards and providing a single
     * {@link EventDispatcher} to publish Gateway updates and {@link Store} for entity caching.
     * <p>
     * To further configure the Gateway connections, such as initial presence, sharding and caching options, see
     * {@link #gateway()}.
     * <p>
     * <strong>Note:</strong> Starting from v3.1, this method will return a {@link Mono} of a
     * {@link GatewayDiscordClient}, emitting the result once shards have connected. Therefore, <strong>calling
     * {@link Mono#block()} will now return upon connection instead of disconnection.</strong>
     *
     * @return a {@link Mono} for a handle to maintain a group of shards connected to real-time Discord Gateway,
     * emitted once at least one connection has been made. This behavior can be configured through
     * {@code gateway().setAwaitConnections(true)}. If an error is received, it is emitted through the {@link Mono}.
     */
    public Mono<GatewayDiscordClient> login() {
        return gateway().login();
    }

    /**
     * Connect to the Discord Gateway upon subscription to acquire a {@link GatewayDiscordClient} instance and use it
     * in a declarative way, releasing the object once the derived usage {@link Function} completes, and the underlying
     * shard group disconnects, according to {@link GatewayDiscordClient#onDisconnect()}.
     * <p>
     * To further configure the bot features, refer to using {@link #gateway()}.
     * <p>
     * Calling this method is useful when you operate on the {@link GatewayDiscordClient} object using reactive API you
     * can compose within the scope of the given {@link Function}.
     *
     * @param whileConnectedFunction the {@link Function} to apply the <strong>connected</strong>
     * {@link GatewayDiscordClient} and trigger a processing pipeline from it.
     * @return an empty {@link Mono} completing after all resources have released
     */
    public Mono<Void> withGateway(Function<GatewayDiscordClient, Publisher<?>> whileConnectedFunction) {
        return gateway().withGateway(whileConnectedFunction);
    }

    /**
     * Start bootstrapping a connection to the real-time Discord Gateway. The resulting builder can be configured to
     * create a {@link GatewayDiscordClient} which groups all connecting shards providing a single
     * {@link EventDispatcher} to publish Gateway updates and {@link Store} for entity caching.
     * <p>
     * The following are some of the features configured by this builder:
     * <ul>
     *     <li>Sharding configuration</li>
     *     <li>Initial event listeners and customization</li>
     *     <li>Gateway intents</li>
     *     <li>Initial bot status</li>
     *     <li>Custom entity cache factory</li>
     *     <li>Distributed architecture options</li>
     *     <li>Member caching options</li>
     *     <li>Guild subscriptions</li>
     *     <li>Threading model customization</li>
     *     <li>Entity fetching strategy</li>
     *     <li>Gateway and voice connection options</li>
     * </ul>
     *
     * @return a bootstrap to create {@link GatewayDiscordClient} instances.
     */
    public GatewayBootstrap<GatewayOptions> gateway() {
        return GatewayBootstrap.create(this);
    }
}
