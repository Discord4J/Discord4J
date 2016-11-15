package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IWebhook;

/**
 * This event is dispatched whenever a webhook is deleted, or access to it is lost.
 */
public class WebhookDeleteEvent extends Event {

	private final IWebhook webhook;

	public WebhookDeleteEvent(IWebhook webhook) {
		this.webhook = webhook;
	}

	/**
	 * Gets the webhook that was deleted.
	 *
	 * @return The deleted webhook.
	 */
	public IWebhook getWebhook() {
		return webhook;
	}
}
