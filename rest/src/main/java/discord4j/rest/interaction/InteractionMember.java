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

package discord4j.rest.interaction;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.entity.RestMember;
import discord4j.rest.entity.RestRole;
import discord4j.rest.util.PermissionSet;

import java.util.Set;

/**
 * A member that initiated a specific interaction.
 */
public interface InteractionMember {

    /**
     * Return the raw member data that created this interaction.
     *
     * @return a member data object
     */
    MemberData getMemberData();

    /**
     * Return the guild ID where this interaction was created.
     *
     * @return this interaction Snowflake guild ID
     */
    Snowflake getGuildId();

    /**
     * Return the user ID who created this interaction.
     *
     * @return this interaction Snowflake user ID
     */
    Snowflake getUserId();

    /**
     * Return the role set for this interaction member.
     *
     * @return the set of {@link RestRole} belonging to this member
     */
    Set<RestRole> getRoles();

    /**
     * Return the effective permission set for this interaction member.
     *
     * @return a {@link PermissionSet} for this member
     */
    PermissionSet getPermissions();

    /**
     * Return a REST operations handler for this interaction member. Can be followed by {@link RestMember#guild()} or
     * {@link RestMember#user()} to access their guild or associated user REST entity.
     *
     * @return a {@link RestMember} facade to operate on this member at the REST API level
     */
    RestMember asRestMember();
}
