package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;

/**
 * This event is dispatched when either the client loses connection to discord or is logged out.
 */
public class VoiceDisconnectedEvent extends Event {

	private final Reason reason;

	public VoiceDisconnectedEvent(Reason reason) {
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
		 * The user left the voice channel.
		 */
		LEFT_CHANNEL,
		/**
		 * The websocket encountered an error while initializing
		 */
		INIT_ERROR
	}
}

