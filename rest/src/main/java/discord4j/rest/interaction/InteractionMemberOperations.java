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

package discord4j.rest.interaction;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.InteractionData;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.RestClient;
import discord4j.rest.entity.RestMember;
import discord4j.rest.entity.RestRole;
import discord4j.rest.util.PermissionSet;

import java.util.Set;
import java.util.stream.Collectors;

class InteractionMemberOperations implements InteractionMember {

    final RestClient restClient;
    final InteractionData interactionData;

    InteractionMemberOperations(RestClient restClient, InteractionData interactionData) {
        this.restClient = restClient;
        this.interactionData = interactionData;
    }

    @Override
    public MemberData getMemberData() {
        return interactionData.member().get();
    }

    @Override
    public Snowflake getGuildId() {
        return Snowflake.of(interactionData.guildId().get());
    }

    @Override
    public Snowflake getUserId() {
        return Snowflake.of(getMemberData().user().id());
    }

    @Override
    public Set<RestRole> getRoles() {
        return getMemberData().roles().stream()
                .map(id -> RestRole.create(restClient, getGuildId(), Snowflake.of(id)))
                .collect(Collectors.toSet());
    }

    @Override
    public PermissionSet getPermissions() {
        return PermissionSet.of(Long.parseLong(getMemberData().permissions().get()));
    }

    @Override
    public RestMember asRestMember() {
        return RestMember.create(restClient, getGuildId(), getUserId());
    }
}
