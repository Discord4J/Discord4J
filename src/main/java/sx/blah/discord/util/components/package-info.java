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
/**
 * This package contains all classes related to the Discord4J module "components" system.
 *
 *
 * Components are a system which can be used to take advantage of IoC (inversion of control, aka Dependency Injection).
 * This allows for objects to not have an "hard" dependencies on something else. This allows for things like separating
 * interfaces from implementations and having optional dependencies without much effort.
 *
 *
 * Components are injected by utilizing a provided {@link sx.blah.discord.util.components.IComponentProvider}. This
 * provider should produce {@link sx.blah.discord.util.components.IComponent}s whenever called (note that it can produce
 * null to signal that a component should not be injected into an injection site) in order to allow for fine grained
 * control in dependency injection.
 *
 *
 * Note that injected objects must implement the marker interface {@link sx.blah.discord.util.components.IComponent}.
 * This is intentional. Forcing users to implement the marker interface will prevent likely conflicts (i.e. from
 * injecting frequently used objects like {@link java.lang.String}).
 *
 *
 * Users can make use of this api in two ways:
 *
 * <ol>
 *     <li>Programmatically</li>
 *     <li>Dynamically</li>
 * </ol>
 *
 *
 * <b>Programmatically</b>
 *
 *
 * All clients have a {@link sx.blah.discord.util.components.ComponentRegistry} (accessible via
 * {@link sx.blah.discord.api.IDiscordClient#getComponentRegistry()}) which acts as an entry-point into the api. Users
 * can register {@link sx.blah.discord.util.components.IComponentProvider}s via
 * {@link sx.blah.discord.util.components.ComponentRegistry#registerComponentProvider(sx.blah.discord.util.components.IComponentProvider)}
 * however it should be noted that no two providers can ever provide the same component type. Users could then inject
 * using {@link sx.blah.discord.util.components.ComponentRegistry#injectorFor(java.lang.Object)} to retrieve an
 * {@link sx.blah.discord.util.components.ComponentRegistry.Injector} object which can then be called via
 * {@link sx.blah.discord.util.components.ComponentRegistry.Injector#createComponent(java.lang.Class)} to generate a
 * component instance.
 *
 *
 * Note: Users can force dynamic dependency injection via
 * {@link sx.blah.discord.util.components.ComponentRegistry#injectInto(java.lang.Object)} or
 * {@link sx.blah.discord.util.components.ComponentRegistry.Injector#injectInto(java.lang.Object)}.
 *
 *
 * <b>Dynamically</b>
 *
 *
 * Dependency injection sites (methods, method params, or fields) can be marked with the
 * {@link sx.blah.discord.util.components.ComponentInjection} annotation and providing the fully-qualified component
 * class name as the annotation parameter. Injection will then occur either by forcing dynamic injection or by utilizing
 * the module system. When utilizing the module system, modules will have
 * {@link sx.blah.discord.modules.IModule#provideComponents(sx.blah.discord.api.IDiscordClient)} called to get
 * {@link sx.blah.discord.util.components.IComponentProvider}s <i>before</i> any modules are enabled in order to fairly
 * generate a set of components which are able to be injected. Then, once all component providers a retrieved, injection
 * sites will be dynamically used to provide components. After which, the module will be enabled.
 *
 *
 * @see sx.blah.discord.util.components.ComponentInjection
 * @see sx.blah.discord.util.components.ComponentRegistry
 * @see sx.blah.discord.util.components.IComponent
 * @see sx.blah.discord.util.components.IComponentProvider
 */
package sx.blah.discord.util.components;
