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
package discord4j.core.event.domain.channel;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.PrivateChannel;

/**
 * TODO what does this mean?
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#channel-delete">Channel Delete</a>
 */
public class PrivateChannelDeleteEvent extends ChannelEvent {

    private final PrivateChannel channel;

    public PrivateChannelDeleteEvent(DiscordClient client, PrivateChannel channel) {
        super(client);
        this.channel = channel;
    }

    /**
     * Gets the {@link PrivateChannel} that was deleted in this event.
     *
     * @return The {@link PrivateChannel} that was deleted.
     */
    public PrivateChannel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "PrivateChannelDeleteEvent{" +
                "channel=" + channel +
                '}';
    }
}
