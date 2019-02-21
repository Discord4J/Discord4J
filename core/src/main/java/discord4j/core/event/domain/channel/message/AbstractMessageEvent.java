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
package discord4j.core.event.domain.channel.message;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.channel.AbstractChannelEvent;
import discord4j.core.object.util.Snowflake;

public abstract class AbstractMessageEvent extends AbstractChannelEvent implements MessageEvent {

    private final long messageId;

    protected AbstractMessageEvent(final DiscordClient client, final long channelId, final long messageId) {
        super(client, channelId);
        this.messageId = messageId;
    }

    @Override
    public final Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }
}
