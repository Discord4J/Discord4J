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
package discord4j.common.json;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class GuildMemberResponse {

    private UserResponse user;
    @Nullable
    private String nick;
    @UnsignedJson
    private long[] roles;
    @JsonProperty("joined_at")
    private String joinedAt;
    @Nullable
    @JsonProperty("premium_since")
    private String premiumSince;
    private boolean deaf;
    private boolean mute;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    public UserResponse getUser() {
        return user;
    }

    @Nullable
    public String getNick() {
        return nick;
    }

    public long[] getRoles() {
        return roles;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    @Nullable
    public String getPremiumSince() {
        return premiumSince;
    }

    public boolean isDeaf() {
        return deaf;
    }

    public boolean isMute() {
        return mute;
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
        return "GuildMemberResponse{" +
                "user=" + user +
                ", nick='" + nick + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", joinedAt='" + joinedAt + '\'' +
                ", premiumSince='" + premiumSince + '\'' +
                ", deaf=" + deaf +
                ", mute=" + mute +
                '}';
    }
}
