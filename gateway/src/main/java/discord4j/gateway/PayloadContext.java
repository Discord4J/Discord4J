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
package discord4j.gateway;

import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.PayloadData;
import reactor.util.annotation.Nullable;

/**
 * Represents gateway payload data enriched with context for processing through a
 * {@link discord4j.gateway.PayloadHandler} defined under {@link discord4j.gateway.PayloadHandlers}
 *
 * @param <T> the type of the {@link discord4j.gateway.json.PayloadData}
 */
public class PayloadContext<T extends PayloadData> {

    private final GatewayPayload<T> payload;
    private final DiscordWebSocketHandler handler;
    private final GatewayClient client;

    public PayloadContext(GatewayPayload<T> payload, DiscordWebSocketHandler handler, GatewayClient client) {
        this.payload = payload;
        this.handler = handler;
        this.client = client;
    }

    public GatewayPayload<T> getPayload() {
        return payload;
    }

    @Nullable
    public T getData() {
        return payload.getData();
    }

    public DiscordWebSocketHandler getHandler() {
        return handler;
    }

    public GatewayClient getClient() {
        return client;
    }
}
