package sx.blah.discord.handle.impl.events.guild.channel;

import sx.blah.discord.handle.obj.IChannel;

/**
 * This event is dispatched when a channel is deleted.
 */
public class ChannelDeleteEvent extends ChannelEvent {
	
	public ChannelDeleteEvent(IChannel channel) {
		super(channel);
	}
}
