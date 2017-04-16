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

package sx.blah.discord.util.cache;

import sx.blah.discord.handle.obj.IIDLinkedObject;

/**
 * This represents a provider of {@link ICacheDelegate}s for use in {@link Cache}s.
 */
@FunctionalInterface
public interface ICacheDelegateProvider {

	/**
	 * This is called to provide a new instance of an {@link ICacheDelegate}.
	 *
	 * @param clazz The class being stored in the delegate. (<b>NOTE:</b> be wary of
	 * {@link sx.blah.discord.util.IDLinkedObjectWrapper}s.
	 * @return The cache delegate to use.
	 */
	<T extends IIDLinkedObject> ICacheDelegate<T> provide(Class<T> clazz);
}
