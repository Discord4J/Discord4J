package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json voice state object.
 */
public class VoiceStateObject {
	/**
	 * The guild id of the voice state.
	 */
	public String guild_id;
	/**
	 * The voice channel id of the voice state.
	 */
	public String channel_id;
	/**
	 * The user id with the state.
	 */
	public String user_id;
	/**
	 * The session id of the voice state.
	 */
	public String session_id;
	/**
	 * Whether the user is deafened.
	 */
	public boolean deaf;
	/**
	 * Whether the user is muted.
	 */
	public boolean mute;
	/**
	 * Whether the user has deafened themselves.
	 */
	public boolean self_deaf;
	/**
	 * Whether the user has muted themselves.
	 */
	public boolean self_mute;
	/**
	 * Whether user is suppressed.
	 */
	public boolean suppress;
}
