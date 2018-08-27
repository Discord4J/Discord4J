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
package discord4j.core.object.entity;

import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;

/** A Discord channel associated to a {@link Guild}. */
public interface GuildChannel extends Channel {

    /**
     * Gets the ID of the guild this channel is associated to.
     *
     * @return The ID of the guild this channel is associated to.
     */
    Snowflake getGuildId();

    /**
     * Requests to retrieve the guild this channel is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this channel is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Guild> getGuild();

    /**
     * Gets the permission overwrites for this channel.
     *
     * @return The permission overwrites for this channel.
     */
    Set<ExtendedPermissionOverwrite> getPermissionOverwrites();

    /**
     * Gets the permission overwrite targeting the given member.
     *
     * @param memberId The ID of the member to get the overwrite for.
     * @return The permission overwrite targeting the given member.
     */
    Optional<ExtendedPermissionOverwrite> getOverwriteForMember(Snowflake memberId);

    /**
     * Gets the permission overwrite targeting the given role.
     *
     * @param roleId The ID of the role to get the overwrite for.
     * @return The permission overwrite targeting the given role.
     */
    Optional<ExtendedPermissionOverwrite> getOverwriteForRole(Snowflake roleId);

    /**
     * Gets the permissions for the given member, taking into account permission overwrites in this channel.
     *
     * @param memberId The ID of the member to get permissions for.
     * @return The permissions for the given member.
     */
    Mono<PermissionSet> getEffectivePermissions(Snowflake memberId);

    /**
     * Gets the name of the channel.
     *
     * @return The name of the channel.
     */
    String getName();

    /**
     * Gets the raw position of the channel as exposed by Discord. This may or may not be accurate with relativity to
     * other channels in the guild.
     *
     * @return The raw position of the channel.
     */
    int getRawPosition();

    /**
     * Requests to retrieve the position of the channel relative to other channels in the guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits the position of the channel. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    Mono<Integer> getPosition();

    /**
     * Requests to add a permission overwrite for the given member.
     *
     * @param memberId The ID of the member to add the overwrite for.
     * @param overwrite The overwrite to add.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the guild has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Void> addMemberOverwrite(Snowflake memberId, PermissionOverwrite overwrite);

    /**
     * Requests to add a permission overwrite for the given role.
     *
     * @param roleId The ID of the role to add the overwrite for.
     * @param overwrite The overwrite to add.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the guild has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Void> addRoleOverwrite(Snowflake roleId, PermissionOverwrite overwrite);
}
