package sx.blah.discord.api.internal.json.objects;

import sx.blah.discord.handle.obj.Status;

/**
 * Represents a json game object.
 */
public class GameObject {
	/**
	 * The type of the game.
	 */
	public int type;
	/**
	 * The name of the game.
	 */
	public String name;
	/**
	 * The url if the type is stream.
	 */
	public String url;

	public GameObject(Status status) {
		if (status.getType() != Status.StatusType.NONE) {
			this.name = status.getStatusMessage();
			this.url = status.getUrl().orElse(null);
			this.type = status.getType().ordinal();
		}
	}
}
