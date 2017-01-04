package sx.blah.discord.handle.impl.events;

import sx.blah.discord.modules.IModule;

/**
 * This event is dispatched when a module is enabled.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.module.ModuleEnabledEvent} instead.
 */
@Deprecated
public class ModuleEnabledEvent extends sx.blah.discord.handle.impl.events.module.ModuleEnabledEvent {
	
	public ModuleEnabledEvent(IModule module) {
		super(module);
	}
}
