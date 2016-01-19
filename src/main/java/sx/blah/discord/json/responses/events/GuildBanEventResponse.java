package sx.blah.discord.json.responses.events;

import sx.blah.discord.json.responses.UserResponse;

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
