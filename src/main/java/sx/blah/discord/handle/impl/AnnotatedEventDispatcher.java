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

import sx.blah.discord.handle.IDispatcher;
import sx.blah.discord.handle.IEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is not necessarily recommended, as
 * reflection is detrimental to performance.
 *
 * @author qt
 * @since 9:05 PM, 10/15/15
 * Project: Discord4J
 */
public class AnnotatedEventDispatcher implements IDispatcher {
    private final Map<Object, List<EventData>> map = new HashMap<>();

    @Override public void unregisterListener(Object listener) {
        if(map.containsKey(listener)) {
            map.remove(listener);
        }
    }

    @Override public void registerListener(Object listener) {
        List<EventData> events = new ArrayList<>();
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventSubscriber.class)
                    && method.getParameterCount() == 1
                    && IEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                method.setAccessible(true);
                events.add(new EventData(method, (Class<? extends IEvent>) method.getParameterTypes()[0]));
            }
        }
        if(!events.isEmpty()) {
            map.put(listener, events);
        }
    }

    @Override public void dispatch(IEvent event) {
        for(Map.Entry<Object, List<EventData>> entry : map.entrySet()) {
            for(EventData e : entry.getValue()) {
                if(event.getClass().isAssignableFrom(e.eventType)) {
                    try {
                        e.source.invoke(entry.getKey(), event);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    class EventData {
        private final Method source;
        private final Class<? extends IEvent> eventType;

        public EventData(Method source, Class<? extends IEvent> eventType) {
            this.source = source;
            this.eventType = eventType;
        }
    }
}
