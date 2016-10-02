package sx.blah.discord.api.internal.json.event;


import sx.blah.discord.api.internal.json.objects.UserObject;

/**
 * This is sent when a new member joins a guild
 */
public class GuildMemberAddEventResponse {

	/**
	 * The user who joined the guild
	 */
	public UserObject user;

	/**
	 * The roles of the user.
	 */
	public String[] roles;

	/**
	 * Timestamp for when the user joined
	 */
	public String joined_at;

	/**
	 * The guild id the user joined.
	 */
	public String guild_id;
}
