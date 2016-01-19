package sx.blah.discord.json.responses.events;

import sx.blah.discord.json.generic.RoleResponse;

/**
 * This is received when a role is created or updated in a guild.
 */
public class GuildRoleEventResponse {
	
	/**
	 * The role involved.
	 */
	public RoleResponse role;
	
	/**
	 * The guild id of the guild involved.
	 */
	public String guild_id;
}
