package sx.blah.discord.api.internal;

import sx.blah.discord.Discord4J;

/**
 * Static class that contains
 * URLs useful to us.
 */
public final class DiscordEndpoints {
	/**
	 * The base URL.
	 */
	public static final String BASE;

	static {
		if (Discord4J.alternateUrl != null) {
			BASE = Discord4J.alternateUrl;
		} else {
			BASE = "https://discordapp.com/";
		}
	}

	/**
	 * The base API location on Discord's servers.
	 */
	public static final String APIBASE = BASE+"api";

	public static final String GATEWAY = APIBASE+"/gateway";

	public static final String USERS = APIBASE+"/users/";

	/**
	 * Used for logging in.
	 */
	public static final String LOGIN = APIBASE+"/auth/login";
	/**
	 * Used for logging out.
	 */
	public static final String LOGOUT = APIBASE+"/auth/logout";

	/**
	 * Guilds URL
	 */
	public static final String GUILDS = APIBASE+"/guilds/";

	public static final String CHANNELS = APIBASE+"/channels/";

	/**
	 * Webhooks URL
	 */
	public static final String WEBHOOKS = APIBASE+"/webhooks/";

	/**
	 * Used for accepting invites
	 */
	public static final String INVITE = APIBASE+"/invite/";

	/**
	 * Formatted string for getting avatar URLs.
	 */
	public static final String AVATARS = "https://cdn.discordapp.com/avatars/%s/%s.jpg";

	/**
	 * Formatted string for getting guild icon URLs.
	 */
	public static final String ICONS = "https://cdn.discordapp.com/icons/%s/%s.jpg";

	/**
	 * Formatted string for getting api metric information.
	 */
	public static final String METRICS = "https://srhpyqt94yxb.statuspage.io/metrics-display/d5cggll8phl5/%s.json";

	/**
	 * Formatted string for getting maintenance information.
	 */
	public static final String STATUS = "https://status.discordapp.com/api/v2/scheduled-maintenances/%s.json";

	/**
	 * Voice url.
	 */
	public static final String VOICE = APIBASE+"/voice/";

	/**
	 * The OAuth2 url.
	 */
	public static final String OAUTH = APIBASE+"/oauth2/";

	/**
	 * The applications url.
	 */
	public static final String APPLICATIONS = OAUTH+"applications";

	/**
	 * Application icon url.
	 */
	public static final String APPLICATION_ICON = "https://cdn.discordapp.com/app-icons/%s/%s.jpg";

	/**
	 * The OAuth2 authorization url.
	 */
	public static final String AUTHORIZE = "https://discordapp.com/oauth2/authorize";

	/**
	 * The emoji image URL.
	 */
	public static final String EMOJI_IMAGE = "https://cdn.discordapp.com/emojis/%s.png";

	/**
	 * The reactions list url for a message. Replacements in order: channel ID, message ID
	 */
	public static final String REACTIONS = CHANNELS + "%s/messages/%s/reactions";

	/**
	 * The reactions user list url. Replacements in order: channel ID, message ID, emoji (emoji or name:id)
	 */
	public static final String REACTIONS_USER_LIST = REACTIONS + "/%s";

	/**
	 * The reactions url for a specific user. Used for deleting/putting a reaction. Replacements in order: channel ID,
	 * message ID, emoji (emoji or name:id), user (@me or ID)
	 */
	public static final String REACTIONS_USER = REACTIONS_USER_LIST + "/%s";

}
