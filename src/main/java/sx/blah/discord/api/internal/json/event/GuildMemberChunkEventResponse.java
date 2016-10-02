package sx.blah.discord.api.internal.json.event;

import sx.blah.discord.api.internal.json.objects.MemberObject;

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
	public MemberObject[] members;
}
