package sx.blah.discord.json.responses;

import com.google.gson.annotations.Expose;

/**
 * Represents a json discord application
 */
public class ApplicationResponse {

	/**
	 * Application secret key
	 */
	@Expose(serialize = false)
	public String secret;

	/**
	 * The application's oauth redirect uris
	 */
	public String[] redirect_uris;

	/**
	 * The application's description.
	 */
	public String description;

	/**
	 * The application name.
	 */
	public String name;

	/**
	 * The application id.
	 */
	@Expose(serialize = false)
	public String id;

	/**
	 * The application icon.
	 */
	public String icon;

	/**
	 * The bot owned by the application (if present)
	 */
	@Expose(serialize = false)
	public BotResponse bot;

	public ApplicationResponse(String[] redirect_uris, String name, String description, String icon) {
		this.redirect_uris = redirect_uris;
		this.description = description;
		this.name = name;
		this.icon = icon;
	}
}
