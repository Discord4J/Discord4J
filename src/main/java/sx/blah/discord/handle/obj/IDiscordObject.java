/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordUtils;

import java.time.LocalDateTime;

/**
 * This represents a generic discord object.
 */
public interface IDiscordObject<SELF extends IDiscordObject<SELF>> extends IIDLinkedObject { //The SELF thing is just a hack to get copy() to work correctly because self types don't exist in java >.>

	/**
	 * Gets the {@link IDiscordClient} instance this object belongs to.
	 *
	 * @return The client instance.
	 */
	IDiscordClient getClient();

	/**
	 * Get the {@link IShard} instance this object belongs to.
	 */
	IShard getShard();

	/**
	 * Gets the {@link LocalDateTime} this object was created at. This is calculated by reversing the snowflake
	 * algorithm on the object's id.
	 *
	 * @return The creation date of this object.
	 */
	default LocalDateTime getCreationDate() {
		return DiscordUtils.getSnowflakeTimeFromID(getLongID());
	}

	/**
	 * Creates a new instance of this object with all the current properties.
	 *
	 * @return The copied instance of this object.
	 */
	SELF copy();
}
