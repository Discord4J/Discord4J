package sx.blah.discord.handle.impl.events.module;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.modules.IModule;

/**
 * This represents a generic module event.
 */
public abstract class ModuleEvent extends Event {
	
	private final IModule module;
	
	public ModuleEvent(IModule module) {
		this.module = module;
	}
	
	/**
	 * This gets the module object involved in this event.
	 *
	 * @return The module.
	 */
	public IModule getModule() {
		return module;
	}
}
