package sx.blah.discord.api.internal.json.requests;

public class GuildMembersRequest {
	/**
	 * The guild's id
	 */
	public String guild_id;

	/**
	 * String the username starts with or empty for all users.
	 */
	public String query = "";

	/**
	 * The limit on users to receive or 0 for max.
	 */
	public int limit = 0;

	public GuildMembersRequest(String guild_id) {
		this.guild_id = guild_id;
	}
}
