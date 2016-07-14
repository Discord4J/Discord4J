package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;

/**
 * This event is dispatched when either the client loses connection to discord or is logged out.
 */
public class DiscordDisconnectedEvent extends Event {

	private final Reason reason;

	public DiscordDisconnectedEvent(Reason reason) {
		this.reason = reason;
	}

	/**
	 * Gets the reason this client disconnected.
	 *
	 * @return The reason.
	 */
	public Reason getReason() {
		return reason;
	}

	/**
	 * This enum represents the possible reasons for discord being disconnected.
	 */
	public enum Reason {
		/**
		 * The reason is unknown for disconnecting.
		 */
		UNKNOWN,
		/**
		 * The connection timed out.
		 */
		TIMEOUT,
		/**
		 * The client logged out
		 */
		LOGGED_OUT,
		/**
		 * Discord missed too many pings.
		 */
		MISSED_PINGS,
		/**
		 * The websocket attempting to reconnect.
		 */
		RECONNECTING,
		/**
		 * The websocket encountered an error while initializing. This is unrecoverable.
		 */
		INIT_ERROR,
		/**
		 * The websocket declared the current session to be invalid.
		 */
		INVALID_SESSION,
		/**
		 * The websocket attempted reconnect to discord but it failed.
		 */
		RECONNECTION_FAILED
	}
}
