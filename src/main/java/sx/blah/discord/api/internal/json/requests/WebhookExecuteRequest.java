package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.api.internal.json.objects.MessageObject;
import sx.blah.discord.handle.obj.IEmbedded;

/**
 * Represents a request to execute a webhook
 */
public class WebhookExecuteRequest {

	/**
	 * The content of the message to send.
	 */
	public String content;

	/**
	 * The username to send the message as. Must be 2-32 characters long
	 */
	public String username;

	/**
	 * The avatar to send the message as.
	 */
	public String avatar_url;

	/**
	 * Whether the message should use tts.
	 */
	public boolean tts;

	/**
	 * An array of embedded rich content
	 */
	public EmbedObject[] embeds;

	public WebhookExecuteRequest(String content, String username, String avatar_url, boolean tts) {
		this.content = content;
		this.username = username;
		this.avatar_url = avatar_url;
		this.tts = tts;
	}

	public WebhookExecuteRequest(String username, String avatar_url, EmbedObject[] embeds) {
		this.username = username;
		this.avatar_url = avatar_url;
		this.embeds = embeds;
	}

	public WebhookExecuteRequest(String username, String avatar_url, IEmbedded[] embeds) {
		this.username = username;
		this.avatar_url = avatar_url;
		this.embeds = new EmbedObject[embeds.length];
		for (int i = 0; i < embeds.length; i++) {
			this.embeds[i] = new EmbedObject(embeds[i]);
		}
	}
}
