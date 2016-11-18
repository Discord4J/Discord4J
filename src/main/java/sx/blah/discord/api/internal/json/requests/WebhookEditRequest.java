package sx.blah.discord.api.internal.json.requests;

/**
 * This is sent to request that a webhook be edited.
 */
public class WebhookEditRequest {

	/**
	 * The new name of the webhook.
	 */
	public String name;

	/**
	 * The new icon.
	 */
	public String avatar;

	public WebhookEditRequest(String name, String avatar) {
		this.name = name;
		this.avatar = avatar;
	}
}
