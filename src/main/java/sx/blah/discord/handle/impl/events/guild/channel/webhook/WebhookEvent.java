package sx.blah.discord.handle.impl.events.guild.channel.webhook;

import sx.blah.discord.handle.impl.events.guild.channel.ChannelEvent;
import sx.blah.discord.handle.obj.IWebhook;

/**
 * This represents a generic webhook event.
 */
public abstract class WebhookEvent extends ChannelEvent {
	
	private final IWebhook webhook;
	
	public WebhookEvent(IWebhook webhook) {
		super(webhook.getChannel());
		this.webhook = webhook;
	}
	
	/**
	 * This gets the webhook involved in this event.
	 *
	 * @return The webhook.
	 */
	public IWebhook getWebhook() {
		return webhook;
	}
}
