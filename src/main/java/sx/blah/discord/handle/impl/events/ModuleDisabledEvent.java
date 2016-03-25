package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
import sx.blah.discord.modules.IModule;

/**
 * This event is dispatched when a module is disabled.
 */
public class ModuleDisabledEvent extends Event {

	/**
	 * The disabled module
	 */
	private IModule module;

	public ModuleDisabledEvent(IModule module) {
		this.module = module;
	}

	/**
	 * Gets the module disabled.
	 *
	 * @return The module.
	 */
	public IModule getModule() {
		return module;
	}
}
