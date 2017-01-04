package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild is edited by its owner.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.GuildUpdateEvent} instead.
 */
@Deprecated
public class GuildUpdateEvent extends sx.blah.discord.handle.impl.events.guild.GuildUpdateEvent {
	
	public GuildUpdateEvent(IGuild oldGuild, IGuild newGuild) {
		super(oldGuild, newGuild);
	}
}
