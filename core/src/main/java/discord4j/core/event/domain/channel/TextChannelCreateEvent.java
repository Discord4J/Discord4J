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
import discord4j.core.event.domain.guild.GuildEvent;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;

/**
 * Dispatched when a {@link TextChannel} is created in a guild.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#channel-create">Channel Create</a>
 */
public class TextChannelCreateEvent extends AbstractChannelEvent implements GuildEvent {

    private final TextChannel channel;

    public TextChannelCreateEvent(DiscordClient client, TextChannel channel) {
        super(client, channel.getId().asLong());
        this.channel = channel;
    }

    public TextChannel getChannel() {
        return channel;
    }

    @Override
    public Snowflake getGuildId() {
        return channel.getGuildId();
    }

    @Override
    public String toString() {
        return "TextChannelCreateEvent{" +
                "channel=" + channel +
                '}';
    }
}
