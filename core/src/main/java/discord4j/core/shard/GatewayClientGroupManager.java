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

package discord4j.core.shard;

import discord4j.gateway.GatewayClient;
import discord4j.gateway.GatewayClientGroup;

/**
 * A {@link GatewayClientGroup} that allows adding or removing {@link GatewayClient}
 * instances.
 */
interface GatewayClientGroupManager extends GatewayClientGroup {

    /**
     * Add a {@link GatewayClient} to be managed by this instance.
     *
     * @param key a key to later reference the added client
     * @param client the client to be managed
     */
    void add(int key, GatewayClient client);

    /**
     * Remove a {@link GatewayClient} from this instance using the given key.
     *
     * @param key the key representing the client to be removed
     */
    void remove(int key);

}
