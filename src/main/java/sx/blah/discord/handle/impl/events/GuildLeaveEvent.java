package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild is deleted or the bot is kicked.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent} instead.
 */
@Deprecated
public class GuildLeaveEvent extends sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent {
	
	public GuildLeaveEvent(IGuild guild) {
		super(guild);
	}
}
