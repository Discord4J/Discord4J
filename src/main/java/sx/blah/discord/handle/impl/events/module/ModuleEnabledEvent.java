package sx.blah.discord.handle.impl.events.module;

import sx.blah.discord.modules.IModule;

/**
 * This event is dispatched when a module is enabled.
 */
public class ModuleEnabledEvent extends ModuleEvent {
	
	public ModuleEnabledEvent(IModule module) {
		super(module);
	}
}
