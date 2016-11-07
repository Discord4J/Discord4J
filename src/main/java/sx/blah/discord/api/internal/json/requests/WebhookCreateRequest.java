package sx.blah.discord.api.internal.json.requests;

/**
 * Represents a request to create a new webhook.
 */
public class WebhookCreateRequest {

	/**
	 * The name of the webhook. Must be 2-32 characters long
	 */
	public String name;

	/**
	 * The default avatar for the webhook.
	 */
	public String avatar;

	public WebhookCreateRequest(String name, String avatar) {
		this.name = name;
		this.avatar = avatar;
	}
}
