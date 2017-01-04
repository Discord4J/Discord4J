package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;

import java.util.List;

/**
 * Fired whenever emojis change in a guild.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.GuildEmojisUpdateEvent} instead.
 */
@Deprecated
public class GuildEmojisUpdateEvent extends sx.blah.discord.handle.impl.events.guild.GuildEmojisUpdateEvent {
	
	public GuildEmojisUpdateEvent(IGuild guild, List<IEmoji> old, List<IEmoji> brandNew) {
		super(guild, old, brandNew);
	}
}
