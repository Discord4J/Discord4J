package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;

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
		 * The user left the voice channel.
		 */
		LEFT_CHANNEL,

		/**
		 * Something unknown caused the websocket to close. The connection will be abandoned.
		 */
		ABNORMAL_CLOSE
	}
}

