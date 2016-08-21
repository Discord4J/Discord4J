package sx.blah.discord.api.internal.json.responses.events;

/**
 * This is received when the connection is resumed after a reconnect.
 */
@Deprecated
public class ResumedEventResponse {

	/**
	 * The new heartbeat interval to use.
	 */
	public long heartbeat_interval;
}
