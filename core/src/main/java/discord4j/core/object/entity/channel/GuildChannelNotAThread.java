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

// TODO naming
public interface GuildChannelNotAThread extends GuildChannel {

    /**
     * Gets the permission overwrites for this channel.
     *
     * @return The permission overwrites for this channel.
     */
    @Override
    Set<ExtendedPermissionOverwrite> getPermissionOverwrites();

    /**
     * Gets the permission overwrite targeting the given member.
     *
     * @param memberId The ID of the member to get the overwrite for.
     * @return The permission overwrite targeting the given member.
     */
    @Override
    Optional<ExtendedPermissionOverwrite> getOverwriteForMember(Snowflake memberId);

    /**
     * Gets the permission overwrite targeting the given role.
     *
     * @param roleId The ID of the role to get the overwrite for.
     * @return The permission overwrite targeting the given role.
     */
    @Override
    Optional<ExtendedPermissionOverwrite> getOverwriteForRole(Snowflake roleId);

    /**
     * Requests to add a permission overwrite for the given member.
     *
     * @param memberId The ID of the member to add the overwrite for.
     * @param overwrite The overwrite to add.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; If an error is received, it is emitted
     * through the {@code Mono}.
     */
    @Override
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
    @Override
    Mono<Void> addMemberOverwrite(Snowflake memberId, PermissionOverwrite overwrite, @Nullable String reason);

    /**
     * Requests to add a permission overwrite for the given role.
     *
     * @param roleId The ID of the role to add the overwrite for.
     * @param overwrite The overwrite to add.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; If an error is received, it is emitted
     * through the {@code Mono}.
     */
    @Override
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
    @Override
    Mono<Void> addRoleOverwrite(Snowflake roleId, PermissionOverwrite overwrite, @Nullable String reason);

    /**
     * Gets the permissions for the given member, taking into account permission overwrites in this channel.
     *
     * @param memberId The ID of the member to get permissions for.
     * @return The permissions for the given member.
     */
    @Override
    Mono<PermissionSet> getEffectivePermissions(Snowflake memberId);

    /**
     * Gets the permissions for the given member, taking into account permission overwrites in this channel.
     *
     * @param member The member to get permissions for.
     * @return The permissions for the given member.
     */
    @Override
    Mono<PermissionSet> getEffectivePermissions(Member member);

    /**
     * Gets the raw position of the channel as exposed by Discord. This may or may not be accurate with relativity to
     * other channels in the guild.
     *
     * @return The raw position of the channel.
     */
    @Override
    int getRawPosition();

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
    @Override
    Mono<Integer> getPosition();

}
