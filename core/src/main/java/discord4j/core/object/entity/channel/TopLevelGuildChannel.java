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
import discord4j.core.object.entity.Guild;
import discord4j.core.util.OrderUtil;
import discord4j.discordjson.json.PermissionsEditRequest;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** A Discord channel in a guild that isn't a thread. */
public interface TopLevelGuildChannel extends GuildChannel {

    /**
     * Gets the permission overwrites for this channel.
     *
     * @return The permission overwrites for this channel.
     */
    default Set<ExtendedPermissionOverwrite> getPermissionOverwrites() {
        return getData().permissionOverwrites().toOptional()
                .map(permissionOverwrites -> {
                    long guildId = getGuildId().asLong();
                    long channelId = getId().asLong();
                    return permissionOverwrites.stream()
                            .map(overwriteData -> new ExtendedPermissionOverwrite(getClient(), overwriteData, guildId, channelId))
                            .collect(Collectors.toSet());
                })
                .orElse(Collections.emptySet());
    }

    /**
     * Gets the permission overwrite targeting the given member.
     *
     * @param memberId The ID of the member to get the overwrite for.
     * @return The permission overwrite targeting the given member.
     */
    default Optional<ExtendedPermissionOverwrite> getOverwriteForMember(Snowflake memberId) {
        return getPermissionOverwrites().stream()
                .filter(overwrite -> overwrite.getMemberId().map(memberId::equals).orElse(false))
                .findFirst();
    }

    /**
     * Gets the permission overwrite targeting the given role.
     *
     * @param roleId The ID of the role to get the overwrite for.
     * @return The permission overwrite targeting the given role.
     */
    default Optional<ExtendedPermissionOverwrite> getOverwriteForRole(Snowflake roleId) {
        return getPermissionOverwrites().stream()
                .filter(overwrite -> overwrite.getRoleId().map(roleId::equals).orElse(false))
                .findFirst();
    }

    /**
     * Requests to add a permission overwrite for the given member.
     *
     * @param memberId The ID of the member to add the overwrite for.
     * @param overwrite The overwrite to add.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; If an error is received, it is emitted
     * through the {@code Mono}.
     */
    default Mono<Void> addMemberOverwrite(final Snowflake memberId, final PermissionOverwrite overwrite) {
        return addMemberOverwrite(memberId, overwrite, null);
    }

    /**
     * Requests to add a permission overwrite for the given member while optionally specifying a reason.
     *
     * @param memberId The ID of the member to add the overwrite for.
     * @param overwrite The overwrite to add.
     * @param reason The reason, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; If an error is received, it is emitted
     * through the {@code Mono}.
     */
    default Mono<Void> addMemberOverwrite(Snowflake memberId, PermissionOverwrite overwrite, @Nullable String reason) {
        PermissionSet allow = overwrite.getAllowed();
        PermissionSet deny = overwrite.getDenied();
        PermissionsEditRequest request = PermissionsEditRequest.builder()
                .allow(allow.getRawValue())
                .deny(deny.getRawValue())
                .type(PermissionOverwrite.Type.MEMBER.getValue())
                .build();

        return getClient().getRestClient().getChannelService()
                .editChannelPermissions(getId().asLong(), memberId.asLong(), request, reason);
    }

    /**
     * Requests to add a permission overwrite for the given role.
     *
     * @param roleId The ID of the role to add the overwrite for.
     * @param overwrite The overwrite to add.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; If an error is received, it is emitted
     * through the {@code Mono}.
     */
    default Mono<Void> addRoleOverwrite(final Snowflake roleId, final PermissionOverwrite overwrite) {
        return addRoleOverwrite(roleId, overwrite, null);
    }

    /**
     * Requests to add a permission overwrite for the given role while optionally specifying a reason.
     *
     * @param roleId The ID of the role to add the overwrite for.
     * @param overwrite The overwrite to add.
     * @param reason The reason, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; If an error is received, it is emitted
     * through the {@code Mono}.
     */
    default Mono<Void> addRoleOverwrite(Snowflake roleId, PermissionOverwrite overwrite, @Nullable String reason) {
        PermissionSet allow = overwrite.getAllowed();
        PermissionSet deny = overwrite.getDenied();
        PermissionsEditRequest request = PermissionsEditRequest.builder()
                .allow(allow.getRawValue())
                .deny(deny.getRawValue())
                .type(PermissionOverwrite.Type.ROLE.getValue())
                .build();

        return getClient().getRestClient().getChannelService()
                .editChannelPermissions(getId().asLong(), roleId.asLong(), request, reason);
    }

    /**
     * Gets the raw position of the channel as exposed by Discord. This may or may not be accurate with relativity to
     * other channels in the guild.
     *
     * @return The raw position of the channel.
     */
    default int getRawPosition() {
        return getData().position().toOptional()
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Requests to retrieve the position of the channel relative to other channels in the guild.
     * <p>
     * This is determined by the index of this channel in the {@link OrderUtil#orderGuildChannels(Flux) sorted} list of channels of the guild.
     * <p>
     * Warning: Because this method must sort the guild channels, it is inefficient to make repeated invocations for the
     * same set of channels (meaning that channels haven't been added or removed). For example, instead of writing:
     * <pre>
     * {@code
     * guild.getChannels()
     *   .flatMap(c -> c.getPosition().map(pos -> c.getName() + " : " + pos))
     * }
     * </pre>
     * It would be much more efficient to write:
     * <pre>
     * {@code
     * guild.getChannels()
     *   .transform(OrderUtil::orderGuildChannels)
     *   .index((pos, c) -> c.getName() + " : " + pos)
     * }
     * </pre>
     *
     * @return A {@link Mono} where, upon successful completion, emits the position of the channel. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    default Mono<Integer> getPosition() {
        return getGuild()
                .flatMapMany(Guild::getChannels)
                .transform(OrderUtil::orderGuildChannels)
                .collectList()
                .map(channels -> channels.indexOf(this));
    }
}
