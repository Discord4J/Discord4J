package sx.blah.discord;

/**
 * @author qt
 * @since 5:44 PM 15 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * Static class that contains
 * URLs useful to us.
 */
public final class DiscordEndpoints {
    /**
     * The base URL.
     */
    public static final String BASE = "https://discordapp.com/";
    /**
     * The base API location on Discord's servers.
     */
    public static final String APIBASE = BASE + "api";

    /**
     * Websocket thing. IDK, I stole all of these
     * urls from Hydrabolt :P
     */
    public static final String WEBSOCKET_HUB = "ws://discordapp.com/hub";

    public static final String USERS = APIBASE + "/users";

    /**
     * Used for logging in.
     */
    public static final String LOGIN = APIBASE + "/auth/login";
    /**
     * Used for logging out.
     */
    public static final String LOGOUT = APIBASE + "/auth/logout";

    /**
     * Servers URL
     */
    public static final String SERVERS = APIBASE + "/guilds";

    public static final String CHANNELS = APIBASE + "/channels";
}
