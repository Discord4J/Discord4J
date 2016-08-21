package sx.blah.discord.api.internal.json.generic;

import sx.blah.discord.handle.obj.Status;

/**
 * Generic Json Status object
 */
public class StatusObject {

	/**
	 * The message.
	 */
	public String name;

	/**
	 * The type; 0 for game and 1 for streaming
	 */
	public int type;

	/**
	 * The url of the stream if a stream type.
	 */
	public String url;

	public StatusObject(Status status) {
		if (status.getType() != Status.StatusType.NONE) {
			this.name = status.getStatusMessage();
			this.url = status.getUrl().orElse(null);
			this.type = status.getType().ordinal();
		}
	}
}
