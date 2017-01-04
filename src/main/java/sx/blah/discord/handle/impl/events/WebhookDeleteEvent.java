package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IWebhook;

/**
 * This event is dispatched whenever a webhook is deleted, or access to it is lost.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.webhook.WebhookDeleteEvent} instead.
 */
@Deprecated
public class WebhookDeleteEvent extends sx.blah.discord.handle.impl.events.guild.channel.webhook.WebhookDeleteEvent {
	
	public WebhookDeleteEvent(IWebhook webhook) {
		super(webhook);
	}
}
