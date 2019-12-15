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
import com.fasterxml.jackson.databind.JsonNode;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;
import discord4j.common.jackson.UnsignedJson;
import discord4j.gateway.json.response.ActivityResponse;
import reactor.util.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@PossibleJson
public class PresenceUpdate implements Dispatch {

    private JsonNode user;
    @Nullable
    @UnsignedJson
    private long[] roles;
    @Nullable
    private ActivityResponse game;
    @JsonProperty("guild_id")
    @UnsignedJson
    private long guildId;
    private String status;
    private ActivityResponse[] activities;
    @JsonProperty("client_status")
    private ClientStatus clientStatus;
    @Nullable
    @JsonProperty("premium_since")
    private String premiumSince;
    @Nullable
    @JsonProperty("nick")
    private String nick;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    public JsonNode getUser() {
        return user;
    }

    @Nullable
    public long[] getRoles() {
        return roles;
    }

    @Nullable
    public ActivityResponse getGame() {
        return game;
    }

    public long getGuildId() {
        return guildId;
    }

    public String getStatus() {
        return status;
    }

    public ActivityResponse[] getActivities() {
        return activities;
    }

    public ClientStatus getClientStatus() {
        return clientStatus;
    }

    @Nullable
    public String getPremiumSince() {
        return premiumSince;
    }

    @Nullable
    public String getNick() {
        return nick;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }

    @PossibleJson
    public static class ClientStatus {

        private Possible<String> desktop;
        private Possible<String> mobile;
        private Possible<String> web;

        public Possible<String> getDesktop() {
            return desktop;
        }

        public Possible<String> getMobile() {
            return mobile;
        }

        public Possible<String> getWeb() {
            return web;
        }

        @Override
        public String toString() {
            return "ClientStatus{" +
                "desktop=" + desktop +
                ", mobile=" + mobile +
                ", web=" + web +
                '}';
        }
    }
}
