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
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class ScheduledEventUpdateEvent extends GuildEvent {

    private final ScheduledEvent current;
    @Nullable
    private final ScheduledEvent old;

    public ScheduledEventUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, ScheduledEvent current,
                                     @Nullable ScheduledEvent old) {
        super(gateway, shardInfo);
        this.current = current;
        this.old = old;
    }

    /**
     * Return the {@link ScheduledEvent} associated with this update event.
     *
     * @return an updated scheduled event
     */
    public ScheduledEvent getCurrent() {
        return current;
    }

    /**
     * Return the previous {@link ScheduledEvent} entity that was updated in this event. May not be present if the
     * entity is not stored.
     *
     * @return a previous version of an updated scheduled event, if present
     */
    public Optional<ScheduledEvent> getOld() {
        return Optional.ofNullable(old);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event.
     *
     * @return the ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return current.getGuildId();
    }

    /**
     * Requests to retrieve the {@link Guild} where a scheduled event was updated.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public String toString() {
        return "ScheduledEventUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                "}";
    }
}
