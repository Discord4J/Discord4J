package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild becomes unavailable.
 * Note: this guild is removed from the guild list when this happens!
 */
public class GuildUnavailableEvent extends Event {

	private final IGuild guild;

	public GuildUnavailableEvent(IGuild guild) {
		this.guild = guild;
	}

	/**
	 * Gets the guild that became unavailable.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
