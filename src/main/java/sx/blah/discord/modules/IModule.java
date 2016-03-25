package sx.blah.discord.modules;

import sx.blah.discord.api.EventSubscriber;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IListener;

/**
 * This class represents a Discord4J "module", implement it to create a new Module. When enabled, modules are registered
 * as event listeners, so your module can use {@link EventSubscriber} or
 * {@link IListener} to listen for events.
 * NOTE: This module must respect the enable() and disable() methods. And it <b>MUST</b> have a default constructor.
 */
public interface IModule {

	/**
	 * This is called to enable the module. NOTE: A new instance of this class is created for each enable() call.
	 *
	 * @param client The client this module instance is being enabled for.
	 * @return Whether the module was successfully loaded, true if successful, false if otherwise.
	 */
	boolean enable(IDiscordClient client);

	/**
	 * This is called to disable the module.
	 */
	void disable();

	/**
	 * This should return the name of the module.
	 *
	 * @return The name of the module.
	 */
	String getName();

	/**
	 * This should return the author(s) of this module.
	 *
	 * @return The author(s).
	 */
	String getAuthor();

	/**
	 * This should return the version of this module.
	 *
	 * @return The version.
	 */
	String getVersion();

	/**
	 * This should return the minimum required version of Discord4J for this to run.
	 *
	 * @return The minimum required version, i.e. "2.2.0".
	 */
	String getMinimumDiscord4JVersion();
}
