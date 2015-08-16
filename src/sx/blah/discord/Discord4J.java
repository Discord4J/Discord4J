package sx.blah.discord;

/**
 * @author qt
 * @since 7:56 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * <p>
 * Main class. :D
 */
public class Discord4J {
    public static final String PROJECT_NAME = "Discord4J";
    public static final double PROJECT_VERSION = 1.0;

    /**
     * Set this to true to see debug output
     */
    public static boolean debug = false;

    /**
     * Outputs a message to debug
     *
     * @param s Message
     */
    public static void debug(String s) {
        if (debug) System.out.println(s);
    }
}
