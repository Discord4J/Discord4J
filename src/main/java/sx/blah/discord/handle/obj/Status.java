package sx.blah.discord.handle.obj;

import java.util.Optional;

/**
 * This represent's a user's "status" i.e. game or streaming.
 */
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
	 */
	public static Status empty() {
		return NONE;
	}

	/**
	 * Creates a game status.
	 *
	 * @param game The game being played.
	 * @return A game status.
	 */
	public static Status game(String game) {
		return new Status(StatusType.GAME, game);
	}

	/**
	 * Creates a streaming status.
	 *
	 * @param message The stream message.
	 * @param url The stream url.
	 * @return The stream status.
	 */
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
	 */
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
