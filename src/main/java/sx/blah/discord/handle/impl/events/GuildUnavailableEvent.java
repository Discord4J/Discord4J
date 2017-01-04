package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild becomes unavailable.
 * Note: this guild is removed from the guild list when this happens!
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.GuildUnavailableEvent} instead.
 */
@Deprecated
public class GuildUnavailableEvent extends sx.blah.discord.handle.impl.events.guild.GuildUnavailableEvent {
	
	public GuildUnavailableEvent(IGuild guild) {
		super(guild);
	}
	
	public GuildUnavailableEvent(String id) {
		super(id);
	}
}
