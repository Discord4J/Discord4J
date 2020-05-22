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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a webhook is updated in a guild.
 * <p>
 * Discord does not send any information about what was actually updated. This is simply a notification of SOME update.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#webhooks-update">Webhooks Update</a>
 */
public class WebhooksUpdateEvent extends Event {

    private final long guildId;
    private final long channelId;

    public WebhooksUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId, long channelId) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.channelId = channelId;
    }

    /**
     * Gets the {@link Snowflake} ID of the guild that had a webhook updated in this event.
     *
     * @return The ID of the guild involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} that had a webhook updated in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the {@link Snowflake} ID of the channel the webhook belongs to.
     *
     * @return The ID of the channel involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the {@link TextChannel} the webhook belongs to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(TextChannel.class);
    }

    @Override
    public String toString() {
        return "WebhooksUpdateEvent{" +
                "guildId=" + guildId +
                ", channelId=" + channelId +
                '}';
    }
}
