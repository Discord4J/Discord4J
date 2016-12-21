package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.api.internal.json.objects.GameObject;
import sx.blah.discord.handle.obj.Status;

public class PresenceUpdateRequest {
	/**
	 * The time (in epoch milliseconds) since the user became idle or null if not idle
	 */
	public Long idle_since; // This must be the boxed Long because it can be null

	/**
	 * The game the user is playing, or null if no game
	 */
	public GameObject game;

	public PresenceUpdateRequest(Long idle_since, Status status) {
		this.idle_since = idle_since;
		this.game = new GameObject(status);
	}
}
