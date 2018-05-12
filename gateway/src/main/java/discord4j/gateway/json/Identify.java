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
package discord4j.gateway.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;

@PossibleJson
public class Identify implements PayloadData {

    private final String token;
    private final IdentifyProperties properties;
    private final boolean compress;
    @JsonProperty("large_threshold")
    private final int largeThreshold;
    private final Possible<int[]> shard;
    private final Possible<StatusUpdate> presence;

    public Identify(String token, IdentifyProperties properties, boolean compress, int largeThreshold,
                    Possible<int[]> shard, Possible<StatusUpdate> presence) {
        this.token = token;
        this.properties = properties;
        this.compress = compress;
        this.largeThreshold = largeThreshold;
        this.shard = shard;
        this.presence = presence;
    }

    @Override
    public String toString() {
        return "Identify[" +
                "token=hunter2" +
                ", properties=" + properties +
                ", compress=" + compress +
                ", largeThreshold=" + largeThreshold +
                ", shard=" + shard +
                ", presence=" + presence +
                ']';
    }
}
