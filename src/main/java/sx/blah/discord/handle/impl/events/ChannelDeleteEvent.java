package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IChannel;

/**
 * This event is dispatched when a channel is deleted.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.ChannelDeleteEvent} instead.
 */
@Deprecated
public class ChannelDeleteEvent extends sx.blah.discord.handle.impl.events.guild.channel.ChannelDeleteEvent {
	
	public ChannelDeleteEvent(IChannel channel) {
		super(channel);
	}
}
