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
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.EmbedResponse;
import reactor.util.annotation.Nullable;

public class MessageUpdate implements Dispatch {

    @UnsignedJson
    private long id;
    @JsonProperty("channel_id")
    @UnsignedJson
    private long channelId;
    @JsonProperty("guild_id")
    @UnsignedJson
    private Possible<Long> guildId;
    @Nullable
    private Possible<String> content = Possible.absent();
    @Nullable
    private Possible<EmbedResponse[]> embeds = Possible.absent();

    public long getId() {
        return id;
    }

    public long getChannelId() {
        return channelId;
    }

    public Possible<Long> getGuildId() {
        return guildId;
    }

    @Nullable
    public Possible<String> getContent() {
        return content;
    }

    @Nullable
    public Possible<EmbedResponse[]> getEmbeds() {
        return embeds;
    }

    @Override
    public String toString() {
        return "MessageUpdate{" +
                "id=" + id +
                ", channelId=" + channelId +
                ", guildId=" + guildId +
                ", content=" + content +
                ", embeds=" + embeds +
                '}';
    }
}
