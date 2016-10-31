package sx.blah.discord.api.internal;

public enum VoiceOps {

	/**
	 * Used to begin a voice websocket connection
	 */
	IDENTIFY,

	/**
	 * Used to select the voice protocol
	 */
	SELECT_PAYLOAD,

	/**
	 * Used to complete the websocket handshake
	 */
	READY,

	/**
	 * Used to keep the websocket connection alive
	 */
	HEARTBEAT,

	/**
	 * Used to describe the session
	 */
	SESSION_DESCRIPTION,

	/**
	 * Used to indicate which users are speaking
	 */
	SPEAKING,

	/**
	 * Unknown opcode.
	 */
	UNKNOWN;

	public static VoiceOps get(int opcode) {
		if (opcode >= values().length) {
			return UNKNOWN;
		} else {
			return values()[opcode];
		}
	}
}
