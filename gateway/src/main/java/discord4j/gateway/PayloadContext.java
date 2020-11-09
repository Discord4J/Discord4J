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

import discord4j.discordjson.json.gateway.PayloadData;
import discord4j.gateway.json.GatewayPayload;
import reactor.util.annotation.Nullable;
import reactor.util.context.ContextView;

/**
 * Represents gateway payload data enriched with context for processing through a {@link PayloadHandler} defined
 * under {@link PayloadHandlers}.
 *
 * @param <T> the type of the {@link PayloadData}
 */
public class PayloadContext<T extends PayloadData> {

    private final GatewayPayload<T> payload;
    private final GatewayWebsocketHandler handler;
    private final DefaultGatewayClient client;
    private final ContextView context;

    public PayloadContext(GatewayPayload<T> payload, GatewayWebsocketHandler handler, DefaultGatewayClient client,
                          ContextView context) {
        this.payload = payload;
        this.handler = handler;
        this.client = client;
        this.context = context;
    }

    public GatewayPayload<T> getPayload() {
        return payload;
    }

    @Nullable
    public T getData() {
        return payload.getData();
    }

    public GatewayWebsocketHandler getHandler() {
        return handler;
    }

    public DefaultGatewayClient getClient() {
        return client;
    }

    public ContextView getContext() {
        return context;
    }
}
