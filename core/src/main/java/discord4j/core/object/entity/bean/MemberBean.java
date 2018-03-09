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
package discord4j.core.object.entity.bean;

import discord4j.common.json.response.GuildMemberResponse;

import javax.annotation.Nullable;
import java.io.Serializable;

public final class MemberBean implements Serializable {

    private static final long serialVersionUID = 6346416731162094189L;

    @Nullable
    private String nick;
    private long[] roles;
    private String joinedAt;

    public MemberBean(final GuildMemberResponse response) {
        nick = response.getNick();
        roles = response.getRoles();
        joinedAt = response.getJoinedAt();
    }

    public MemberBean() {}

    @Nullable
    public String getNick() {
        return nick;
    }

    public void setNick(@Nullable final String nick) {
        this.nick = nick;
    }

    public long[] getRoles() {
        return roles;
    }

    public void setRoles(final long[] roles) {
        this.roles = roles;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(final String joinedAt) {
        this.joinedAt = joinedAt;
    }
}
