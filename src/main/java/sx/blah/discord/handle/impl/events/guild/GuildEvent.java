package sx.blah.discord.handle.impl.events.guild;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;

/**
 * This represents a generic guild event.
 */
public abstract class GuildEvent extends Event {
	
	private final IGuild guild;
	
	public GuildEvent(IGuild guild) {
		this.guild = guild;
	}
	
	/**
	 * This gets the guild object involved in this event.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
