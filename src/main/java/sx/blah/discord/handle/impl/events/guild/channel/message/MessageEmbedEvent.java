package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * This event is dispatched whenever a message is edited.
 */
public class MessageEmbedEvent extends MessageEvent {

	private final List<IEmbed> newEmbed;

	public MessageEmbedEvent(IMessage message, List<IEmbed> oldEmbed) {
		super(message);
		List<IEmbed> tempArray = new ArrayList<>();
		for (IEmbed attachment : message.getEmbedded()) {
			if (!oldEmbed.contains(attachment)) {
				tempArray.add(attachment);
			}
		}
		newEmbed = tempArray;
	}

	/**
	 * The new embedded media that has been added to the message.
	 *
	 * @return An array of the new embedded media.
	 */
	public List<IEmbed> getNewEmbed() {
		return newEmbed;
	}
}
