package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;

import java.util.Optional;

/**
 * This event is dispatched when a guild becomes unavailable.
 * Note: this guild is removed from the guild list when this happens!
 */
public class GuildUnavailableEvent extends Event {

	private final IGuild guild;
	private final String id;

	public GuildUnavailableEvent(IGuild guild) {
		this.guild = guild;
		this.id = guild.getID();
	}

	public GuildUnavailableEvent(String id) {
		this.id = id;
		this.guild = null;
	}

	/**
	 * Gets the guild that became unavailable.
	 *
	 * @return The guild. This will not be present if a guild was never initialized before the ready event.
	 */
	public Optional<IGuild> getGuild() {
		return Optional.ofNullable(guild);
	}

	/**
	 * Gets the id of the guild that became unavailable. This is always available.
	 *
	 * @return The unavailable guild.
	 */
	public String getGuildID() {
		return id;
	}
}
