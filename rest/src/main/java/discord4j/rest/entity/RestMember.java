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

import discord4j.discordjson.json.MemberData;
import discord4j.rest.RestClient;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Represents a user (bot or normal) that is member of a specific guild.
 */
public class RestMember {

    private final RestClient restClient;
    private final long guildId;
    private final long id;

    private RestMember(RestClient restClient, long guildId, long id) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.id = id;
    }

    /**
     * Create a {@link RestMember} with the given parameters. This method does not perform any API request.
     *
     * @param restClient REST API resources
     * @param guildId the ID of the guild this member belongs to
     * @param id the ID of this member
     * @return a {@code RestMember} represented by the given parameters.
     */
    public static RestMember create(RestClient restClient, Snowflake guildId, Snowflake id) {
        return new RestMember(restClient, guildId.asLong(), id.asLong());
    }

    static RestMember create(RestClient restClient, long guildId, long id) {
        return new RestMember(restClient, guildId, id);
    }

    /**
     * Returns the ID of the guild this member belongs to.
     *
     * @return The ID of the the guild this member belongs to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Returns the ID of this member.
     *
     * @return The ID of this member
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    public RestGuild guild() {
        return RestGuild.create(restClient, guildId);
    }

    public RestUser user() {
        return RestUser.create(restClient, id);
    }

    public Mono<MemberData> getData() {
        return restClient.getGuildService()
                .getGuildMember(guildId, id);
    }
}
