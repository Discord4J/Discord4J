package sx.blah.discord.json.responses;

/**
 * Represents a voice state
 */
public class VoiceStateResponse {

	/**
	 * The user's id
	 */
	public String user_id;

	/**
	 * From @Voltana: afk channel, it's a temporary mute/deaf
	 */
	public boolean suppress;

	/**
	 * Voice session id
	 */
	public String session_id;

	/**
	 * Whether the user muted him/herself
	 */
	public boolean self_mute;

	/**
	 * Whether the user deafened him/herself
	 */
	public boolean self_deaf;

	/**
	 * Whether the user is muted by the server
	 */
	public boolean mute;

	/**
	 * The guild id of the guild involved.
	 */
	public String guild_id;

	/**
	 * Whether the user is deafened by the server
	 */
	public boolean deaf;

	/**
	 * The voice channel's id
	 */
	public String channel_id;
}
