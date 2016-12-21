package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json invite object.
 */
public class InviteObject {
	/**
	 * The invite code.
	 */
	public String code;
	/**
	 * The guild the invite is for.
	 */
	public GuildObject guild;
	/**
	 * The channel the invite is for.
	 */
	public ChannelObject channel;
}
