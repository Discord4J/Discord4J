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

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;

import javax.annotation.Nullable;
import java.util.Arrays;

public class GuildMemberResponse {

    private UserResponse user;
    @Nullable
    private String nick;
    @UnsignedJson
    private long[] roles;
    @JsonProperty("joined_at")
    private String joinedAt;
    private boolean deaf;
    private boolean mute;

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

    public boolean isDeaf() {
        return deaf;
    }

    public boolean isMute() {
        return mute;
    }

    @Override
    public String toString() {
        return "GuildMemberResponse{" +
                "user=" + user +
                ", nick='" + nick + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", joinedAt='" + joinedAt + '\'' +
                ", deaf=" + deaf +
                ", mute=" + mute +
                '}';
    }
}
