package sx.blah.discord.modules;

/**
 * This is used to configure the module behavior in Discord4J.
 */
public class Configuration {
	
	/**
	 * When this is true, modules are automatically enabled when loaded.
	 * This is recommended because it automatically resolves dependencies.
	 */
	public static boolean AUTOMATICALLY_ENABLE_MODULES = true;
	
	/**
	 * When this is true, modules are dynamically loaded from the modules directory.
	 */
	public static boolean LOAD_EXTERNAL_MODULES = true;
}
