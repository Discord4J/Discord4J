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

import discord4j.discordjson.json.InviteData;
import discord4j.rest.RestClient;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;

/**
 * Represents a code that can be used to add a user to a guild.
 */
public class RestInvite {

    private final RestClient restClient;
    private final String code;

    private RestInvite(RestClient restClient, String code) {
        this.restClient = restClient;
        this.code = code;
    }

    /**
     * Create a {@link RestInvite} with the given parameters. This method does not perform any API request.
     *
     * @param restClient REST API resources
     * @param code the ID of this entity
     * @return a {@code RestInvite} represented by the given parameters.
     */
    public static RestInvite create(RestClient restClient, String code) {
        return new RestInvite(restClient, code);
    }

    /**
     * Gets the invite code.
     *
     * @return The invite code
     */
    public String getCode() {
        return code;
    }

    public Mono<InviteData> getData() {
        return restClient.getInviteService().getInvite(code);
    }

    public Mono<InviteData> delete(@Nullable String reason) {
        return restClient.getInviteService().deleteInvite(code, reason);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RestInvite that = (RestInvite) o;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
