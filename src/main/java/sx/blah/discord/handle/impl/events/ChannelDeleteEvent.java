package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IChannel;

/**
 * This event is dispatched when a channel is deleted.
 */
public class ChannelDeleteEvent extends Event {

	private final IChannel channel;

	public ChannelDeleteEvent(IChannel channel) {
		this.channel = channel;
	}

	/**
	 * Gets the channel involved.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return channel;
	}
}
