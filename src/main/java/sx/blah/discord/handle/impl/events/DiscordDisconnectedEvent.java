package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IDiscordClient;
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
		 * The websocket received {@link sx.blah.discord.api.internal.GatewayOps#INVALID_SESSION}
		 * The client will attempt to reconnect.
		 * Note: The connection may sometimes be resumed. However, reidentifying may be required. In this case, caches
		 * will be cleared and the client will attempt to establish a brand new connection to the gateway.
		 */
		INVALID_SESSION_OP,

		/**
		 * The websocket received {@link sx.blah.discord.api.internal.GatewayOps#RECONNECT}
		 * The client will clear its caches and attempt to establish a new connection to the gateway.
		 */
		RECONNECT_OP,

		/**
		 * A direct call to {@link IDiscordClient#logout()} was made.
		 */
		LOGGED_OUT,

		/**
		 * The websocket was closed unexpectedly (error code 1006). It will attempt to reconnect.
		 */
		ABNORMAL_CLOSE
	}
}
