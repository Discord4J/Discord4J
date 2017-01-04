package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Presences;

/**
 * This event is dispatched when a user changes his/her presence.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent} instead.
 */
@Deprecated
public class PresenceUpdateEvent extends sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent {
	
	public PresenceUpdateEvent(IUser user, Presences oldPresence, Presences newPresence) {
		super(user, oldPresence, newPresence);
	}
}
