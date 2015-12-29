package sx.blah.discord.json.requests;

/**
 * This is sent to request that the connection between the client and the Discord Servers stay alive.
 */
public class KeepAliveRequest {
	
	/**
	 * The opcode, always 1.
	 */
	public int op = 1;
	
	/**
	 * The time the keep-alive request was sent (in epoch milliseconds).
	 */
	public long d;
	
	public KeepAliveRequest() {
		this.d = System.currentTimeMillis();
	}
}
