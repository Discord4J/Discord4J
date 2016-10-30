package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;

import java.util.List;

/**
 * Fired whenever emojis change in a guild.
 */
public class GuildEmojisUpdateEvent extends Event {

	private final IGuild guild;
	private final List<IEmoji> oldEmojis;
	private final List<IEmoji> newEmojis;

	public GuildEmojisUpdateEvent(IGuild guild, List<IEmoji> old, List<IEmoji> brandNew){
		this.guild = guild;
		oldEmojis = old;
		newEmojis = brandNew;
	}

	/**
	 * Gets the guild the emojis were updated for.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}

	/**
	 * Gets the list of old emojis prior to the change.
	 *
	 * @return The old emojis.
	 */
	public List<IEmoji> getOldEmojis() {
		return oldEmojis;
	}

	/**
	 * Gets the new list of emojis.
	 *
	 * @return The old emojis.
	 */
	public List<IEmoji> getNewEmojis() {
		return newEmojis;
	}

}
