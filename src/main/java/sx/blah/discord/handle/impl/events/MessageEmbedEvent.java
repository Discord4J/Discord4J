package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * This event is dispatched whenever a message is edited.
 */
public class MessageEmbedEvent extends Event {

	private final IMessage message;

	private final List<IEmbed> newEmbed;

	public MessageEmbedEvent (IMessage message, List<IEmbed> oldEmbed) {
		this.message = message;
		List<IEmbed> tempArray = new ArrayList<>();
		for (IEmbed attachment : message.getEmbedded()) {
			if (!oldEmbed.contains(attachment)) {
				tempArray.add(attachment);
			}
		}
		newEmbed = tempArray;
	}

	/**
	 * The Message that embedded media has been added too.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
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
