package sx.blah.discord.api.internal.json.responses;

/**
 * This is received on HELLO (op 10)
 */
public class HelloResponse {

	/**
	 * How long to wait in between heartbeats
	 */
	public long heartbeat_interval;
}
