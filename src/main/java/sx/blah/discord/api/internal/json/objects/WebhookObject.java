package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json webhook object.
 */
public class WebhookObject {
	/**
	 * The id of the webhook.
	 */
	public String id;
	/**
	 * The id of the guild this webhook is in.
	 */
	public String guild_id;
	/**
	 * The id of the channel this webhook can post to.
	 */
	public String channel_id;
	/**
	 * The user that will post with this webhook.
	 */
	public UserObject user;
	/**
	 * The name of the webhook.
	 */
	public String name;
	/**
	 * The avatar of the webhook.
	 */
	public String avatar;
	/**
	 * The token of the webhook.
	 */
	public String token;
}
