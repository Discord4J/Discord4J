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

/**
 * An abstract factory to create a {@link GatewayClient}, allowing clients to send and receive Discord Gateway events
 * from this node.
 */
public interface GatewayClientFactory {

    /**
     * Create {@link GatewayClient} objects, supporting real-time events from Discord.
     *
     * @param options the {@link GatewayOptions} used to configure the resulting client
     * @return a client allowing communication with Discord Gateway
     */
    GatewayClient getGatewayClient(GatewayOptions options);
}
