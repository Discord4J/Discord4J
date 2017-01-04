package sx.blah.discord.handle.impl.events.shard;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.GatewayOps;

/**
 * This event is dispatched when either a shard loses connection to discord or is logged out.
 */
public class DisconnectedEvent extends ShardEvent {

	private final Reason reason;

	public DisconnectedEvent(Reason reason, IShard shard) {
		super(shard);
		this.reason = reason;
	}

	/**
	 * Gets the reason this shard disconnected.
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
		 * The gateway received {@link GatewayOps#INVALID_SESSION}. The shard will attempt to begin a new session.
		 */
		INVALID_SESSION_OP,

		/**
		 * The gateway received {@link GatewayOps#RECONNECT}. The shard will attempt to resume.
		 */
		RECONNECT_OP,

		/**
		 * A direct call to {@link IShard#logout()} was made.
		 */
		LOGGED_OUT,

		/**
		 * Something unknown caused the websocket to close. The shard will attempt to resume.
		 */
		ABNORMAL_CLOSE
	}
}
