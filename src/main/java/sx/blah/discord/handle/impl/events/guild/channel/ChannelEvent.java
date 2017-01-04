package sx.blah.discord.handle.impl.events.guild.channel;

import sx.blah.discord.handle.impl.events.guild.GuildEvent;
import sx.blah.discord.handle.obj.IChannel;

/**
 * This represents a generic channel event.
 */
public abstract class ChannelEvent extends GuildEvent {
	
	private final IChannel channel;
	
	public ChannelEvent(IChannel channel) {
		super(channel.getGuild());
		this.channel = channel;
	}
	
	/**
	 * This gets the channel involved in this event.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return channel;
	}
}
