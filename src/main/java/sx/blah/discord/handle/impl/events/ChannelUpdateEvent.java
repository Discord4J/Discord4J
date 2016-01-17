package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IChannel;

/**
 * This event is dispatched when a channel's information is updated.
 */
public class ChannelUpdateEvent extends Event {
	
	private final IChannel oldChannel, newChannel;
	
	public ChannelUpdateEvent(IChannel oldChannel, IChannel newChannel) {
		this.oldChannel = oldChannel;
		this.newChannel = newChannel;
	}
	
	/**
	 * Gets the original channel.
	 *
	 * @return The un-updated instance of the channel.
	 */
	public IChannel getOldChannel() {
		return oldChannel;
	}
	
	/**
	 * Gets the new channel.
	 *
	 * @return The updated instance of the channel.
	 */
	public IChannel getNewChannel() {
		return newChannel;
	}
}
