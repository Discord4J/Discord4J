package sx.blah.discord.api.internal.json.responses.events;

import sx.blah.discord.api.internal.json.responses.UserResponse;

/**
 * This event is received when a member is updated in a guild.
 */
public class GuildMemberUpdateEventResponse {

	/**
	 * The guild affected.
	 */
	public String guild_id;

	/**
	 * The user's roles.
	 */
	public String[] roles;

	/**
	 * The user.
	 */
	public UserResponse user;

	/**
	 * The user's new nick.
	 */
	public String nick;
}
