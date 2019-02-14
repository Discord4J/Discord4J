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

import java.util.HashMap;
import java.util.Map;

/**
 * A set of parameters currently used to establish a connection to the gateway.
 */
public final class ClientConfig {

    private final String token;
    private final int shardIndex;
    private final int shardCount;

    ClientConfig(final String token, final int shardIndex, final int shardCount) {
        this.token = token;
        this.shardIndex = shardIndex;
        this.shardCount = shardCount;
    }

    /**
     * The bot token used to identify to the gateway.
     *
     * @return a bot token
     */
    public String getToken() {
        return token;
    }

    /**
     * The current shard index used to identify to the gateway.
     *
     * @return the shard index
     */
    public int getShardIndex() {
        return shardIndex;
    }

    /**
     * The number of shards used to identify to the gateway.
     *
     * @return the shard count
     */
    public int getShardCount() {
        return shardCount;
    }

    /**
     * Retrieves the set of query parameters used to establish a gateway URL connection.
     *
     * @return a Map of query parameters targeting a gateway connection
     */
    public Map<String, Object> getGatewayParameters() {
        final Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("compress", "zlib-stream");
        parameters.put("encoding", "json");
        parameters.put("v", 6);
        return parameters;
    }
}
