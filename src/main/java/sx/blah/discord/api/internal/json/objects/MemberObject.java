package sx.blah.discord.api.internal.json.objects;

public class MemberObject {
	public UserObject user;
	public String nick;
	public String[] roles;
	public String joined_at;
	public boolean deaf;
	public boolean mute;

	public MemberObject(UserObject user, String[] roles) {
		this.user = user;
		this.roles = roles;
	}
}
