package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a guild's ownership is transferred.
 */
public class GuildTransferOwnershipEvent extends Event {

	private final IUser oldOwner, newOwner;
	private final IGuild guild;

	public GuildTransferOwnershipEvent(IUser oldOwner, IUser newOwner, IGuild guild) {
		this.oldOwner = oldOwner;
		this.newOwner = newOwner;
		this.guild = guild;
	}

	/**
	 * Gets the original owner of the guild.
	 *
	 * @return The original owner.
	 */
	public IUser getOldOwner() {
		return oldOwner;
	}

	/**
	 * Gets the new owner of the guild.
	 *
	 * @return The new owner.
	 */
	public IUser getNewOwner() {
		return newOwner;
	}

	/**
	 * Gets the guild that ownership was transferred in.
	 *
	 * @return The effected guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
