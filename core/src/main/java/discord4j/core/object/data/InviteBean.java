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
package discord4j.core.object.data;

import discord4j.rest.json.response.InviteResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

public class InviteBean implements Serializable {

    private static final long serialVersionUID = -92456339756174244L;

    private String code;
    private long guildId;
    private long channelId;
    @Nullable
    private Integer approximatePresenceCount;

    public InviteBean(final InviteResponse response) {
        code = response.getCode();
        guildId = response.getGuild().getId();
        channelId = response.getChannel().getId();
        approximatePresenceCount = response.getApproximatePresenceCount();
    }

    public InviteBean() {}

    public final String getCode() {
        return code;
    }

    public final void setCode(final String code) {
        this.code = code;
    }

    public final long getGuildId() {
        return guildId;
    }

    public final void setGuildId(final long guildId) {
        this.guildId = guildId;
    }

    public final long getChannelId() {
        return channelId;
    }

    public final void setChannelId(final long channelId) {
        this.channelId = channelId;
    }

    @Nullable
    public final Integer getApproximatePresenceCount() {
        return approximatePresenceCount;
    }

    public final void setApproximatePresenceCount(@Nullable final Integer approximatePresenceCount) {
        this.approximatePresenceCount = approximatePresenceCount;
    }

    @Override
    public String toString() {
        return "InviteBean{" +
                "code='" + code + '\'' +
                ", guildId=" + guildId +
                ", channelId=" + channelId +
                ", approximatePresenceCount=" + approximatePresenceCount +
                '}';
    }
}
