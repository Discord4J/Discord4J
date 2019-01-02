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
package discord4j.core.util;

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PermissionUtil {

    /**
     * Computes the permissions granted by a member's roles.
     *
     * @param everyonePerms The permissions granted by the everyone role.
     * @param rolePerms The list of permissions granted by each of the member's other roles.
     * @return The combined permissions of everyonePerms and rolePerms.
     *
     * @see discord4j.core.object.entity.Member#getBasePermissions() Member#getBasePermissions()
     */
    public static PermissionSet computeBasePermissions(PermissionSet everyonePerms, List<PermissionSet> rolePerms) {
        return rolePerms.stream().reduce(everyonePerms, PermissionSet::or);
    }

    /**
     * Computes the permissions of a member taking into account permission overwrites.
     *
     * @param base The base permissions granted by the member's roles.
     * @param everyoneOverwrite The overwrite applied to the everyone role in the channel.
     * @param roleOverwrites The overwrites applied to every other role in the channel.
     * @param memberOverwrite The overwrite applied to the member in the channel.
     * @return The permissions with overwrites taken into account.
     *
     * @see discord4j.core.object.entity.GuildChannel#getEffectivePermissions(discord4j.core.object.util.Snowflake)
     * GuildChannel#getEffectivePermissions(Snowflake)
     */
    public static PermissionSet computePermissions(PermissionSet base, @Nullable PermissionOverwrite everyoneOverwrite,
                                                   List<PermissionOverwrite> roleOverwrites,
                                                   @Nullable PermissionOverwrite memberOverwrite) {
        if (base.contains(Permission.ADMINISTRATOR)) {
            return PermissionSet.all();
        }

        List<PermissionOverwrite> allOverwrites = new ArrayList<>();
        if (everyoneOverwrite != null) allOverwrites.add(everyoneOverwrite);
        allOverwrites.addAll(roleOverwrites);
        if (memberOverwrite != null) allOverwrites.add(memberOverwrite);

        return allOverwrites.stream().reduce(base, PermissionUtil::applyOverwrite, PermissionSet::or); // combiner is never used
    }

    private static PermissionSet applyOverwrite(PermissionSet base, PermissionOverwrite overwrite) {
        return base.and(overwrite.getDenied().not()).or(overwrite.getAllowed());
    }
}
