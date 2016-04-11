package sx.blah.discord.json.requests;

/**
 * This is sent to request that the connection between the client and the Discord Servers stay alive.
 */
public class VoiceKeepAliveRequest {

	/**
	 * The opcode, 3.
	 */
	public int op = 3;

	/**
	 * The time the keep-alive request was sent (in epoch milliseconds).
	 */
	public long d;

	public VoiceKeepAliveRequest(long seq) {
		this.d = seq;
	}
}
