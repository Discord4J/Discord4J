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

import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import reactor.netty.http.client.HttpClient;

/**
 * Default factory to create {@link GatewayClient} objects based on {@link DefaultGatewayClient} that connects using a
 * single shard per client, forwarding events through {@link discord4j.gateway.json.GatewayPayload} objects.
 */
public class DefaultGatewayClientFactory implements GatewayClientFactory {

    @Override
    public GatewayClient getGatewayClient(HttpClient httpClient, PayloadReader payloadReader,
                                          PayloadWriter payloadWriter,
                                          RetryOptions retryOptions, String token, IdentifyOptions identifyOptions,
                                          GatewayObserver observer, PayloadTransformer identifyLimiter) {
        return new DefaultGatewayClient(httpClient, payloadReader, payloadWriter, retryOptions, token, identifyOptions,
                observer, identifyLimiter);
    }
}
