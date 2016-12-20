package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json member object.
 */
public class MemberObject {
	/**
	 * The user associated with this member.
	 */
	public UserObject user;
	/**
	 * The nickname of the member.
	 */
	public String nick;
	/**
	 * The roles of the member.
	 */
	public String[] roles;
	/**
	 * When the member joined the guild.
	 */
	public String joined_at;
	/**
	 * Whether this member is deafened.
	 */
	public boolean deaf;
	/**
	 * Whether this member is muted.
	 */
	public boolean mute;

	public MemberObject(UserObject user, String[] roles) {
		this.user = user;
		this.roles = roles;
	}
}
