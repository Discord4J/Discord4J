package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IChannel;

/**
 * This event is dispatched when a channel is created.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.ChannelCreateEvent} instead.
 */
@Deprecated
public class ChannelCreateEvent extends sx.blah.discord.handle.impl.events.guild.channel.ChannelCreateEvent {
	
	public ChannelCreateEvent(IChannel channel) {
		super(channel);
	}
}
