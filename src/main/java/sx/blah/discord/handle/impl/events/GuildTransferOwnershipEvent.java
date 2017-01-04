package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a guild's ownership is transferred.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.GuildTransferOwnershipEvent} instead.
 */
@Deprecated
public class GuildTransferOwnershipEvent extends sx.blah.discord.handle.impl.events.guild.GuildTransferOwnershipEvent {
	
	public GuildTransferOwnershipEvent(IUser oldOwner, IUser newOwner, IGuild guild) {
		super(oldOwner, newOwner, guild);
	}
}
