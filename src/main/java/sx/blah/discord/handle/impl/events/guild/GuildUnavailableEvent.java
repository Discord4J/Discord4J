package sx.blah.discord.handle.impl.events.guild;

import sx.blah.discord.handle.obj.IGuild;

import java.util.Optional;

/**
 * This event is dispatched when a guild becomes unavailable.
 * Note: this guild is removed from the guild list when this happens!
 */
public class GuildUnavailableEvent extends GuildEvent {

	private final String id;

	public GuildUnavailableEvent(IGuild guild) {
		super(guild);
		this.id = guild.getID();
	}

	public GuildUnavailableEvent(String id) {
		super(null);
		this.id = id;
	}

	/**
	 * Gets the guild that became unavailable.
	 *
	 * @return The guild. This will not be present if a guild was never initialized before the ready event.
	 */
	public Optional<IGuild> getOptionalGuild() {
		return Optional.ofNullable(getGuild());
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
