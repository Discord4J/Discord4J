package sx.blah.discord.json.responses.events;

/**
 * This is received when a message has been acknowledged by this bot on another machine.
 */
public class MessageAcknowledgedEventResponse {
	
	/**
	 * The message acknowledged.
	 */
	public String message_id;
	
	/**
	 * The channel where the message was acknowledged.
	 */
	public String channel_id;
}
