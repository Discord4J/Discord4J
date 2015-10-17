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

package sx.blah.discord.handle.impl;

import net.jodah.typetools.TypeResolver;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.IDispatcher;
import sx.blah.discord.handle.IEvent;
import sx.blah.discord.handle.IListener;

import java.util.*;

/**
 * @author x
 * @since 10/6/2015
 */
public class EventDispatcher implements IDispatcher {
	// holy generics, batman!
	private Map<Class<? extends IEvent>, List<IListener>> listenerMap = new HashMap<>();

	/**
	 * Unregisters a listener, so the listener will no longer receive events.
	 *
	 * @param listener Listener to unregister
	 */
	@Override public void unregisterListener(Object listener) {
		if(listener instanceof IListener)
			for (Map.Entry<Class<? extends IEvent>, List<IListener>> entry : listenerMap.entrySet()) {
				entry.getValue().stream().filter(listener1 -> listener1.equals(listener)).forEach(listener1 -> entry.getValue().remove(listener1));
			}
	}

	/**
	 * Registers an IListener to receive events.
	 * @param listener Listener to register
	 */
	@Override public void registerListener(Object listener) {
		if(listener instanceof IListener) {
			Class<?> rawType = TypeResolver.resolveRawArgument(IListener.class, listener.getClass());
			if (IEvent.class.isAssignableFrom(rawType)) {
				Class<? extends IEvent> eventType = (Class<? extends IEvent>) rawType;
				if (listenerMap.containsKey(eventType)) {
					listenerMap.get(eventType).add((IListener) listener);
				} else {
					listenerMap.put(eventType, new ArrayList<>(Collections.singletonList((IListener) listener)));
				}
				Discord4J.logger.debug("Registered IListener for {}. Map size now {}.", eventType.getSimpleName(), listenerMap.size());
			}
		}
	}

	/**
	 * Sends an IEvent to all listeners that listen for that specific event.
	 * @param event The event to dispatch.
	 */
	@Override public void dispatch(IEvent event) {
		Class<? extends IEvent> eventType = event.getClass();
		Discord4J.logger.debug("Dispatching event of type {}.", eventType.getSimpleName());
		if (listenerMap.containsKey(eventType)) {
			for (IListener listener : listenerMap.get(eventType)) {
				listener.receive(event);
			}
		}
	}
}
