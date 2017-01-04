package sx.blah.discord.handle.impl.events.guild.channel;

import sx.blah.discord.handle.obj.IChannel;

/**
 * This event is dispatched when a channel is created.
 */
public class ChannelCreateEvent extends ChannelEvent {
	
	public ChannelCreateEvent(IChannel channel) {
		super(channel);
	}
}
