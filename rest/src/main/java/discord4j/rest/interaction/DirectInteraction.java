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
import discord4j.discordjson.json.UserData;

/**
 * An {@link RestInteraction} originated from a DM channel, available for global commands and users that share a guild
 * with a bot associated with this application. Allows access to the interaction user.
 *
 * @see Interactions
 */
@Experimental
public interface DirectInteraction extends RestInteraction {

    /**
     * Return the raw user data that created this interaction.
     *
     * @return a user data object
     */
    UserData getUserData();
}
