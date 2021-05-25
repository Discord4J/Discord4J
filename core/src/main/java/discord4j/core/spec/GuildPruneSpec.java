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

package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.rest.util.Multimap;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Spec used to begin a prune operation.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#begin-guild-prune">Begin Guild Prune</a>
 */
public class GuildPruneSpec implements Spec<Multimap<String, Object>> {

    private final Multimap<String, Object> map = new Multimap<>();
    @Nullable
    private String reason;

    /**
     * Set the number of days to prune.
     *
     * @param days the number of days
     * @return this spec
     */
    public GuildPruneSpec setDays(int days) {
        map.set("days", days);
        return this;
    }

    /**
     * Include a role in the prune request. By default, prune will not remove users with roles, therefore this method
     * can be used to include such users.
     *
     * @param roleId the role ID to include for prune
     * @return this spec
     */
    public GuildPruneSpec addRole(Snowflake roleId) {
        map.add("include_roles", roleId.asString());
        return this;
    }

    /**
     * Include multiple roles in the prune request. By default, prune will not remove users with roles, therefore
     * this method can be used to include such users.
     *
     * @param roleIds the role IDs to include for prune
     * @return this spec
     */
    public GuildPruneSpec addRoles(Collection<Snowflake> roleIds) {
        map.addAll("include_roles", roleIds.stream().map(Snowflake::asString).collect(Collectors.toList()));
        return this;
    }

    /**
     * Set whether the number of pruned members is returned when this actions completes. By default this is enabled,
     * but this is discouraged on large guilds so you can set it to {@code false}.
     *
     * @param enable whether the pruned total is returned, if {@code false}, the prune action will eventually
     * complete with an empty {@link Mono}.
     * @return this spec
     */
    public GuildPruneSpec setComputePruneCount(boolean enable) {
        map.set("compute_prune_count", enable);
        return this;
    }

    public GuildPruneSpec setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public Multimap<String, Object> asRequest() {
        return map;
    }
}
