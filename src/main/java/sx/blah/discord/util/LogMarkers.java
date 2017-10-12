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

/**
 * The SLF4J {@link org.slf4j.Marker log markers} used by Discord4J.
 */
public class LogMarkers {

	/**
	 * The "parent" marker to all others.
	 */
	public static final Marker MAIN = MarkerFactory.getMarker("MAIN");
	/**
	 * The marker for all classes in the {@link sx.blah.discord.util} package.
	 * It is a child of {@link #MAIN}.
	 */
	public static final Marker UTIL = MarkerFactory.getMarker("UTIL");
	/**
	 * The marker for all classes in the {@link sx.blah.discord.modules} package.
	 * It is a child of {@link #MAIN}.
	 */
	public static final Marker MODULES = MarkerFactory.getMarker("MODULES");
	/**
	 * The marker for all classes in the {@link sx.blah.discord.handle} package.
	 * It is a child of {@link #MAIN}.
	 */
	public static final Marker HANDLE = MarkerFactory.getMarker("HANDLE");
	/**
	 * The marker for all classes in the {@link sx.blah.discord.api} package.
	 * It is a child of {@link #MAIN}.
	 */
	public static final Marker API = MarkerFactory.getMarker("API");
	/**
	 * The marker for all event-related logging.
	 * It is a child of {@link #API}.
	 */
	public static final Marker EVENTS = MarkerFactory.getMarker("EVENTS");
	/**
	 * The marker for all presence-related logging.
	 * It is a child of {@link #EVENTS}.
	 */
	public static final Marker PRESENCES = MarkerFactory.getMarker("PRESENCES");
	/**
	 * The marker for all message-related logging.
	 * It is a child of {@link #EVENTS}.
	 */
	public static final Marker MESSAGES = MarkerFactory.getMarker("MESSAGES");
	/**
	 * The marker for all websocket-related logging.
	 * It is a child of {@link #API}.
	 */
	public static final Marker WEBSOCKET = MarkerFactory.getMarker("WEBSOCKET");
	/**
	 * The marker for messages being sent and received on the websocket.
	 * It is a child of {@link #WEBSOCKET}
	 */
	public static final Marker WEBSOCKET_TRAFFIC = MarkerFactory.getMarker("WEBSOCKET_TRAFFIC");
	/**
	 * The marker for all reconnect-related logging.
	 * It is a child of {@link #WEBSOCKET}
	 */
	public static final Marker RECONNECTS = MarkerFactory.getMarker("RECONNECTS");
	/**
	 * The marker for all voice-related logging.
	 * It is a child of {@link #API}.
	 */
	public static final Marker VOICE = MarkerFactory.getMarker("VOICE");
	/**
	 * The marker for all voice websocket-related logging.
	 * It is a child of {@link #VOICE} and {@link #WEBSOCKET}.
	 */
	public static final Marker VOICE_WEBSOCKET = MarkerFactory.getMarker("VOICE_WEBSOCKET");
	/**
	 * The marker for websocket keepalive actions.
	 * It is a child of {@link #WEBSOCKET} and {@link #VOICE_WEBSOCKET}.
	 */
	public static final Marker KEEPALIVE = MarkerFactory.getMarker("KEEPALIVE");

	static {
		MAIN.add(UTIL);
		MAIN.add(MODULES);
		MAIN.add(HANDLE);
		MAIN.add(API);
		API.add(EVENTS);
		EVENTS.add(PRESENCES);
		EVENTS.add(MESSAGES);
		API.add(WEBSOCKET);
		WEBSOCKET.add(WEBSOCKET_TRAFFIC);
		WEBSOCKET.add(RECONNECTS);
		API.add(VOICE);
		WEBSOCKET.add(VOICE_WEBSOCKET);
		VOICE.add(VOICE_WEBSOCKET);
		WEBSOCKET.add(KEEPALIVE);
		VOICE_WEBSOCKET.add(KEEPALIVE);
	}
}
