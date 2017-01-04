package sx.blah.discord.handle.impl.events.guild.channel.webhook;

import sx.blah.discord.handle.obj.IWebhook;

/**
 * This event is dispatched whenever a webhook is created or discovered.
 */
public class WebhookCreateEvent extends WebhookEvent {
	
	public WebhookCreateEvent(IWebhook webhook) {
		super(webhook);
	}
}
