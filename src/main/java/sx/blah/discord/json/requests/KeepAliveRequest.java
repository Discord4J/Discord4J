package sx.blah.discord.json.requests;

/**
 * This is sent to request that the connection between the client and the Discord Servers stay alive.
 */
public class KeepAliveRequest {

	/**
	 * The opcode.
	 */
	public int op;

	/**
	 * The time the keep-alive request was sent (in epoch milliseconds).
	 */
	public long d;

	public KeepAliveRequest(int op) {
		this.op = op;
		this.d = System.currentTimeMillis();
	}
}
