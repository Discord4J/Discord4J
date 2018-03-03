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
package sx.blah.discord.api.events.handler;

import java.util.concurrent.Executor;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.EventPriority;
import sx.blah.discord.api.events.IListener;

/**
 * An event handler implementation that delegates the event invocation to
 * {@link IListener#handle(Event)} method.
 * 
 * @param <T>
 *            the event type.
 */
public final class ListenerEventHandler<T extends Event> implements EventHandler {

	/**
	 * The reflected class type of the event.
	 */
	private final Class<? extends Event> eventClass;

	/**
	 * The listener which we are delegating to.
	 */
	private final IListener<T> listener;

	/**
	 * The executor which will be used to execute the event.
	 */
	private final Executor executor;

	/**
	 * Tells whether this handler is a temporary handler or not.
	 */
	private final boolean temporary;

	/**
	 * Constructs a new {@link ListenerEventHandler} object instance.
	 * 
	 * @param eventClass
	 *            the reflected class type of the event.
	 * @param listener
	 *            the listener which we are delegating to.
	 * @param executor
	 *            the executor which will be used to execute the event.
	 * @param temporary
	 *            tells whether this handler is a temporary handler or not.
	 */
	public ListenerEventHandler(Class<? extends Event> eventClass, IListener<T> listener, Executor executor,
			boolean temporary) {
		this.eventClass = eventClass;
		this.listener = listener;
		this.executor = executor;
		this.temporary = temporary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#handle(sx.blah.discord.api.events.Event)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handle(Event e) throws Throwable {
		listener.handle((T) e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#accepts(sx.blah.discord.api.events.Event)
	 */
	@Override
	public boolean accepts(Event e) {
		return eventClass.isInstance(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return listener.getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#getEventClass()
	 */
	@Override
	public Class<? extends Event> getEventClass() {
		return eventClass;
	}

	/**
	 * Gets the listener which we will delegate the invocation to.
	 * 
	 * @return the listener object.
	 */
	public IListener<T> getListener() {
		return listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#getExecutor()
	 */
	@Override
	public Executor getExecutor() {
		return executor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#isTemporary()
	 */
	@Override
	public boolean isTemporary() {
		return temporary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#getPriority()
	 */
	@Override
	public EventPriority getPriority() {
		return EventPriority.NORMAL;
	}

}