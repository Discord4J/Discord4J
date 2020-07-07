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

import java.util.LinkedHashMap;
import java.util.Map;

public class UserResponse {

    @UnsignedJson
    private long id;
    private String username;
    private String discriminator;
    @Nullable
    private String avatar;
    @Nullable
    private Boolean bot;
    @JsonProperty("public_flags")
    @Nullable
    private Integer publicFlags;

    @JsonIgnore
    private Map<String, Object> additionalProperties;

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    @Nullable
    public String getAvatar() {
        return avatar;
    }

    @Nullable
    public Boolean isBot() {
        return bot;
    }

    @Nullable
    public Integer getPublicFlags() {
        return publicFlags;
    }

    private void checkAdditionalProperties() {
        if (this.additionalProperties == null) {
            this.additionalProperties = new LinkedHashMap<>();
        }
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        checkAdditionalProperties();
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        checkAdditionalProperties();
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", discriminator='" + discriminator + '\'' +
                ", avatar='" + avatar + '\'' +
                ", bot=" + bot +
                ", publicFlags=" + publicFlags +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
