package sx.blah.discord.api.internal.json.responses.events;

import sx.blah.discord.api.internal.json.responses.UserResponse;

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
	public UserResponse user;
}
