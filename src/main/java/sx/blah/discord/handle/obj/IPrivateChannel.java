package sx.blah.discord.handle.obj;

/**
 * Represents a private channel where you could direct message a user.
 */
public interface IPrivateChannel extends IChannel {
	
	/**
	 * Indicates the user with whom you are communicating.
	 *
	 * @return The user.
	 */
	IUser getRecipient();
}
