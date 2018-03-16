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
package discord4j.common.json.payload.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.PossibleJson;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.response.GameResponse;
import discord4j.common.json.response.UserResponse;

import javax.annotation.Nullable;
import java.util.Arrays;

@PossibleJson
public class PresenceUpdate implements Dispatch {

    @Nullable
    private UserResponse user;
    @Nullable
    @UnsignedJson
    private long[] roles;
    @Nullable
    private GameResponse game;
    @JsonProperty("guild_id")
    @UnsignedJson
    private long guildId; // TODO: nullable?
    private String status;
    @Nullable
    private String nick;

    @Nullable
    public UserResponse getUser() {
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

    @Override
    public String toString() {
        return "PresenceUpdate[" +
                "user=" + user +
                ", roles=" + Arrays.toString(roles) +
                ", game=" + game +
                ", guildId=" + guildId +
                ", status=" + status +
                ", nick=" + nick +
                ']';
    }
}
