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
package discord4j.core.event.domain;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a webhook is updated in a guild.
 * <p>
 * Discord does not send any information about what was actually updated. This is simply a notification of SOME update.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#webhooks-update">Webhooks Update</a>
 */
public class WebhooksUpdateEvent extends Event {

    private final long guildId;
    private final long channelId;

    public WebhooksUpdateEvent(DiscordClient client, long guildId, long channelId) {
        super(client);
        this.guildId = guildId;
        this.channelId = channelId;
    }

    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    public Mono<TextChannel> getChannel() {
        return getClient().getTextChannelById(getChannelId());
    }

    @Override
    public String toString() {
        return "WebhooksUpdateEvent{" +
                "guildId=" + guildId +
                ", channelId=" + channelId +
                '}';
    }
}
