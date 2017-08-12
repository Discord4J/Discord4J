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

package sx.blah.discord.api.events;

import sx.blah.discord.api.IDiscordClient;

/**
 * The superclass of all events fired by a {@link EventDispatcher}. These can be events received from
 * Discord, events sent by Discord4J, or custom events.
 */
public abstract class Event {

	/**
	 * The client that the {@link EventDispatcher} this event was fired from is associated with.
	 */
	protected IDiscordClient client;

	/**
	 * Gets the client associated with this event's {@link EventDispatcher}.
	 *
	 * @return The associated client.
	 */
	public IDiscordClient getClient() {
		return client;
	}
}
