package sx.blah.discord.handle.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;

import java.time.LocalDateTime;

/**
 * This represents a generic discord object.
 */
public interface IDiscordObject<SELF extends IDiscordObject> { //The SELF thing is just a hack to get copy() to work correctly because self types don't exist in java >.>

	/**
	 * Gets the snowflake unique id for this object.
	 *
	 * @return The id.
	 */
	String getID();

	/**
	 * Gets the {@link IDiscordClient} instance this object is tied to.
	 *
	 * @return The client instance.
	 */
	IDiscordClient getClient();

	/**
	 * Gets the {@link LocalDateTime} this object was created at. This is calculated by reversing the snowflake
	 * algorithm on the object's id.
	 *
	 * @return The creation date of this object.
	 */
	default LocalDateTime getCreationDate() {
		return DiscordUtils.getSnowflakeTimeFromID(getID());
	}

	/**
	 * Creates a new instance of this object with all the current properties.
	 *
	 * @return The copied instance of this object.
	 */
	SELF copy();
}
