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

/**
 * An event listener to be registered with a client's {@link EventDispatcher}.
 * <b>WARNING: Due to an issue in TypeTools, using this class through a lambda expression *may* slow your bot down.</b>
 *
 * @param <T> The event type to handle.
 */
@FunctionalInterface
public interface IListener <T extends Event> {

	/**
	 * Invoked when the {@link EventDispatcher} this listener is registered with fires an event of type {@link T}.
	 *
	 * @param event The event object.
	 */
	void handle(T event);
}
