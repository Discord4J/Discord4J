package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild is created/ the bot joins the guild.
 */
public class GuildCreateEvent extends Event {

	private final IGuild guild;

	public GuildCreateEvent(IGuild guild) {
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
