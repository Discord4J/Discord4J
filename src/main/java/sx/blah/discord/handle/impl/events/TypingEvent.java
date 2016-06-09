package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched if a user is typing.
 */
public class TypingEvent extends Event {

	private final IUser user;
	private final IChannel channel;

	public TypingEvent(IUser user, IChannel channel) {
		this.user = user;
		this.channel = channel;
	}

	/**
	 * The user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
	}

	/**
	 * The channel involved.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return channel;
	}
}
