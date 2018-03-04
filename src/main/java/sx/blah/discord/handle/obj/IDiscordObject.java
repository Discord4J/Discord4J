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

import java.time.Instant;

/**
 * An object that is identifiable by a unique snowflake ID and belongs to a {@link IDiscordClient}.
 */
public interface IDiscordObject<SELF extends IDiscordObject<SELF>> extends IIDLinkedObject { //The SELF thing is just a hack to get copy() to work correctly because self types don't exist in java >.>

	/**
	 * Gets the client the object belongs to.
	 *
	 * @return The client the object belongs to.
	 */
	IDiscordClient getClient();

	/**
	 * Gets the shard the object belongs to.
	 *
	 * @return The shard the object belongs to.
	 */
	IShard getShard();

	/**
	 * Gets the time at which the object was created.
	 *
	 * @return The time at which the object was created.
	 */
	default Instant getCreationDate() {
		return DiscordUtils.getSnowflakeTimeFromID(getLongID());
	}

	/**
	 * Creates a new instance of the object with all the current properties.
	 *
	 * @return The copied instance of the object.
	 */
	SELF copy();
}
