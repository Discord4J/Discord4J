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
import discord4j.gateway.json.response.GatewayChannelResponse;
import discord4j.rest.json.response.ChannelResponse;
import reactor.util.annotation.Nullable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public final class TextChannelBean extends ChannelBean {

    private static final long serialVersionUID = 7465805580034446331L;

    private GuildChannelBean guildChannel;
    private MessageChannelBean messageChannel;
    @Nullable
    private String topic;
    private boolean nsfw;
    private int rateLimitPerUser;

    public TextChannelBean(final GatewayChannelResponse channel, long guildId) {
        super(channel.getId(), channel.getType());
        guildChannel = new GuildChannelBean(channel, guildId);
        messageChannel = new MessageChannelBean(channel);
        topic = channel.getTopic();
        nsfw = channel.getNsfw() != null && channel.getNsfw();
        final Integer rateLimitPerUser = channel.getRateLimitPerUser();
        this.rateLimitPerUser = (rateLimitPerUser == null) ? 0 : rateLimitPerUser;
    }

    public TextChannelBean(final ChannelResponse response) {
        super(response);
        guildChannel = new GuildChannelBean(response);
        messageChannel = new MessageChannelBean(response);
        topic = response.getTopic();
        nsfw = response.isNsfw() != null && response.isNsfw();
        final Integer rateLimitPerUser = response.getRateLimitPerUser();
        this.rateLimitPerUser = (rateLimitPerUser == null) ? 0 : rateLimitPerUser;
    }

    public TextChannelBean() {}

    public GuildChannelBean getGuildChannel() {
        return guildChannel;
    }

    public void setGuildChannel(final GuildChannelBean guildChannel) {
        this.guildChannel = guildChannel;
    }

    public long getGuildId() {
        return this.guildChannel.getGuildId();
    }

    public void setGuildId(Long guildId) {
        this.guildChannel.setGuildId(guildId);
    }

    public MessageChannelBean getMessageChannel() {
        return messageChannel;
    }

    public void setMessageChannel(final MessageChannelBean messageChannel) {
        this.messageChannel = messageChannel;
    }

    @Nullable
    public String getTopic() {
        return topic;
    }

    public void setTopic(@Nullable final String topic) {
        this.topic = topic;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(final boolean nsfw) {
        this.nsfw = nsfw;
    }

    public int getRateLimitPerUser() {
        return rateLimitPerUser;
    }

    public void setRateLimitPerUser(final int rateLimitPerUser) {
        this.rateLimitPerUser = rateLimitPerUser;
    }

    @Override
    public String toString() {
        return "TextChannelBean{" +
                "guildChannel=" + guildChannel +
                ", messageChannel=" + messageChannel +
                ", topic='" + topic + '\'' +
                ", nsfw=" + nsfw +
                ", rateLimitPerUser=" + rateLimitPerUser +
                "} " + super.toString();
    }
}
