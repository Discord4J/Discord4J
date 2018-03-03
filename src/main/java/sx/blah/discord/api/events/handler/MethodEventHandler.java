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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.EventPriority;

/**
 * An event handler implementation that depends on an unreflected method invokes using
 * {@link MethodHandle} to call the event.
 */
public final class MethodEventHandler implements EventHandler {

	/***
	 * The reflected class type of the event.
	 */
	private final Class<? extends Event> eventClass;

	/**
	 * The unreflected method handle of the event.
	 */
	private final MethodHandle methodHandle;

	/**
	 * The reflected method of the event.
	 */
	private final Method method;

	/**
	 * The object instance which owns the event.
	 */
	private final Object instance;

	/**
	 * The executor which will be used to execute the event.
	 */
	private final Executor executor;

	/**
	 * Tells whether the handler is a temporary handler or not.
	 */
	private final boolean temporary;

	/**
	 * The event execution order priority.
	 */
	private final EventPriority priority;

	/**
	 * Constructs a new {@link MethodEventHandler} object instance.
	 * 
	 * @param eventClass
	 *            the reflected class type of the event.
	 * @param methodHandle
	 *            the unreflected method handle of the event.
	 * @param method
	 *            the reflected method handle of the event.
	 * @param instance
	 *            the object instance which owns the event.
	 * @param executor
	 *            the executor which will be used to execute the event.
	 * @param temporary
	 *            tells whether the handler is a temporary handler or not.
	 * @param priority
	 *            the event execution order priority.
	 */
	public MethodEventHandler(Class<? extends Event> eventClass, MethodHandle methodHandle, Method method, Object instance,
			Executor executor, boolean temporary, EventPriority priority) {
		this.eventClass = eventClass;
		this.methodHandle = methodHandle;
		this.method = method;
		this.instance = instance;
		this.executor = executor;
		this.temporary = temporary;
		this.priority = priority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#handle(sx.blah.discord.api.events.Event)
	 */
	@Override
	public void handle(Event event) throws Throwable {
		methodHandle.invoke(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#accepts(sx.blah.discord.api.events.Event)
	 */
	@Override
	public boolean accepts(Event event) {
		return eventClass.isInstance(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return method.toString();
	}

	public Class<? extends Event> getEventClass() {
		return eventClass;
	}

	/**
	 * Gets the unreflected method handle of the event.
	 * 
	 * @return the unreflected method handle of the event.
	 */
	public MethodHandle getMethodHandle() {
		return methodHandle;
	}

	/**
	 * Gets the reflected method of the event.
	 * 
	 * @return the reflected method of the event.
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Gets the object instance which owns the event method.
	 * 
	 * @return the object instance which owns the event method
	 */
	public Object getInstance() {
		return instance;
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
		return priority;
	}

}