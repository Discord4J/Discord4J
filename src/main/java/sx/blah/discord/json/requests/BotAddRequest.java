package sx.blah.discord.json.requests;

/**
 * This is sent to request a bot is added to a guild.
 */
public class BotAddRequest {

	/**
	 * The guild to be added to.
	 */
	public String guild_id;

	/**
	 * The permissions to be added with.
	 */
	public int permissions;

	/**
	 * Whether to actually add the bot.
	 */
	public boolean authorize = true;

	public BotAddRequest(String guild_id, int permissions) {
		this.guild_id = guild_id;
		this.permissions = permissions;
	}
}
