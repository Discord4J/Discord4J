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
package sx.blah.discord.api.events.registery;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolver.Unknown;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.EventPriority;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.api.events.handler.EventHandler;
import sx.blah.discord.api.events.handler.ListenerEventHandler;
import sx.blah.discord.util.LogMarkers;

/**
 * Represents a handlers list ordered by priority and updated upon registering or unregistering
 * handlers.
 */
public final class EventRegistry {

	/**
	 * The handlers list for each of the present priorities.
	 */
	private final Map<EventPriority, List<EventHandler>> handlersList;

	/**
	 * A linear execution table, used to make the execution process faster.
	 */
	private EventHandler[] table;

	/**
	 * Constructs a new {@link EventRegistry} object instance.
	 */
	public EventRegistry() {
		handlersList = new EnumMap<EventPriority, List<EventHandler>>(EventPriority.class);
		for (EventPriority priority : EventPriority.values()) {
			handlersList.put(priority, new CopyOnWriteArrayList<EventHandler>());
		}
	}

	/**
	 * Creates a linear table for best performance at iterating throughout the handlers.
	 */
	private void createTable() {
		// calculate the table size.
		int size = 0;
		for (EventPriority priority : EventPriority.values()) {
			size += handlersList.get(priority).size();
		}
		// create the table.
		EventHandler[] table = this.table = new EventHandler[size];
		// populate the table.
		int index = 0;
		for (EventPriority priority : EventPriority.values()) {
			List<EventHandler> handlers = handlersList.get(priority);
			for (EventHandler handler : handlers) {
				table[index++] = handler;
			}
		}
	}

	/**
	 * Registers the specified {@link EventHandler} into this {@link EventRegistry}.
	 * 
	 * @param handler
	 *            the handler to register.
	 */
	public void register(EventHandler handler) {
		List<EventHandler> handlers = handlersList.get(handler.getPriority());
		if (handlers.contains(handler)) {
			return;
		}
		handlers.add(handler);
		table = null;
		Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Registered event handler {}", handler);
	}

	/**
	 * Unregisters the specified {@link EventHandler} from this register.
	 * 
	 * @param handler
	 *            the handler to unregister.
	 */
	public void unregister(EventHandler handler) {
		List<EventHandler> handlers = handlersList.get(handler.getPriority());
		if (!handlers.contains(handler)) {
			return;
		}
		handlers.remove(handler);
		table = null;
		Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered event handler {}", handler);
	}

	// TODO: #registerListener method, unnecessary but someone could find it useful.

	/**
	 * Unregisters the specified {@link IListener} from this {@link EventRegistry}.
	 * 
	 * @param listener
	 *            the listener to unregister.
	 */
	@SuppressWarnings("rawtypes")
	public void unregisterListener(IListener<? extends Event> listener) {
		Class<?> rawType = TypeResolver.resolveRawArgument(IListener.class, listener.getClass());
		// checks whether a parameter argument is present or not.
		if (rawType == Unknown.class)
			return;
		for (EventHandler handler : getHandlers()) {
			if (handler instanceof ListenerEventHandler) {
				if (((ListenerEventHandler) handler).getListener() == listener) {
					unregister(handler);
					table = null;
					break;// supposing each instance can be only registered once.
				}
			}
		}

	}

	/**
	 * Unregisters all the event handlers within this registry.
	 */
	public void unregisterAll() {
		for (List<EventHandler> handlers : handlersList.values()) {
			handlers.clear();
		}
		table = null;
	}

	/**
	 * Gets the handlers list within this registry ordered by priority from the highest to lowest.
	 * 
	 * @return the handlers list within this registry.
	 */
	public EventHandler[] getHandlers() {
		if (table == null)
			createTable();
		return table;
	}

}
