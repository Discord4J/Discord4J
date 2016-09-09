package sx.blah.discord.api.internal.json.responses.events;

/**
 * This is received when a bot deletes multiple messages at once.
 */
public class MessageDeleteBulkEventResponse {

	/**
	 * The ids of the messages deleted.
	 */
	public String[] ids;
	/**
	 * The id of the channel the messages belong to.
	 */
	public String channel_id;
}
