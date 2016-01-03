package sx.blah.discord.json.responses.events;

import sx.blah.discord.json.responses.GuildResponse;

/**
 * This is returned when requesting additional guild members from a "large" guild.
 */
public class GuildMemberChunkEventResponse {
	
	/**
	 * The guild id.
	 */
	public String guild_id;
	
	/**
	 * The members requested.
	 */
	public GuildResponse.MemberResponse[] members;
}
