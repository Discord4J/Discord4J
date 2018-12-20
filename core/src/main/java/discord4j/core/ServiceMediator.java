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

public final class ServiceMediator {

    private final GatewayClient gatewayClient;
    private final RestClient restClient;
    private final StoreService storeService;
    private final StateHolder stateHolder;
    private final EventDispatcher eventDispatcher;
    private final DiscordClient discordClient;
    private final ClientConfig clientConfig;
    private final VoiceClient voiceClient;

    public ServiceMediator(final GatewayClient gatewayClient, final RestClient restClient, final StoreService storeService,
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

    public GatewayClient getGatewayClient() {
        return gatewayClient;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public StateHolder getStateHolder() {
        return stateHolder;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public DiscordClient getClient() {
        return discordClient;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public VoiceClient getVoiceClient() {
        return voiceClient;
    }
}
