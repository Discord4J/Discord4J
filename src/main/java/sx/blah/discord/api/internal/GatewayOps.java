package sx.blah.discord.api.internal;

/**
 * This enum represents gateway op codes.
 */
enum GatewayOps {
	/**
	 * Dispatches an event.
	 */
	DISPATCH,
	/**
	 * Used for ping checking.
	 */
	HEARTBEAT,
	/**
	 * Used for client handshake.
	 */
	IDENTIFY,
	/**
	 * Used to update the client status.
	 */
	STATUS_UPDATE,
	/**
	 * Used to join/move/leave voice channels.
	 */
	VOICE_STATE_UPDATE,
	/**
	 * Used for voice ping checking.
	 */
	VOICE_SERVER_PING,
	/**
	 * Used to resume a closed connection.
	 */
	RESUME,
	/**
	 * Used to redirect clients to a new gateway.
	 */
	RECONNECT,
	/**
	 * Used to request guild members.
	 */
	REQUEST_GUILD_MEMBERS,
	/**
	 * Used to notify client they have an invalid session id.
	 */
	INVALID_SESSION
}
