/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.modules;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.util.components.IComponentProvider;

/**
 * This class represents a Discord4J "module", implement it to create a new Module. When enabled, modules are registered
 * as event listeners, so your module can use {@link EventSubscriber} or
 * {@link IListener} to listen for events.
 * Deployment: The Module Loader will first look for an attribute called "Discord4J-ModuleClass" in a jar's manifest which
 * contains the fully qualified name of all IModule implementations, separated by semicolons ";". If it does not exist then it will default to
 * recursively searching through all the classes of the jar. It is recommended to use the jar attribute in order to reduce Loader overhead.
 * NOTE: This module must respect the enable() and disable() methods. And it <b>MUST</b> have a default constructor.
 */
public interface IModule {
	
	/**
	 * This is called to enable the module. NOTE: A new instance of this class is created for each enable() call.
	 *
	 * @param client The client this module instance is being enabled for.
	 * @return Whether the module was successfully started, true if successful, false if otherwise.
	 */
	boolean enable(IDiscordClient client);

	/**
	 * This is called to disable the module.
	 */
	void disable();
	
	/**
	 * This is called to get component providers from this module. This allows the module to provide components before
	 * anything is initialized, which helps prevent race conditions on initialization of modules.
	 *
	 * @param client The client the components will be provided for.
	 * @return The providers this module contains.
	 */
	default IComponentProvider[] provideComponents(IDiscordClient client) {
		return new IComponentProvider[0];
	}

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
