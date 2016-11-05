package sx.blah.discord.handle.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

/**
 * Represents a single emoji with the users that reacted.
 */
public interface IReaction {

	/**
	 * Gets the message for this reaction.
	 *
	 * @return The message object
	 */
	IMessage getMessage();

	/**
	 * Whether or not this reaction is a custom emoji.
	 *
	 * @return If this is a custom emoji
	 */
	boolean isCustomEmoji();

	/**
	 * The IEmoji object if this is a custom emoji reaction, or null otherwise
	 *
	 * @return The IEmoji object or null if it's not a custom emoji
	 */
	IEmoji getCustomEmoji();

	/**
	 * The amount of people that reacted.
	 *
	 * @return The amount of people that reacted
	 */
	int getCount();

	/**
	 * Gets the users that reacted. Note: this may not be in sync with getCount (getCount is always right). To
	 * re-sync, call refreshUsers.
	 *
	 * @return A list of users that reacted
	 */
	List<IUser> getUsers();

	/**
	 * Forces a call to get all the users that reacted.
	 * @return The list of users that reacted (getUsers)
	 */
	List<IUser> refreshUsers() throws RateLimitException, DiscordException;

	/**
	 * Gets the {@link IDiscordClient} instance this object belongs to.
	 *
	 * @return The client instance.
	 */
	IDiscordClient getClient();

	/**
	 * Get the {@link IShard} instance this object belongs to.
	 */
	IShard getShard();

	/**
	 * Creates a new instance of this object with all the current properties.
	 *
	 * @return The copied instance of this object.
	 */
	IReaction copy();

	/**
	 * Gets a string representation of the emoji. Either the actual emoji, or the IEmoji#toString.
	 *
	 * @return The emoji, either as the emoji itself or the IEmoji formatted string
	 */
	String toString();

}
