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

package sx.blah.discord.util;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Iterator;

/**
 * This class contains all the SLF4J log {@link org.slf4j.Marker}s used in the Discord4J logger.
 * Each marker enum implements {@link org.slf4j.Marker} and is named after the value of {@link LogMarkers#toString()}.
 */
public enum LogMarkers implements Marker {

	/**
	 * The "parent" marker to all others.
	 */
	MAIN,
	/**
	 * The marker for all classes in the {@link sx.blah.discord.util} package.
	 * It is a child of {@link #MAIN}.
	 */
	UTIL(MAIN),
	/**
	 * The marker for all classes in the {@link sx.blah.discord.modules} package.
	 * It is a child of {@link #MAIN}.
	 */
	MODULES(MAIN),
	/**
	 * The marker for all classes in the {@link sx.blah.discord.handle} package.
	 * It is a child of {@link #MAIN}.
	 */
	HANDLE(MAIN),
	/**
	 * The marker for all classes in the {@link sx.blah.discord.api} package.
	 * It is a child of {@link #MAIN}.
	 */
	API(MAIN),
	/**
	 * The marker for all event-related logging.
	 * It is a child of {@link #API}.
	 */
	EVENTS(API),
	/**
	 * The marker for all presence-related logging.
	 * It is a child of {@link #EVENTS}.
	 */
	PRESENCES(EVENTS),
	/**
	 * The marker for all message-related logging.
	 * It is a child of {@link #EVENTS}.
	 */
	MESSAGES(EVENTS),
	/**
	 * The marker for all websocket-related logging.
	 * It is a child of {@link #API}.
	 */
	WEBSOCKET(API),
	/**
	 * The marker for messages being sent and received on the websocket.
	 * It is a child of {@link #WEBSOCKET}
	 */
	WEBSOCKET_TRAFFIC(WEBSOCKET),
	/**
	 * The marker for all reconnect-related logging.
	 * It is a child of {@link #WEBSOCKET}
	 */
	RECONNECTS(WEBSOCKET),
	/**
	 * The marker for all voice-related logging.
	 * It is a child of {@link #API}.
	 */
	VOICE(API),
	/**
	 * The marker for all voice websocket-related logging.
	 * It is a child of {@link #VOICE} and {@link #WEBSOCKET}.
	 */
	VOICE_WEBSOCKET(VOICE, WEBSOCKET),
	/**
	 * The marker for websocket keepalive actions.
	 * It is a child of {@link #WEBSOCKET} and {@link #VOICE_WEBSOCKET}.
	 */
	KEEPALIVE(WEBSOCKET, VOICE_WEBSOCKET);

	final Marker marker;

	LogMarkers() {
		marker = MarkerFactory.getMarker(this.toString());
	}

	LogMarkers(LogMarkers... parents) {
		this();
		for (LogMarkers parent : parents)
			parent.add(this);
	}

	@Override
	public String getName() {
		return marker.getName();
	}

	@Override
	public void add(Marker reference) {
		marker.add(reference);
	}

	@Override
	public boolean remove(Marker reference) {
		return marker.remove(reference);
	}

	@Override
	public boolean hasChildren() {
		return marker.hasChildren();
	}

	@Override
	public boolean hasReferences() {
		return marker.hasReferences();
	}

	@Override
	public Iterator<Marker> iterator() {
		return marker.iterator();
	}

	@Override
	public boolean contains(Marker other) {
		return marker.contains(other);
	}

	@Override
	public boolean contains(String name) {
		return marker.contains(name);
	}
}
