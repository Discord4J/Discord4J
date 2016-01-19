package sx.blah.discord.json.responses.events;

import sx.blah.discord.json.responses.UserResponse;

/**
 * This is sent when a new member joins a guild
 */
public class GuildMemberAddEventResponse {
	
	/**
	 * The user who joined the guild
	 */
	public UserResponse user;
	
	/**
	 * The roles of the user. TODO: Is this ever not empty?
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
