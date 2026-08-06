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
package discord4j.core.event.domain.channel;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.ChannelInfo;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Dispatched when a Request Channel Info is requested.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://docs.discord.com/developers/events/gateway-events#channel-info">
 * Channel Info</a>
 */
public class ChannelInfoEvent extends ChannelEvent {

    private final long guildId;
    private final List<ChannelInfo> channels;

    public ChannelInfoEvent(
            final GatewayDiscordClient gateway,
            final ShardInfo shardInfo,
            long guildId,
            List<ChannelInfo> channels
    ) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.channels = channels;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event.
     *
     * @return The ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(this.guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} involved in the event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return this.getClient().getGuildById(this.getGuildId());
    }

    /**
     * Gets the list of channels involved in the event.
     *
     * @return The list of channels involved in the event.
     */
    public List<ChannelInfo> getChannels() {
        return this.channels;
    }

}
