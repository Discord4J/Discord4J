package sx.blah.discord.handle.impl.events.guild.member;

import sx.blah.discord.handle.impl.events.guild.GuildEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This represents a generic guild user event.
 */
public abstract class GuildMemberEvent extends GuildEvent {
	
	private final IUser user;
	
	public GuildMemberEvent(IGuild guild, IUser user) {
		super(guild);
		this.user = user;
	}
	
	/**
	 * This gets the guild user object involved in this event.
	 *
	 * @return The guild user.
	 */
	public IUser getUser() {
		return user;
	}
}
