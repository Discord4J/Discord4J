/**
 * This package contains everything relating to Discord4J "modules". Modules are like addons to Discord4J, they add
 * additional, optional features. While bots aren't required to use this module system, it is recommended as it allows
 * for multiple bots to work at the same time.
 * <p>
 * There are two stages to module loading:
 * <p>
 * First, the class loading. When Discord4J searches for a class implementing
 * {@link sx.blah.discord.modules.IModule}, it will first check the jar file's MANIFEST.MF for the "module-requires"
 * property. If that property exists, Discord4J will attempt to load a jar file containing a class which corresponds to
 * the value of that property first, preventing any class loading errors (NOTE: "module-requires" must be in the form
 * "package.name.ClassName").
 * <p>
 * Second, the instance loading. When a new {@link sx.blah.discord.api.IDiscordClient} is created, its
 * {@link sx.blah.discord.modules.ModuleLoader} instance will attempt to enable all class-loaded modules. In order to do
 * this, the module loader first verifies that the module is compatible with the current Discord4J version by creating
 * a new instance using a default constructor (constructor with no args) and then comparing the current
 * {@link sx.blah.discord.Discord4J#VERSION} with the {@link sx.blah.discord.modules.IModule#getMinimumDiscord4JVersion()}.
 * If that check is passed, that instance of {@link sx.blah.discord.modules.IModule} then is registered as an event
 * listener and it has the {@link sx.blah.discord.modules.IModule#enable(sx.blah.discord.api.IDiscordClient)} method
 * called for it.
 */
package sx.blah.discord.modules;
