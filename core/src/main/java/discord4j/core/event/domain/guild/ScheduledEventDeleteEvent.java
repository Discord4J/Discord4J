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

package discord4j.core.event.domain.guild;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.ScheduledEvent;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a guild scheduled event is deleted.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#guild-scheduled-event-delete">Discord</a>
 */
public class ScheduledEventDeleteEvent extends GuildEvent {

    private final ScheduledEvent scheduledEvent;

    public ScheduledEventDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, ScheduledEvent scheduledEvent) {
        super(gateway, shardInfo);
        this.scheduledEvent = scheduledEvent;
    }

    /**
     * Return the {@link ScheduledEvent} associated with this create event.
     *
     * @return a deleted scheduled event
     */
    public ScheduledEvent getScheduledEvent() {
        return scheduledEvent;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event.
     *
     * @return the ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return scheduledEvent.getGuildId();
    }

    /**
     * Requests to retrieve the {@link Guild} where a scheduled event was created.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public String toString() {
        return "ScheduledEventDeleteEvent{" +
                "scheduledEvent=" + scheduledEvent +
                "} " + super.toString();
    }
}
