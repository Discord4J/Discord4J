package sx.blah.discord.json.requests;

/**
 * The request sent to request an invite for a channel.
 */
public class InviteRequest {
	
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
	 * Whether to use an xkcd, human-readable style invite code (http://xkcd.com/936).
	 */
	public boolean xkcdpass;
	
	public InviteRequest(int max_age, int max_uses, boolean temporary, boolean xkcdpass) {
		this.max_age = max_age;
		this.max_uses = max_uses;
		this.temporary = temporary;
		this.xkcdpass = xkcdpass;
	}
}
