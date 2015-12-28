package sx.blah.discord.json.responses.events;

/**
 * A response sent when a user starts typing
 */
public class TypingEventResponse {
	
	/**
	 * The user's id who started typing
	 */
	public String user_id;
	
	/**
	 * The timestamp when the event was launching, in epoch milliseconds FIXME ???
	 */
	public long timestamp;
	
	/**
	 * The channel id for where this is occurring
	 */
	public String channel_id;
}
