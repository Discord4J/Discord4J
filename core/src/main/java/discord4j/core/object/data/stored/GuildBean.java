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
import discord4j.common.json.UserResponse;
import discord4j.gateway.json.dispatch.GuildCreate;
import discord4j.gateway.json.dispatch.GuildUpdate;
import discord4j.gateway.json.response.GatewayChannelResponse;

import java.util.Arrays;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public final class GuildBean extends BaseGuildBean {

    private static final long serialVersionUID = 4350381811087818276L;

    private String joinedAt;
    private boolean large;
    private int memberCount;
    private long[] members;
    private long[] channels;

    public GuildBean(GuildCreate guildCreate) {
        super(guildCreate);

        this.joinedAt = guildCreate.getJoinedAt();
        this.large = guildCreate.isLarge();
        this.memberCount = guildCreate.getMemberCount();

        members = Arrays.stream(guildCreate.getMembers())
                .map(GuildMemberResponse::getUser)
                .mapToLong(UserResponse::getId)
                .distinct()
                .toArray();

        channels = Arrays.stream(guildCreate.getChannels())
                .mapToLong(GatewayChannelResponse::getId)
                .toArray();
    }

    public GuildBean(final GuildBean toCopy, final GuildUpdate guildUpdate) {
        super(guildUpdate);

        this.joinedAt = toCopy.joinedAt;
        this.large = toCopy.large;
        this.memberCount = toCopy.memberCount;
        this.members = Arrays.copyOf(toCopy.members, toCopy.members.length);
        this.channels = Arrays.copyOf(toCopy.channels, toCopy.channels.length);
    }

    public GuildBean(final GuildBean toCopy) {
        super(toCopy);

        this.joinedAt = toCopy.getJoinedAt();
        this.large = toCopy.getLarge();
        this.memberCount = toCopy.getMemberCount();
        this.members = Arrays.copyOf(toCopy.members, toCopy.members.length);
        this.channels = Arrays.copyOf(toCopy.channels, toCopy.channels.length);
    }

    public GuildBean() {}

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(final String joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean getLarge() {
        return large;
    }

    public void setLarge(final Boolean large) {
        this.large = large;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(final Integer memberCount) {
        this.memberCount = memberCount;
    }

    public long[] getMembers() {
        return members;
    }

    public void setMembers(final long[] members) {
        this.members = members;
    }

    public long[] getChannels() {
        return channels;
    }

    public void setChannels(final long[] channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "GuildBean{" +
                "joinedAt='" + joinedAt + '\'' +
                ", large=" + large +
                ", memberCount=" + memberCount +
                ", members=" + Arrays.toString(members) +
                ", channels=" + Arrays.toString(channels) +
                "} " + super.toString();
    }
}
