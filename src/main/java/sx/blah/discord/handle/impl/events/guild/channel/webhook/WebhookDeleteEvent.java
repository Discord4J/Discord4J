package sx.blah.discord.handle.impl.events.guild.channel.webhook;

import sx.blah.discord.handle.obj.IWebhook;

/**
 * This event is dispatched whenever a webhook is deleted, or access to it is lost.
 */
public class WebhookDeleteEvent extends WebhookEvent {
	
	public WebhookDeleteEvent(IWebhook webhook) {
		super(webhook);
	}
}
