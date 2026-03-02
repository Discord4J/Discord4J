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

package discord4j.core.spec.legacy;

import discord4j.common.util.Snowflake;
import discord4j.rest.util.Multimap;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * LegacySpec used to retrieve the number of members that would be removed in a prune operation.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-prune-count">Get Guild Prune Count</a>
 */
public class LegacyGuildPruneCountSpec implements LegacySpec<Multimap<String, Object>> {

    private final Multimap<String, Object> map = new Multimap<>();
    @Nullable
    private String reason;

    /**
     * Set the number of days to count prune for.
     *
     * @param days the number of days
     * @return this spec
     */
    public LegacyGuildPruneCountSpec setDays(int days) {
        map.set("days", days);
        return this;
    }

    /**
     * Include a role in the prune count request. By default, prune will not remove users with roles, therefore this
     * method can be used to include such users.
     *
     * @param roleId the role ID to include
     * @return this spec
     */
    public LegacyGuildPruneCountSpec addRole(Snowflake roleId) {
        map.add("include_roles", roleId.asString());
        return this;
    }

    /**
     * Include multiple roles in the prune count request. By default, prune will not remove users with roles,
     * therefore this method can be used to include such users.
     *
     * @param roleIds the role IDs to include
     * @return this spec
     */
    public LegacyGuildPruneCountSpec addRoles(Collection<Snowflake> roleIds) {
        map.addAll("include_roles", roleIds.stream().map(Snowflake::asString).collect(Collectors.toList()));
        return this;
    }

    @Override
    public Multimap<String, Object> asRequest() {
        return map;
    }
}
