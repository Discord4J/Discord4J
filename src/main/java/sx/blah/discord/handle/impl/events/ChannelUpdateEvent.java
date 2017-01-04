package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IChannel;

/**
 * This event is dispatched when a channel's information is updated.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.ChannelUpdateEvent} instead.
 */
@Deprecated
public class ChannelUpdateEvent extends sx.blah.discord.handle.impl.events.guild.channel.ChannelUpdateEvent {
	
	public ChannelUpdateEvent(IChannel oldChannel, IChannel newChannel) {
		super(oldChannel, newChannel);
	}
}
