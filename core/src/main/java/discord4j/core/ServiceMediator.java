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

import discord4j.core.event.EventDispatcher;
import discord4j.gateway.GatewayClient;
import discord4j.rest.RestClient;
import discord4j.store.api.service.StoreService;
import discord4j.voice.VoiceClient;

/**
 * {@link ServiceMediator} encapsulates the set of dependencies core module requires to function, giving access to the
 * underlying resources currently on use.
 * <p>
 * The following are some of the resources available through this mediator:
 * <ul>
 * <li>Access to {@link GatewayClient} allows retrieving specific properties and actions regarding Discord real-time
 * websocket connections.</li>
 * <li>Access to {@link RestClient} provides a low-level way to perform API requests.</li>
 * <li>Access to {@link StoreService} and {@link StateHolder} allows low-level store manipulation. Modifying the
 * underlying structure during runtime can result in unexpected behavior.</li>
 * </ul>
 */
public final class ServiceMediator {

    private final GatewayClient gatewayClient;
    private final RestClient restClient;
    private final StoreService storeService;
    private final StateHolder stateHolder;
    private final EventDispatcher eventDispatcher;
    private final DiscordClient discordClient;
    private final ClientConfig clientConfig;
    private final VoiceClient voiceClient;

    public ServiceMediator(final GatewayClient gatewayClient, final RestClient restClient,
                           final StoreService storeService,
                           final StateHolder stateHolder, final EventDispatcher eventDispatcher,
                           final ClientConfig clientConfig, VoiceClient voiceClient) {
        this.gatewayClient = gatewayClient;
        this.restClient = restClient;
        this.storeService = storeService;
        this.stateHolder = stateHolder;
        this.eventDispatcher = eventDispatcher;
        this.discordClient = new DiscordClient(this);
        this.clientConfig = clientConfig;
        this.voiceClient = voiceClient;
    }

    /**
     * Get the current client for Gateway operations.
     *
     * @return the current {@link GatewayClient}
     */
    public GatewayClient getGatewayClient() {
        return gatewayClient;
    }

    /**
     * Get the current client for REST operations providing a low-level way to perform requests.
     *
     * @return the current {@link RestClient}
     */
    public RestClient getRestClient() {
        return restClient;
    }

    /**
     * Get the current store factory.
     *
     * @return the current {@link StoreService}
     */
    public StoreService getStoreService() {
        return storeService;
    }

    /**
     * Get access to the stored/cached values coming from real-time Gateway updates.
     *
     * @return the current {@link StateHolder}
     */
    public StateHolder getStateHolder() {
        return stateHolder;
    }

    /**
     * Get the event dispatching processor used by this client.
     *
     * @return the current {@link EventDispatcher}
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Get the {@link DiscordClient} that is served by this instance.
     *
     * @return the current {@link DiscordClient}
     */
    public DiscordClient getClient() {
        return discordClient;
    }

    /**
     * Get the current configuration for initiating gateway connections.
     *
     * @return the current {@link ClientConfig}
     */
    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    /**
     * Get the current voice client to initiate voice gateway connections.
     *
     * @return the current {@link VoiceClient}
     */
    public VoiceClient getVoiceClient() {
        return voiceClient;
    }
}
