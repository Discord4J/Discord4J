package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
import sx.blah.discord.modules.IModule;

/**
 * This event is dispatched when a module is enabled.
 */
public class ModuleEnabledEvent extends Event {

	/**
	 * The enabled module
	 */
	private IModule module;

	public ModuleEnabledEvent(IModule module) {
		this.module = module;
	}

	/**
	 * Gets the module enabled.
	 *
	 * @return The module.
	 */
	public IModule getModule() {
		return module;
	}
}
