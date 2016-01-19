package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild is edited by its owner.
 */
public class GuildUpdateEvent extends Event {
	
	private final IGuild oldGuild, newGuild;
	
	public GuildUpdateEvent(IGuild oldGuild, IGuild newGuild) {
		this.oldGuild = oldGuild;
		this.newGuild = newGuild;
	}
	
	/**
	 * Gets the unupdated guild.
	 *
	 * @return The old guild.
	 */
	public IGuild getOldGuild() {
		return oldGuild;
	}
	
	/**
	 * Gets the updated guild.
	 *
	 * @return The new guild.
	 */
	public IGuild getNewGuild() {
		return newGuild;
	}
}
