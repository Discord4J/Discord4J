package sx.blah.discord.api.internal.json.event;

import sx.blah.discord.api.internal.json.objects.UserObject;

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
	public UserObject user;

	/**
	 * The user's new nick.
	 */
	public String nick;
}
