package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;

import java.util.List;

/**
 * This event is dispatched whenever a message is edited.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.MessageEmbedEvent} instead.
 */
@Deprecated
public class MessageEmbedEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.MessageEmbedEvent {
	
	public MessageEmbedEvent(IMessage message, List<IEmbed> oldEmbed) {
		super(message, oldEmbed);
	}
}
