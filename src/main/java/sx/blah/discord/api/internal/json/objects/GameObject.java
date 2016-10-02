package sx.blah.discord.api.internal.json.objects;

import sx.blah.discord.handle.obj.Status;

public class GameObject {
	public int type;
	public String name;
	public String url;

	public GameObject(Status status) {
		if (status.getType() != Status.StatusType.NONE) {
			this.name = status.getStatusMessage();
			this.url = status.getUrl().orElse(null);
			this.type = status.getType().ordinal();
		}
	}
}
