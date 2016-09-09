package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.api.internal.json.generic.StatusObject;

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

	public PresenceUpdateRequest(Long idle_since, Status status) {
		d = new EventObject(idle_since, status);
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
		public StatusObject game;

		public EventObject(Long idle_since, Status status) {
			this.idle_since = idle_since;
			this.game = new StatusObject(status);
		}
	}
}
