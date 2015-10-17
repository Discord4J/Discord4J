/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord.handle;

/**
 * @author x
 * @since 10/6/2015
 */
public interface IDispatcher {
	/**
	 * Unregisters a listener, so the listener will no longer receive events.
	 *
	 * @param listener Listener to unregister
	 */
	void unregisterListener(Object listener);

	/**
	 * Registers an IListener to receive events.
	 * @param listener Listener to register
	 */
	void registerListener(Object listener);

	/**
	 * Sends an IEvent to all listeners that listen for that specific event.
	 * @param IEvent Event to dispatch.
	 */
	void dispatch(IEvent IEvent);
}
