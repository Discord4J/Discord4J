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
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.UserResponse;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class GuildMemberUpdate implements Dispatch {

    @JsonProperty("guild_id")
    @UnsignedJson
    private long guildId;
    @UnsignedJson
    private long[] roles;
    private UserResponse user;
    private String nick;
    @Nullable
    @JsonProperty("premium_since")
    private String premiumSince;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    public long getGuildId() {
        return guildId;
    }

    public long[] getRoles() {
        return roles;
    }

    public UserResponse getUser() {
        return user;
    }

    public String getNick() {
        return nick;
    }

    @Nullable
    public String getPremiumSince() {
        return premiumSince;
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
        return "GuildMemberUpdate{" +
                "guildId=" + guildId +
                ", roles=" + Arrays.toString(roles) +
                ", user=" + user +
                ", nick='" + nick + '\'' +
                ", premiumSince='" + premiumSince + '\'' +
                '}';
    }
}
