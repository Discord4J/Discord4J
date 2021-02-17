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

package discord4j.rest.util;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.RoleData;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import javax.management.relation.Role;

/**
 * A utility class for the sorting of {@link  discord4j.rest.entity.RestRole}.
 */

public class OrderUtil {

    /**
     * The ordering of Discord {@link Role roles}.
     * <p>
     * In Discord, two orderable entities may have the same "raw position," the position as reported by the "position"
     * field.
     * This conflict is resolved by comparing the creation time of the entities, reflected in their
     * {@link discord4j.common.util.Snowflake IDs}.
     */
    public static final Comparator<RoleData> ROLE_ORDER =
            Comparator.comparing(RoleData::position).thenComparing(OrderUtil::idFromRoleData);

    /**
     * Sorts {@link RoleData roles} according to visual ordering in Discord. Roles at the bottom of the list are first.
     * <p>
     * sorts roles according to {@link #ROLE_ORDER}.
     *
     * @param roles The roles to sort.
     * @return The sorted roles.
     */
    public static Flux<RoleData> orderRoles(Flux<RoleData> roles) {
        return roles.sort(OrderUtil.ROLE_ORDER);
    }

    /**
     * Simple utility method for getting a Snowflake ID from RoleData for comparison.
     *
     * @param data RoleData to get the ID from
     * @return A Snowflake representation of the role's ID
     */
    private static Snowflake idFromRoleData(RoleData data) {
        return Snowflake.of(data.id());
    }
}
