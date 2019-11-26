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
package discord4j.gateway.json.dispatch;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.json.UserResponse;
import discord4j.gateway.json.response.UnavailableGuildResponse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Ready implements Dispatch {

    @JsonProperty("v")
    private int version;
    private UserResponse user;
    private UnavailableGuildResponse[] guilds;
    @JsonProperty("session_id")
    private String sessionId;
    @JsonProperty("_trace")
    private String[] trace;
    private int[] shard;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    public int getVersion() {
        return version;
    }

    public UserResponse getUser() {
        return user;
    }

    public UnavailableGuildResponse[] getGuilds() {
        return guilds;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String[] getTrace() {
        return trace;
    }

    public int[] getShard() {
        return shard;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "Ready{" +
                "version=" + version +
                ", user=" + user +
                ", guilds=" + Arrays.toString(guilds) +
                ", sessionId='" + sessionId + '\'' +
                ", trace=" + Arrays.toString(trace) +
                ", shard=" + Arrays.toString(shard) +
                '}';
    }
}
