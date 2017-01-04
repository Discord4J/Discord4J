package sx.blah.discord.handle.impl.events;

import sx.blah.discord.modules.IModule;

/**
 * This event is dispatched when a module is disabled.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.module.ModuleDisabledEvent} instead.
 */
@Deprecated
public class ModuleDisabledEvent extends sx.blah.discord.handle.impl.events.module.ModuleDisabledEvent {
	
	public ModuleDisabledEvent(IModule module) {
		super(module);
	}
}
