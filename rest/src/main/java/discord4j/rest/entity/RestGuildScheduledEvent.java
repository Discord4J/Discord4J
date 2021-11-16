/*
 *  This file is part of Discord4J.
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

package discord4j.rest.entity;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.GuildScheduledEventData;
import discord4j.rest.RestClient;
import reactor.core.publisher.Mono;

/**
 * Represents a guild scheduled event within Discord.
 */
public class RestGuildScheduledEvent {

    private final RestClient restClient;

    private final long guildId;

    private final long id;

    private RestGuildScheduledEvent(RestClient restClient, long guildId, long id) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.id = id;
    }

    /**
     * Create a {@link RestGuildScheduledEvent} for the given parameters. This method does not perform any API request.
     *
     * @param restClient The client to make API requests.
     * @param guildId The ID of the guild this entity belongs to.
     * @param id the ID of this entity.
     * @return A {@code RestGuildScheduledEvent} represented by the given parameters.
     */
    public static RestGuildScheduledEvent create(RestClient restClient, Snowflake guildId, Snowflake id) {
        return new RestGuildScheduledEvent(restClient, guildId.asLong(), id.asLong());
    }

    static RestGuildScheduledEvent create(RestClient restClient, long guildId, long id) {
        return new RestGuildScheduledEvent(restClient, guildId, id);
    }

    /**
     * Returns the ID of the guild this scheduled event belongs to.
     *
     * @return The ID of the guild this scheduled event belongs to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Returns the ID of this scheduled event.
     *
     * @return The ID of this scheduled event.
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    /**
     * Return the guild tied to thi role as a REST operations handle.
     *
     * @return The parent guild for this scheduled event.
     */
    public RestGuild guild() {
        return RestGuild.create(restClient, guildId);
    }

    //TODO: Edit

    //TODO: Delete

    //TODO: get users

    /**
     * Retrieve this scheduled event's data upon subscription.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildScheduledEventData} belonging to
     * this scheduled event. If an error is received, it is emitted through the {@code Mono}.
     */
    //TODO: get data
}
