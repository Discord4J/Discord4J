package sx.blah.discord.api.internal.json.responses.events;

import sx.blah.discord.api.internal.json.responses.GuildResponse;

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
