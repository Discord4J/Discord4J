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
import discord4j.discordjson.json.RolePositionModifyRequest;
import discord4j.discordjson.json.RoleData;
import discord4j.discordjson.json.RoleModifyRequest;
import discord4j.rest.RestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;

/**
 * Roles represent a set of permissions, unique per guild, attached to a group of users.
 */
public class RestRole {

    private final RestClient restClient;
    private final long guildId;
    private final long id;

    private RestRole(RestClient restClient, long guildId, long id) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.id = id;
    }

    /**
     * Create a {@link RestRole} for the given parameters. This method does not perform any API request.
     *
     * @param restClient the client to make API requests
     * @param id the ID of this entity
     * @return a {@code RestRole} represented by the given parameters.
     */
    public static RestRole create(RestClient restClient, Snowflake guildId, Snowflake id) {
        return new RestRole(restClient, guildId.asLong(), id.asLong());
    }

    static RestRole create(RestClient restClient, long guildId, long id) {
        return new RestRole(restClient, guildId, id);
    }

    /**
     * Returns the ID of the guild this role belongs to.
     *
     * @return The ID of the the guild this role belongs to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Returns the ID of this role.
     *
     * @return The ID of this role
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    /**
     * Return the guild tied to this role as a REST operations handle.
     *
     * @return the parent guild for this role
     */
    public RestGuild guild() {
        return RestGuild.create(restClient, guildId);
    }

    /**
     * Requests to edit this role.
     *
     * @param request A {@link RoleModifyRequest} to parameterize this request.
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link RoleData}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<RoleData> edit(final RoleModifyRequest request, @Nullable String reason) {
        return restClient.getGuildService().modifyGuildRole(guildId, id, request, reason);
    }

    /**
     * Requests to delete this role while optionally specifying the reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the role has been deleted. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return restClient.getGuildService().deleteGuildRole(guildId, id, reason);
    }

    /**
     * Requests to change this role's position.
     *
     * @param position The position to change for this role.
     * @param reason The reason, if present.
     * @return A {@link Flux} that continually emits all the {@link RoleData roles} associated to this role's
     * guild. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<RoleData> changePosition(final int position, @Nullable final String reason) {
        final RolePositionModifyRequest[] requests = {RolePositionModifyRequest.builder()
            .id(Snowflake.asString(id))
            .positionOrNull(position)
            .build()};
        return restClient.getGuildService().modifyGuildRolePositions(guildId, requests, reason);
    }

    /**
     * Requests to change this role's position.
     *
     * @param position The position to change for this role.
     * @return A {@link Flux} that continually emits all the {@link RoleData roles} associated to this role's
     * guild. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<RoleData> changePosition(final int position) {
        return changePosition(position, null);
    }

    /**
     * Retrieve this role's data upon subscription.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link RoleData} belonging to this role.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<RoleData> getData() {
        return restClient.getGuildService()
                .getGuildRoles(guildId)
                .filter(response -> Snowflake.asLong(response.id()) == id)
                .singleOrEmpty();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RestRole restRole = (RestRole) o;
        return guildId == restRole.guildId && id == restRole.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, id);
    }
}
