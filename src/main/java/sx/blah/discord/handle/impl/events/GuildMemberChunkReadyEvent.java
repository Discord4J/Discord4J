package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.obj.IGuild;

public class GuildMemberChunkReadyEvent extends Event {
	private final Guild guild;

	public GuildMemberChunkReadyEvent(Guild guild) {
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
