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

import discord4j.common.json.response.ChannelResponse;
import discord4j.common.json.response.GuildMemberResponse;
import discord4j.common.json.response.GuildResponse;
import discord4j.common.json.response.UserResponse;

import java.util.Arrays;
import java.util.Objects;

public final class GuildBean extends BaseGuildBean {

    private static final long serialVersionUID = 4350381811087818276L;

    private String joinedAt;
    private boolean large;
    private int memberCount;
    private long[] members;
    private long[] channels;

    public GuildBean(final GuildResponse response) {
        super(response);

        // None of these fields can be null if the GuildResponse was received on the gateway. Enforce this.
        this.joinedAt = Objects.requireNonNull(response.getJoinedAt());
        this.large = Objects.requireNonNull(response.getLarge());
        this.memberCount = Objects.requireNonNull(response.getMemberCount());

        members = Arrays.stream(Objects.requireNonNull(response.getMembers()))
                .map(GuildMemberResponse::getUser)
                .mapToLong(UserResponse::getId)
                .toArray();

        channels = Arrays.stream(Objects.requireNonNull(response.getChannels()))
                .mapToLong(ChannelResponse::getId)
                .toArray();
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
}
