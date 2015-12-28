package sx.blah.discord.json.responses.events;

import sx.blah.discord.json.responses.UserResponse;

/**
 * This response is received when a user leaves a guild
 */
public class GuildMemberRemoveEventResponse {
	
	/**
	 * The user who left
	 */
	public UserResponse user;
	
	/**
	 * The guild the user left
	 */
	public String guild_id;
}
