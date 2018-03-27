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

import discord4j.core.store.StoreHolder;
import discord4j.gateway.GatewayClient;
import discord4j.rest.RestClient;

public final class ServiceMediator {

    private final GatewayClient gatewayClient;
    private final RestClient restClient;
    private final DiscordClient discordClient;
    private final StoreHolder storeHolder;

    ServiceMediator(final GatewayClient gatewayClient, final RestClient restClient, StoreHolder storeHolder) {
        this.gatewayClient = gatewayClient;
        this.restClient = restClient;
        this.storeHolder = storeHolder;
        discordClient = new DiscordClient(this);
    }

    public GatewayClient getGatewayClient() {
        return gatewayClient;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public DiscordClient getClient() {
        return discordClient;
    }

    public StoreHolder getStoreHolder() {
        return storeHolder;
    }
}
