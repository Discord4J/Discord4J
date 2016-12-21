package sx.blah.discord.api.internal.json.event;

import sx.blah.discord.api.internal.json.objects.UserObject;

/**
 * This is a generic object representing all guild ban events.
 */
public class GuildBanEventResponse {

	/**
	 * The guild involved.
	 */
	public String guild_id;

	/**
	 * The user involved.
	 */
	public UserObject user;
}
