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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.rest.entity.data;

import discord4j.rest.json.response.InviteResponse;

public class InviteData {

    private final String code;
    private final long guildId;
    private final long channelId;

    public InviteData(InviteResponse response) {
        code = response.getCode();
        guildId = response.getGuild().getId();
        channelId = response.getChannel().getId();
    }

    public String getCode() {
        return code;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    @Override
    public String toString() {
        return "InviteData{" +
                "code='" + code + '\'' +
                ", guildId=" + guildId +
                ", channelId=" + channelId +
                '}';
    }
}
