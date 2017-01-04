package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This event gets called on large servers which require members to be sent
 * out over time.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.AllUsersReceivedEvent} instead.
 */
@Deprecated
public class AllUsersReceivedEvent extends sx.blah.discord.handle.impl.events.guild.AllUsersReceivedEvent {
	
	public AllUsersReceivedEvent(IGuild guild) {
		super(guild);
	}
}
