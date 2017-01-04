package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild is created/ the bot joins the guild.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.GuildCreateEvent} instead.
 */
@Deprecated
public class GuildCreateEvent extends sx.blah.discord.handle.impl.events.guild.GuildCreateEvent {
	
	public GuildCreateEvent(IGuild guild) {
		super(guild);
	}
}
