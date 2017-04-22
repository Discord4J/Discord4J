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

package sx.blah.discord.handle.obj;

import java.util.Optional;

/**
 * <b>DEPRECATED</b> - Use {@link IPresence#getPlayingText()} and {@link IPresence#getStreamingUrl()} instead
 * <br><br>
 * This represent's a user's "online status" i.e. game or streaming.
 *
 * @deprecated Use {@link IPresence#getPlayingText()} and {@link IPresence#getStreamingUrl()}
 */
@Deprecated
public class Status {

	/**
	 * Empty status singleton.
	 */
	private static final Status NONE = new Status(StatusType.NONE);

	private final StatusType type;
	private final String name;
	private final String url;

	protected Status(StatusType type, String message, String url) {
		this.type = type;
		this.name = message;
		this.url = url;
	}

	protected Status(StatusType type, String message) {
		this(type, message, null);
	}

	protected Status(StatusType type) {
		this(type, null);
	}

	/**
	 * Creates an empty status (i.e. one with no message shown for the user).
	 *
	 * @return An empty status.
	 * @deprecated Use {@link sx.blah.discord.api.IDiscordClient#changePlayingText(String)}
	 */
	@Deprecated
	public static Status empty() {
		return NONE;
	}

	/**
	 * Creates a game status.
	 *
	 * @param game The game being played.
	 * @return A game status.
	 * @deprecated Use {@link sx.blah.discord.api.IDiscordClient#changePlayingText(String)}
	 */
	@Deprecated
	public static Status game(String game) {
		return new Status(StatusType.GAME, game);
	}

	/**
	 * Creates a streaming status.
	 *
	 * @param message The stream message.
	 * @param url The stream url.
	 * @return The stream status.
	 * @deprecated Use {@link sx.blah.discord.api.IDiscordClient#streaming(String, String)}
	 */
	@Deprecated
	public static Status stream(String message, String url) {
		return new Status(StatusType.STREAM, message, url);
	}

	/**
	 * Gets the status type this represents.
	 *
	 * @return The type.
	 */
	public StatusType getType() {
		return type;
	}

	/**
	 * Gets the status message for this status.
	 *
	 * @return The status message.
	 */
	public String getStatusMessage() {
		return name;
	}

	/**
	 * Gets the url of the status.
	 *
	 * @return The url or empty if not a streaming status.
	 */
	public Optional<String> getUrl() {
		return Optional.ofNullable(url);
	}

	/**
	 * Returns if this status is empty (i.e. no message).
	 *
	 * @return True if empty, false if otherwise.
	 */
	public boolean isEmpty() {
		return type.equals(StatusType.NONE);
	}

	@Override
	public String toString() {
		return String.format("%s (name=%s, url=%s)", type.toString(), name, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Status))
			return false;

		Status statusObj = (Status) obj;
		return statusObj.getType().equals(getType())
				&& ((statusObj.getStatusMessage() == null && getStatusMessage() == null)
					|| (statusObj.getStatusMessage() != null && statusObj.getStatusMessage().equals(getStatusMessage())))
				&& (statusObj.getUrl() != null && statusObj.getUrl().equals(getUrl()));
	}

	/**
	 * This represents the types of statuses that exist.
	 * @deprecated Use {@link IPresence}
	 */
	@Deprecated
	public enum StatusType {
		/**
		 * This represents a game playing status.
		 */
		GAME,
		/**
		 * This represents a streaming status.
		 */
		STREAM,
		/**
		 * This represents a "null"/empty status.
		 */
		NONE
	}
}
