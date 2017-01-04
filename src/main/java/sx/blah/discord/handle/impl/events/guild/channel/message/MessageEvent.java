package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.impl.events.guild.channel.ChannelEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * This represents a generic message event.
 */
public abstract class MessageEvent extends ChannelEvent {
	
	private final IMessage message;
	
	public MessageEvent(IMessage message) {
		super(message.getChannel());
		this.message = message;
	}
	
	/**
	 * This gets the message involved in this event.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
	
	/**
	 * This gets the author of the message.
	 *
	 * @return The author.
	 */
	public IUser getAuthor() {
		return message.getAuthor();
	}
}
