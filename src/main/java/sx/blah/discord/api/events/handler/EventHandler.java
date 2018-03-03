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

/**
 * An interface which represents our event handling system, it handles the events filtering and
 * their executions.
 */
public interface EventHandler {

	/**
	 * Handles the specified {@link Event} using this {@link EventHandler}.
	 * 
	 * @param event
	 *            the event to handle.
	 * @throws Throwable
	 *             if anything occurs during the event execution.
	 */
	void handle(Event event) throws Throwable;

	/**
	 * Checks whether the event can be accepted to be handled by this {@link EventHandler} or not. When
	 * the event is accepted, it will be sent in a later process to {@link #handle(Event)} method to be
	 * executed.
	 * 
	 * @param event
	 *            he event which will be checked by this handler.
	 * @return <code>true</code> if the event should be handled otherwise <code>false</code>.
	 */
	boolean accepts(Event event);

	/**
	 * Gets the reflected class type of the event.
	 * 
	 * @return the reflected event class type of the event.
	 */
	Class<? extends Event> getEventClass();

	/**
	 * Gets the {@link Executor} object which is responsible about executing this handler in the
	 * iteration.
	 * 
	 * @return the {@link Executor} object.
	 */
	Executor getExecutor();

	/**
	 * Checks whether this handler is a temporary handler or not, if it is, then the handler will be
	 * removed and disposed after it handles the event for the first time, otherwise it says active
	 * until it is manually removed.
	 * 
	 * @return <code>true</code> if it is a temporary handler otherwise <code>false</code>.
	 */
	boolean isTemporary();

	/**
	 * Gets the event execution priority of this handler.
	 * 
	 * @return the event execution priority of this handler.
	 */
	EventPriority getPriority();

}