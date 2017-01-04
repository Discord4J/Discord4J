package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IWebhook;

/**
 * This event is dispatched whenever a webhook is created or discovered.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.webhook.WebhookCreateEvent} instead.
 */
@Deprecated
public class WebhookCreateEvent extends sx.blah.discord.handle.impl.events.guild.channel.webhook.WebhookCreateEvent {
	
	public WebhookCreateEvent(IWebhook webhook) {
		super(webhook);
	}
}
