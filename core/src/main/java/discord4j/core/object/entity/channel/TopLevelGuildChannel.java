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
package discord4j.core.object.entity.channel;

import discord4j.common.util.Snowflake;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Member;
import discord4j.core.util.OrderUtil;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;
import java.util.Set;

/** A Discord channel in a guild that isn't a thread. */
public interface TopLevelGuildChannel extends GuildChannel {

    @Override
    Set<ExtendedPermissionOverwrite> getPermissionOverwrites();

    @Override
    Optional<ExtendedPermissionOverwrite> getOverwriteForMember(Snowflake memberId);

    @Override
    Optional<ExtendedPermissionOverwrite> getOverwriteForRole(Snowflake roleId);

    @Override
    default Mono<Void> addMemberOverwrite(final Snowflake memberId, final PermissionOverwrite overwrite) {
        return addMemberOverwrite(memberId, overwrite, null);
    }

    @Override
    Mono<Void> addMemberOverwrite(Snowflake memberId, PermissionOverwrite overwrite, @Nullable String reason);

    @Override
    default Mono<Void> addRoleOverwrite(final Snowflake roleId, final PermissionOverwrite overwrite) {
        return addRoleOverwrite(roleId, overwrite, null);
    }

    @Override
    Mono<Void> addRoleOverwrite(Snowflake roleId, PermissionOverwrite overwrite, @Nullable String reason);

    @Override
    int getRawPosition();

    @Override
    Mono<Integer> getPosition();
}
