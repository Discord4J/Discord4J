package sx.blah.discord.json.requests;

import sx.blah.discord.json.generic.GameObject;

/**
 * This request is sent when the user updates his/her presence
 */
public class PresenceUpdateRequest {

	/**
	 * The opcode, always 3 in this case
	 */
	public int op = 3;

	/**
	 * The event object
	 */
	public EventObject d;

	public PresenceUpdateRequest(Long idle_since, String game) {
		d = new EventObject(idle_since, game);
	}

	/**
	 * Represents the event
	 */
	public static class EventObject {

		/**
		 * The time (in epoch milliseconds) since the user became idle or null if not idle
		 */
		public Long idle_since;

		/**
		 * The game the user is playing, or null if no game
		 */
		public GameObject game;

		public EventObject(Long idle_since, String game) {
			this.idle_since = idle_since;
			this.game = new GameObject(game);
		}
	}
}
