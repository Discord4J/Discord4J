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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import discord4j.common.jackson.PossibleJson;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.UserResponse;
import discord4j.gateway.json.response.GameResponse;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

@PossibleJson
public class PresenceUpdate implements Dispatch {

    private JsonNode user;
    @Nullable
    @UnsignedJson
    private long[] roles;
    @Nullable
    private GameResponse game;
    @JsonProperty("guild_id")
    @UnsignedJson
    private long guildId;
    private String status;
    @Nullable
    private String nick;

    public JsonNode getUser() {
        return user;
    }

    @Nullable
    public long[] getRoles() {
        return roles;
    }

    @Nullable
    public GameResponse getGame() {
        return game;
    }

    public long getGuildId() {
        return guildId;
    }

    public String getStatus() {
        return status;
    }

    @Nullable
    public String getNick() {
        return nick;
    }

    @Override
    public String toString() {
        return "PresenceUpdate{" +
                "user=" + user +
                ", roles=" + Arrays.toString(roles) +
                ", game=" + game +
                ", guildId=" + guildId +
                ", status='" + status + '\'' +
                ", nick='" + nick + '\'' +
                '}';
    }
}
