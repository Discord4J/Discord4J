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

package sx.blah.discord.util.components;

/**
 * Interface for constructing components for individual object instances.
 */
public interface IComponentProvider<C extends IComponent> {
	
	/**
	 * Creates a {@link IComponent} instance for the provided {@link Object}.
	 *
	 * @param obj The object to construct for.
	 * @return The component instance, or null if no component should be provided to this module.
	 */
	C provideFor(Object obj);
	
	/**
	 * Gets the class for the components generated via this component provider.
	 *
	 * @return The component class.
	 */
	Class<C> getComponentClass();
	
	/**
	 * This generates an {@link IComponentProvider} which returns a singleton instance of a component to all modules.
	 *
	 * @param component The component to provide.
	 * @return The single component provider.
	 */
	static <C extends IComponent> IComponentProvider<C> singletonProvider(C component) {
		return new IComponentProvider<C>() {
			@Override
			public C provideFor(Object obj) {
				return component;
			}
			
			@Override
			public Class<C> getComponentClass() {
				return (Class<C>) component.getClass();
			}
		};
	}
}
