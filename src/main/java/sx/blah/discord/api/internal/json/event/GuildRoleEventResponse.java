package sx.blah.discord.api.internal.json.event;

import sx.blah.discord.api.internal.json.objects.RoleObject;

/**
 * This is received when a role is created or updated in a guild.
 */
public class GuildRoleEventResponse {

	/**
	 * The role involved.
	 */
	public RoleObject role;

	/**
	 * The guild id of the guild involved.
	 */
	public String guild_id;
}
