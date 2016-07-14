package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever the bot is @mentioned.
 */
public class MentionEvent extends Event {

	private final IMessage message;

	public MentionEvent(IMessage message) {
		this.message = message;
	}

	/**
	 * Gets the messaged which @mention'd the bot.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
}
