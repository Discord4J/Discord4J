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

import com.darichey.discordjson.json.ImmutablePositionModifyRequest;
import com.darichey.discordjson.json.PositionModifyRequest;
import com.darichey.discordjson.json.RoleData;
import com.darichey.discordjson.json.RoleModifyRequest;
import discord4j.rest.RestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class RestRole {

    private final RestClient restClient;
    private final long guildId;
    private final long id;

    public RestRole(RestClient restClient, long guildId, long id) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.id = id;
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
     * @return A {@link Flux} that continually emits all the {@link RoleData roles} associated to this role's
     * guild. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<RoleData> changePosition(final int position) {
        final PositionModifyRequest[] requests = {ImmutablePositionModifyRequest.of(Long.toUnsignedString(id), position) };
        return restClient.getGuildService().modifyGuildRolePositions(guildId, requests);
    }

    public Mono<RoleData> getData() {
        return restClient.getGuildService()
                .getGuildRoles(guildId)
                .filter(response -> Long.parseUnsignedLong(response.id()) == id)
                .singleOrEmpty();
    }
}
