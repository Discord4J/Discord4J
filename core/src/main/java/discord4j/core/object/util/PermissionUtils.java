package discord4j.core.object.util;

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Utilities for calculating effective permissions.
 */
public class PermissionUtils {
    /**
     * Requests and calculates the effective permissions of a {@link Member} in a {@link GuildChannel}. This accounts
     * for guild ownership, {@link Permission#ADMINISTRATOR}, and channel overwrites.
     *
     * @param channel The {@link GuildChannel} in which to check permissions.
     * @param member The {@link Member} for which to check permissions.
     * @return A {@link Mono} where, upon successful completion, emits the effective permissions of member in channel.
     * If an error is received, it is emitted through the {@code Mono}.
     *
     * @see <a href="https://discordapp.com/developers/docs/topics/permissions#permission-overwrites">Permission Overwrites</a>
     * @see PermissionSet
     * @see PermissionOverwrite
     */
    public static Mono<PermissionSet> effectivePermissions(GuildChannel channel, Member member) {

        return channel.getGuild().flatMap(g ->                                 // Get Guild instance
            g.getEveryoneRole().map(Role::getPermissions).flatMap(everyone ->  // Get everyone role perms
                effectivePermissions(member.getId(), g.getOwnerId(),
                    everyone, member.getRoles().map(Role::getPermissions),
                    channel.getPermissionOverwrites(), member.getRoleIds()))
        );
    }

    /**
     * Calculates effective permissions from the given parameters. This accounts
     * for guild ownership, {@link Permission#ADMINISTRATOR}, and channel overwrites.
     *
     *
     * @return A {@link Mono} where, upon successful completion, emits the effective permissions of member in channel.
     * If an error is received, it is emitted through the {@code Mono}.
     *
     * @see <a href="https://discordapp.com/developers/docs/topics/permissions#permission-overwrites">Permission Overwrites</a>
     * @see PermissionSet
     * @see PermissionOverwrite
     */
    public static Mono<PermissionSet> effectivePermissions(Snowflake memberId, Snowflake ownerId,
                                                    PermissionSet everyonePerms, Flux<PermissionSet> rolePerms,
                                                    Set<PermissionOverwrite> channelOverwrites, Set<Snowflake> roleIds) {

        if (ownerId.equals(memberId)) { // Check if member is owner of g
            return Mono.just(PermissionSet.all());
        }

        return calculateBasePerms(everyonePerms, rolePerms)
            .flatMap(basePerms -> {
                if (basePerms.contains(Permission.ADMINISTRATOR)) // Check if user has admin
                    return Mono.just(PermissionSet.all());        // If yes, they have all permissions
                // Otherwise do channel overwrites (below)
                return calculateOverwrites(basePerms, channelOverwrites, roleIds, memberId);
            });
    }

    /* Utility method to calculate base guild-level PermissionSet */
    static Mono<PermissionSet> calculateBasePerms(PermissionSet everyonePerms, Flux<PermissionSet> rolePermissions) {
        return rolePermissions.reduce(everyonePerms, PermissionSet::or);
    }

    /* Utility method to calculate PermissionSet after applying a number of overwrites */
    static Mono<PermissionSet> calculateOverwrites(PermissionSet basePerms, Set<PermissionOverwrite> overwrites,
                                                   Set<Snowflake> roleIds, Snowflake memberId) {
        Flux<PermissionOverwrite> over = Flux.fromIterable(overwrites);
        return over
            // Get channel-level overwrites for member's roles
            .filter(po -> po.getRoleId().map(roleIds::contains).orElse(false))
            // Apply role permission overwrites
            .reduce(basePerms, PermissionUtils::applyOverwrite)
            .flatMap(baseRolePerms -> over
                // Get channel-level overwrites for member directly
                .filter(po -> po.getUserId().map(memberId::equals).orElse(false))
                // Apply member permission overwrites
                .reduce(baseRolePerms, PermissionUtils::applyOverwrite)
            );

    }

    /* Utility method for applying a PermissionOverwrite to a PermissionSet */
    static PermissionSet applyOverwrite(PermissionSet ps, PermissionOverwrite po) {
        return ps.and(po.getDenied().not()).or(po.getAllowed());
    }
}
