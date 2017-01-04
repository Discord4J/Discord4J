package sx.blah.discord.handle.impl.events.guild;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild is deleted or the bot is kicked.
 */
public class GuildLeaveEvent extends GuildEvent {
	
	public GuildLeaveEvent(IGuild guild) {
		super(guild);
	}
}
