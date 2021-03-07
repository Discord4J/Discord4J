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

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;

/**
 * An {@link RestInteraction} originated from a guild, giving access to specific guild ID and interaction member data.
 *
 * @see Interactions
 */
@Experimental
public interface GuildInteraction extends RestInteraction {

    /**
     * Return the guild ID where this interaction was created.
     *
     * @return this interaction Snowflake guild ID
     */
    Snowflake getGuildId();

    /**
     * Return this interaction member.
     *
     * @return an object with methods to operate on this interaction member
     */
    InteractionMember getInteractionMember();
}
