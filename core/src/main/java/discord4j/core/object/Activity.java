/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

/** Activity for a {@link Presence}. */
public interface Activity {

	/**
	 * Gets the activity's name.
	 *
	 * @return The activity's name.
	 */
	String getName();

	/**
	 * Gets the specific "action" for this activity.
	 *
	 * @return The specific "action" for this activity.
	 */
	Type getType();

	/**
	 * Gets the stream URL, if present.
	 *
	 * @return The stream url, if present.
	 */
	Optional<String> getStreamingUrl();

	/**
	 * Gets the UNIX time (in milliseconds) of when the activity started, if present.
	 *
	 * @return The UNIX time (in milliseconds) of when the activity started, if present.
	 */
	Optional<Instant> getStart();

	/**
	 * Gets the UNIX time (in milliseconds) of when the activity ends, if present.
	 *
	 * @return The UNIX time (in milliseconds) of when the activity ends, if present.
	 */
	Optional<Instant> getEnd();

	/**
	 * Gets the application ID for the game, if present.
	 *
	 * @return The application ID for the game, if present.
	 */
	Optional<Snowflake> getApplicationId();

	/**
	 * Gets what the player is currently doing, if present.
	 *
	 * @return What the player is currently doing, if present.
	 */
	Optional<String> getDetails();

	/**
	 * Gets the user's current party status, if present.
	 *
	 * @return The user's current party status, if present.
	 */
	Optional<String> getState();

	/**
	 * Gets the ID of the party, if present.
	 *
	 * @return The ID of the party, if present.
	 */
	Optional<String> getPartyId();

	/**
	 * Gets the party's current size, if present.
	 *
	 * @return The party's current size, if present.
	 */
	OptionalInt getCurrentPartySize();

	/**
	 * Gets the party's max size, if present.
	 *
	 * @return The party's max size, if present.
	 */
	OptionalInt getMaxPartySize();

	/**
	 * Gets the ID for a large asset of the activity, usually a {@code Snowflake}, if present.
	 *
	 * @return The ID for a large asset of the activity, usually a {@code Snowflake}, if present.
	 */
	Optional<String> getLargeImageId();

	/**
	 * Gets the text displayed when hovering over the large image of the activity, if present.
	 *
	 * @return The text displayed when hovering over the large image of the activity, if present.
	 */
	Optional<String> getLargeText();

	/**
	 * Gets the ID for a small asset of the activity, usually a {@code Snowflake}, if present.
	 *
	 * @return The ID for a small asset of the activity, usually a {@code Snowflake}, if present.
	 */
	Optional<String> getSmallImageId();

	/**
	 * Gets the text displayed when hovering over the small image of the activity, if present.
	 *
	 * @return The text displayed when hovering over the small image of the activity, if present.
	 */
	Optional<String> getSmallText();

	/** The type of "action" for an activity. */
	enum Type {

		/** "Playing {name}" */
		PLAYING(0),

		/** "Streaming {name}" */
		STREAMING(1),

		/** Listening to {name} */
		LISTENING(2);

		/** The underlying value as represented by Discord. */
		private final int value;

		/**
		 * Constructs a {@code Activity.Type}.
		 *
		 * @param value The underlying value as represented by Discord.
		 */
		Type(final int value) {
			this.value = value;
		}

		/**
		 * Gets the underlying value as represented by Discord.
		 *
		 * @return The underlying value as represented by Discord.
		 */
		public int getValue() {
			return value;
		}
	}
}
