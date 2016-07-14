package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild is deleted or the bot is kicked.
 */
public class GuildLeaveEvent extends Event {

	private final IGuild guild;

	public GuildLeaveEvent(IGuild guild) {
		this.guild = guild;
	}

	/**
	 * Gets the guild involved.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
