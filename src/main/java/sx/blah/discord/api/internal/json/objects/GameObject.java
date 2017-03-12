package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json game object.
 */
public class GameObject {

	/**
	 * The GameObject type integer for playing a game.
	 */
	public static final int GAME = 0;
	/**
	 * The GameObject type integer for streaming a game.
	 */
	public static final int STREAMING = 1;
	/**
	 * The GameObject type integer for lack of playing/streaming anything.
	 */
	public static final int NONE = 0;

	/**
	 * The type of the game.
	 */
	public int type;
	/**
	 * The name of the game.
	 */
	public String name;
	/**
	 * The url if the type is stream.
	 */
	public String url;

	public GameObject() {}

	public GameObject(String name, String url, int type) {
		this.name = name;
		this.url = url;
		this.type = type;
	}
}
