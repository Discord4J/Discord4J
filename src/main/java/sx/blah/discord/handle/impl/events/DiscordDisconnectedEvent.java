package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;

/**
 * This event is dispatched when either a shard loses connection to discord or is logged out.
 * @deprecated Use {@link DisconnectedEvent}
 */
public class DiscordDisconnectedEvent extends DisconnectedEvent {

	public DiscordDisconnectedEvent(Reason reason, IShard shard) {
		super(DisconnectedEvent.Reason.valueOf(reason.name()), shard);
	}

	/**
	 * This enum represents the possible reasons for discord being disconnected.
	 * @deprecated Use {@link DisconnectedEvent.Reason}
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
