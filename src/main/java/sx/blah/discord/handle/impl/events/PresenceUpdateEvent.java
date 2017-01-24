package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a user changes his/her presence.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent} instead.
 */
@Deprecated
public class PresenceUpdateEvent extends sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent {

	public PresenceUpdateEvent(IUser user, IPresence oldPresence, IPresence newPresence) {
		super(user, oldPresence, newPresence);
	}
}
