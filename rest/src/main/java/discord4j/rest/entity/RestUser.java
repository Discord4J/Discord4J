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

package discord4j.rest.entity;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.DMCreateRequest;
import discord4j.discordjson.json.UserData;
import discord4j.rest.RestClient;
import reactor.core.publisher.Mono;

/**
 * Represents a user (bot or normal) entity in Discord. Users can spawn across the entire platform, be members of
 * guilds, participate in text and voice chat, and much more.
 */
public class RestUser {

    private final RestClient restClient;
    private final long id;

    private RestUser(RestClient restClient, long id) {
        this.restClient = restClient;
        this.id = id;
    }

    /**
     * Create a {@link RestUser} for a given ID. This method does not perform any API request.
     *
     * @param restClient the client to make API requests
     * @param id the ID of this entity
     * @return a {@code RestUser} represented by this {@code id}.
     */
    public static RestUser create(RestClient restClient, Snowflake id) {
        return new RestUser(restClient, id.asLong());
    }

    static RestUser create(RestClient restClient, long id) {
        return new RestUser(restClient, id);
    }

    /**
     * Returns the ID of this user.
     *
     * @return The ID of this user
     */
    public long getId() {
        return id;
    }

    /**
     * Retrieve this user's data upon subscription.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link UserData} belonging to this user.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<UserData> getData() {
        return restClient.getUserService().getUser(id);
    }

    /**
     * Requests to retrieve the private channel (DM) to this user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link ChannelData private channel} to
     * this user. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<ChannelData> getPrivateChannel() {
        return restClient.getUserService().createDM(DMCreateRequest.builder().recipientId(Snowflake.asString(id)).build());
    }
}
