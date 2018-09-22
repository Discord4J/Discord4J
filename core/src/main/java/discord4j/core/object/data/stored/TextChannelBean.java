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

import discord4j.gateway.json.response.GatewayChannelResponse;
import discord4j.rest.json.response.ChannelResponse;

import javax.annotation.Nullable;

public final class TextChannelBean extends ChannelBean {

    private static final long serialVersionUID = 7465805580034446331L;

    private GuildChannelBean guildChannel;
    private MessageChannelBean messageChannel;
    @Nullable
    private String topic;
    private boolean nsfw;

    public TextChannelBean(final GatewayChannelResponse channel, long guildId) {
        super(channel.getId(), channel.getType());
        guildChannel = new GuildChannelBean(channel, guildId);
        messageChannel = new MessageChannelBean(channel);
        topic = channel.getTopic();
        nsfw = channel.getNsfw() != null && channel.getNsfw();
    }

    public TextChannelBean(final ChannelResponse response) {
        super(response);
        guildChannel = new GuildChannelBean(response);
        messageChannel = new MessageChannelBean(response);
        topic = response.getTopic();
        nsfw = response.isNsfw() != null && response.isNsfw();
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

    @Override
    public String toString() {
        return "TextChannelBean{" +
                "guildChannel=" + guildChannel +
                ", messageChannel=" + messageChannel +
                ", topic='" + topic + '\'' +
                ", nsfw=" + nsfw +
                "} " + super.toString();
    }
}
