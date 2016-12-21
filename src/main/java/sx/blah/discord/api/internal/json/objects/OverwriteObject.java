package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json permission overwrite object.
 */
public class OverwriteObject {
	/**
	 * The id of the overwrite.
	 */
	public String id;
	/**
	 * The type of the overwrite.
	 */
	public String type;
	/**
	 * The permissions allowed by this overwrite.
	 */
	public int allow;
	/**
	 * The permissions denied by this overwrite.
	 */
	public int deny;

	public OverwriteObject(String type, String id, int allow, int deny) {
		this.id = id;
		this.type = type;
		this.allow = allow;
		this.deny = deny;
	}
}
