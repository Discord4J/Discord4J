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

    public static PermissionSet computeBasePermissions(PermissionSet everyonePerms, List<PermissionSet> rolePerms) {
        return rolePerms.stream().reduce(everyonePerms, PermissionSet::or);
    }

    public static PermissionSet computePermissions(PermissionSet base, List<PermissionOverwrite> roleOverwrites,
                                                   @Nullable PermissionOverwrite memberOverwrite) {
        if (base.contains(Permission.ADMINISTRATOR)) {
            return PermissionSet.all();
        }

        List<PermissionOverwrite> allOverwrites = new ArrayList<>(roleOverwrites);
        if (memberOverwrite != null) allOverwrites.add(memberOverwrite);

        return allOverwrites.stream().reduce(base, PermissionUtil::applyOverwrite, PermissionSet::or); // combiner is never used
    }

    private static PermissionSet applyOverwrite(PermissionSet base, PermissionOverwrite overwrite) {
        return base.and(overwrite.getDenied().not()).or(overwrite.getAllowed());
    }
}
