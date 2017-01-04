package sx.blah.discord.handle.impl.events.module;

import sx.blah.discord.modules.IModule;

/**
 * This event is dispatched when a module is disabled.
 */
public class ModuleDisabledEvent extends ModuleEvent {
	
	public ModuleDisabledEvent(IModule module) {
		super(module);
	}
}
