package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IMessage;

import java.util.List;

/**
 * This request is sent to discord to delete a set of messages.
 */
public class BulkDeleteRequest {

	/**
	 * The array of message ids to delete.
	 */
	public String[] messages;

	public BulkDeleteRequest(String[] messages) {
		this.messages = messages;
	}

	public BulkDeleteRequest(List<IMessage> messages) {
		this(messages.stream().map(IDiscordObject::getID).toArray(String[]::new));
	}
}
