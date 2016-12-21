package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IWebhook;

/**
 * This event is dispatched whenever a webhook is updated.
 */
public class WebhookUpdateEvent extends Event {

	private final IWebhook oldWebhook, newWebhook;
	private final IChannel channel;

	public WebhookUpdateEvent(IWebhook oldWebhook, IWebhook newWebhook, IChannel channel) {
		this.oldWebhook = oldWebhook;
		this.newWebhook = newWebhook;
		this.channel = channel;
	}

	/**
	 * Gets the original version of the webhook.
	 *
	 * @return The old webhook.
	 */
	public IWebhook getOldWebhook() {
		return oldWebhook;
	}

	/**
	 * Gets the new version of the webhook.
	 *
	 * @return The new webhook.
	 */
	public IWebhook getNewWebhook() {
		return newWebhook;
	}

	/**
	 * Gets the channel the webhook was created for.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return channel;
	}
}
