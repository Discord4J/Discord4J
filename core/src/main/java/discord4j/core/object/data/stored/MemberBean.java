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
package discord4j.core.object.data.stored;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import discord4j.common.json.GuildMemberResponse;
import discord4j.common.json.MessageMember;
import discord4j.gateway.json.dispatch.GuildMemberUpdate;
import discord4j.gateway.json.dispatch.MessageCreate;
import reactor.util.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public final class MemberBean implements Serializable {

    private static final long serialVersionUID = 6346416731162094189L;

    @Nullable
    private String nick;
    private long[] roles;
    private String joinedAt;
    @Nullable
    private String premiumSince;

    public MemberBean(final GuildMemberResponse response) {
        nick = response.getNick();
        roles = response.getRoles();
        joinedAt = response.getJoinedAt();
        premiumSince = response.getPremiumSince();
    }

    public MemberBean(final MessageMember member) {
        nick = member.getNick();
        roles = member.getRoles();
        joinedAt = member.getJoinedAt();
        premiumSince = member.getPremiumSince();
    }

    public MemberBean(final MemberBean toCopy, final GuildMemberUpdate update) {
        nick = update.getNick();
        roles = update.getRoles();
        joinedAt = toCopy.getJoinedAt();
        premiumSince = update.getPremiumSince();
    }

    public MemberBean(final MemberBean toCopy) {
        nick = toCopy.getNick();
        roles = Arrays.copyOf(toCopy.getRoles(), toCopy.getRoles().length);
        joinedAt = toCopy.getJoinedAt();
        premiumSince = toCopy.getPremiumSince();
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

    @Nullable
    public String getPremiumSince() {
        return premiumSince;
    }

    public void setPremiumSince(@Nullable final String premiumSince) {
        this.premiumSince = premiumSince;
    }

    @Override
    public String toString() {
        return "MemberBean{" +
                "nick='" + nick + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", joinedAt='" + joinedAt + '\'' +
                ", premiumSince='" + premiumSince + '\'' +
                '}';
    }
}
