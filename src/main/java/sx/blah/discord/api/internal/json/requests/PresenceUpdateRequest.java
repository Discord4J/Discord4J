package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.api.internal.json.objects.GameObject;
import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.StatusType;

public class PresenceUpdateRequest {
	/**
	 * The time (in epoch milliseconds) since the user became idle or null if not idle
	 */
	public Long idle_since; // This must be the boxed Long because it can be null

	/**
	 * The game the user is playing, or null if no game
	 */
	public GameObject game;

	public PresenceUpdateRequest(Long idle_since, GameObject obj) {
		this.idle_since = idle_since;
		this.game = obj;
	}

	public PresenceUpdateRequest(Long idle_since, IPresence presence) {
		this(idle_since, new GameObject(presence.getPlayingText().orElse(null),
				presence.getStreamingUrl().orElse(null),
				presence.getStatus() == StatusType.STREAMING
						? GameObject.STREAMING
						: (presence.getPlayingText().isPresent() ? GameObject.GAME : GameObject.NONE)));
	}
}
