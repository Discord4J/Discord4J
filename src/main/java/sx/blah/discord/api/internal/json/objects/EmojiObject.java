package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json custom emoji object.
 */
public class EmojiObject {
	/**
	 * The id of the emoji.
	 */
	public String id;
	/**
	 * The name of the emoji.
	 */
	public String name;
	/**
	 * Array of role IDs that can use the emoji.
	 */
	public String[] roles;
	/**
	 * Whether the emoji must be wrapped in colons.
	 */
	public boolean require_colons;
	/**
	 * Whether the emoji is managed.
	 */
	public boolean managed;
}
