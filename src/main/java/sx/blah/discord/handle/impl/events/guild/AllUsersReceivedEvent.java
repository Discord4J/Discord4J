package sx.blah.discord.handle.impl.events.guild;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This event gets called on large servers which require members to be sent
 * out over time.
 */
public class AllUsersReceivedEvent extends GuildEvent {
	
	public AllUsersReceivedEvent(IGuild guild) {
		super(guild);
	}
}
