package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.obj.IGuild;

/**
 * This event gets called on large servers which require members to be sent
 * out over time.
 */
public class AllUsersReceivedEvent extends Event {
	private final Guild guild;

	public AllUsersReceivedEvent(Guild guild) {
		this.guild = guild;
	}

	/**
	 * Gets the guild the member chunk is ready for.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
