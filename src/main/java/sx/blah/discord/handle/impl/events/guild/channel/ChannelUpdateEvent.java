package sx.blah.discord.handle.impl.events.guild.channel;

import sx.blah.discord.handle.obj.IChannel;

/**
 * This event is dispatched when a channel's information is updated.
 */
public class ChannelUpdateEvent extends ChannelEvent {

	private final IChannel oldChannel, newChannel;

	public ChannelUpdateEvent(IChannel oldChannel, IChannel newChannel) {
		super(newChannel);
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
