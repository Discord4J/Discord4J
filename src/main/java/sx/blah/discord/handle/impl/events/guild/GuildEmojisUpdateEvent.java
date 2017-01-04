package sx.blah.discord.handle.impl.events.guild;

import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;

import java.util.List;

/**
 * Fired whenever emojis change in a guild.
 */
public class GuildEmojisUpdateEvent extends GuildEvent {

	private final List<IEmoji> oldEmojis;
	private final List<IEmoji> newEmojis;

	public GuildEmojisUpdateEvent(IGuild guild, List<IEmoji> old, List<IEmoji> brandNew){
		super(guild);
		oldEmojis = old;
		newEmojis = brandNew;
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
