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
	 * The marker for all websocket-related logging.
	 * It is a child of {@link #API}.
	 */
	WEBSOCKET(API),
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
