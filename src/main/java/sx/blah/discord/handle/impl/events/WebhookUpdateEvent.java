package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IWebhook;

/**
 * This event is dispatched whenever a webhook is updated.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.webhook.WebhookUpdateEvent} instead.
 */
@Deprecated
public class WebhookUpdateEvent extends sx.blah.discord.handle.impl.events.guild.channel.webhook.WebhookUpdateEvent {
	
	public WebhookUpdateEvent(IWebhook oldWebhook, IWebhook newWebhook) {
		super(oldWebhook, newWebhook);
	}
}
