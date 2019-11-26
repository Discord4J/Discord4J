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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;

public class RequestGuildMembers implements PayloadData {

    @JsonProperty("guild_id")
    @UnsignedJson
    private final long guildId;
    private final String query;
    private final int limit;

    @JsonCreator
    public RequestGuildMembers(@JsonProperty("guild_id") long guildId,
                               @JsonProperty("query") String query,
                               @JsonProperty("limit") int limit) {
        this.guildId = guildId;
        this.query = query;
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "RequestGuildMembers{" +
                "guildId=" + guildId +
                ", query='" + query + '\'' +
                ", limit=" + limit +
                '}';
    }
}
