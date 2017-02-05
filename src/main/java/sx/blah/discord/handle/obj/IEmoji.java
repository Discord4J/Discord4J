package sx.blah.discord.handle.obj;

import java.util.List;

/**
 * Represents a guild's <b>custom</b> emoji, not a Unicode one. This can be used for reactions. If you need the
 * Unicode type emojis, use {@link com.vdurmont.emoji.EmojiManager#getForAlias(String)} to get
 * an {@link com.vdurmont.emoji.Emoji}.
 */
public interface IEmoji extends IDiscordObject<IEmoji> {

	/**
	 * Copies this emoji object.
	 *
	 * @return A copy of this object.
	 */
	IEmoji copy();

	/**
	 * Gets the emoji's name.
	 *
	 * @return The name.
	 */
	String getName();

	/**
	 * Gets the guild for this emoji.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Returns true if the emoji needs colons, false otherwise.
	 *
	 * @return True if the emoji needs colons, false otherwise.
	 */
	boolean requiresColons();

	/**
	 * Checks whether the role is managed by an external plugin like Twitch.
	 *
	 * @return True if managed, false if otherwise.
	 */
	boolean isManaged();

	/**
	 * Gets the roles for this emoji. Possibly for integration, but unused at the moment.
	 *
	 * @return The roles list.
	 */
	List<IRole> getRoles();

	/**
	 * Gets the image URL for this emoji.
	 *
	 * @return The image URL.
	 */
	String getImageUrl();

	/**
	 * The emoji as a properly formatted string. "&lt;:name:emoji_id&gt;"
	 *
	 * @return The formatted string.
	 */
	@Override
	String toString();

}
