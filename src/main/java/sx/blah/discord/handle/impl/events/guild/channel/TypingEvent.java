package sx.blah.discord.handle.impl.events.guild.channel;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched if a user is typing.
 */
public class TypingEvent extends ChannelEvent {

	private final IUser user;

	public TypingEvent(IUser user, IChannel channel) {
		super(channel);
		this.user = user;
	}

	/**
	 * The user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
	}
}
