package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IWebhook;

/**
 * This event is dispatched whenever a webhook is created or discovered.
 */
public class WebhookCreateEvent extends Event {

	private final IWebhook webhook;

	public WebhookCreateEvent(IWebhook webhook) {
		this.webhook = webhook;
	}

	/**
	 * Gets the newly created webhook.
	 *
	 * @return The webhook.
	 */
	public IWebhook getWebhook() {
		return webhook;
	}
}
