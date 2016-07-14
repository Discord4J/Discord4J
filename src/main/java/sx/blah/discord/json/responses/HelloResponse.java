package sx.blah.discord.json.responses;

/**
 * This is received on a HELLO (op 10) frame.
 */
public class HelloResponse {

	/**
	 * How long to wait before refreshing statuses
	 */
	public long heartbeat_interval;
}
