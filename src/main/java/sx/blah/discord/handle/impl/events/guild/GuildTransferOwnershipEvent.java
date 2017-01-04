package sx.blah.discord.handle.impl.events.guild;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a guild's ownership is transferred.
 */
public class GuildTransferOwnershipEvent extends GuildEvent {

	private final IUser oldOwner, newOwner;

	public GuildTransferOwnershipEvent(IUser oldOwner, IUser newOwner, IGuild guild) {
		super(guild);
		this.oldOwner = oldOwner;
		this.newOwner = newOwner;
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
}
