package sx.blah.discord.api.internal.json.objects;

public class OverwriteObject {
	public String id;
	public String type;
	public int allow;
	public int deny;

	public OverwriteObject(String type, String id, int allow, int deny) {
		this.id = id;
		this.type = type;
		this.allow = allow;
		this.deny = deny;
	}
}
