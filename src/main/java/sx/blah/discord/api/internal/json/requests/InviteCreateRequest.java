package sx.blah.discord.api.internal.json.requests;

/**
 * The request sent to create an invite for a channel.
 */
public class InviteCreateRequest {

	/**
	 * The time until the invite expires (in seconds). Set to 0 for an infinite invite.
	 */
	public int max_age;

	/**
	 * The maximum amount of new users this invite can be used on. Set to 0 for an infinite amount of users.
	 */
	public int max_uses;

	/**
	 * Whether the users added through this invite are temporary or not.
	 */
	public boolean temporary;

	/**
	 * Whether to reuse similar invites or not.
	 */
	public boolean unique;

	public InviteCreateRequest(int max_age, int max_uses, boolean temporary, boolean unique) {
		this.max_age = max_age;
		this.max_uses = max_uses;
		this.temporary = temporary;
		this.unique = unique;
	}
}
