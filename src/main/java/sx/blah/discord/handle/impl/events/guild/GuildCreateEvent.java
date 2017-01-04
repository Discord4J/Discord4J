package sx.blah.discord.handle.impl.events.guild;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild is created/ the bot joins the guild.
 */
public class GuildCreateEvent extends GuildEvent {
	
	public GuildCreateEvent(IGuild guild) {
		super(guild);
	}
}
