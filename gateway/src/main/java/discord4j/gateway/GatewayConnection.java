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

import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

/**
 * A handle to manipulate a gateway connection, represents a connection to an active {@link GatewayClient}.
 */
public class GatewayConnection {

    private final GatewayClient gatewayClient;
    private final IdentifyOptions identifyOptions;
    private final MonoProcessor<Void> disconnectFuture;

    public GatewayConnection(GatewayClient gatewayClient, IdentifyOptions identifyOptions,
                             MonoProcessor<Void> disconnectFuture) {
        this.gatewayClient = gatewayClient;
        this.identifyOptions = identifyOptions;
        this.disconnectFuture = disconnectFuture;
    }

    /**
     * Logs out the client from the gateway.
     *
     * @return a {@link Mono} deferring completion until this client has completely disconnected from the gateway
     */
    public Mono<Void> logout() {
        return gatewayClient.close(false);
    }

    /**
     * Returns whether this client is currently connected to Discord Gateway.
     *
     * @return true if the gateway connection is currently established, false otherwise.
     */
    public boolean isConnected() {
        return gatewayClient.isConnected();
    }

    /**
     * Gets the amount of time it last took Discord Gateway to respond to a heartbeat with an ack.
     *
     * @return the time in milliseconds took Discord to respond to the last heartbeat with an ack.
     */
    public long getResponseTime() {
        return gatewayClient.getResponseTime();
    }

    /**
     * Get the set of {@link IdentifyOptions} currently used by this connection.
     *
     * @return an {@code IdentifyOptions} object
     */
    public IdentifyOptions getIdentifyOptions() {
        return identifyOptions;
    }

    /**
     * Get a {@link Mono} that completes when the current gateway has disconnected.
     *
     * @return a {@link Mono} signaling completion upon disconnect
     */
    public Mono<Void> onDisconnect() {
        return disconnectFuture;
    }

    @Override
    public String toString() {
        return "GatewayConnection{" +
                "gatewayClient=" + gatewayClient +
                ", identifyOptions=" + identifyOptions +
                '}';
    }
}
