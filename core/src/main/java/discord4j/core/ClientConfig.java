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

import javax.annotation.Nullable;

public final class ClientConfig {

    private final String token;
    private final Integer shardIndex;
    private final Integer shardCount;

    ClientConfig(final String token, @Nullable final Integer shardIndex, @Nullable final Integer shardCount) {
        this.token = token;
        this.shardIndex = shardIndex;
        this.shardCount = shardCount;
    }

    public String getToken() {
        return token;
    }

    @Nullable
    public Integer getShardIndex() {
        return shardIndex;
    }

    @Nullable
    public Integer getShardCount() {
        return shardCount;
    }
}
