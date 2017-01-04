package sx.blah.discord.handle.impl.events.guild.channel.webhook;

import sx.blah.discord.handle.obj.IWebhook;

/**
 * This event is dispatched whenever a webhook is updated.
 */
public class WebhookUpdateEvent extends WebhookEvent {

	private final IWebhook oldWebhook, newWebhook;

	public WebhookUpdateEvent(IWebhook oldWebhook, IWebhook newWebhook) {
		super(newWebhook);
		this.oldWebhook = oldWebhook;
		this.newWebhook = newWebhook;
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
}
