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

public class MessageChannelBean extends ChannelBean {

    private static final long serialVersionUID = -1704295320338705282L;

    @Nullable
    private Long lastMessageId;
    @Nullable
    private String lastPinTimestamp;

    public MessageChannelBean(final GatewayChannelResponse channel) {
        super(channel.getId(), channel.getType());
        lastMessageId = channel.getLastMessageId();
        lastPinTimestamp = channel.getLastPinTimestamp();
    }

    public MessageChannelBean(final ChannelResponse response) {
        super(response);
        lastMessageId = response.getLastMessageId();
        lastPinTimestamp = response.getLastPinTimestamp();
    }

    public MessageChannelBean() {}

    @Nullable
    public final Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(@Nullable final Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    @Nullable
    public final String getLastPinTimestamp() {
        return lastPinTimestamp;
    }

    public void setLastPinTimestamp(@Nullable final String lastPinTimestamp) {
        this.lastPinTimestamp = lastPinTimestamp;
    }
}
