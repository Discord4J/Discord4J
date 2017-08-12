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

package sx.blah.discord.util;

import sx.blah.discord.handle.obj.IIDLinkedObject;

/**
 * An object which doesn't have an ID but can be associated with one in a limited context.
 */
public class IDLinkedObjectWrapper<T> implements IIDLinkedObject {

	private final long id;
	private final T obj;

	public IDLinkedObjectWrapper(long id, T obj) {
		this.id = id;
		this.obj = obj;
	}

	@Override
	public long getLongID() {
		return id;
	}

	/**
	 * Gets the object stored by the wrapper.
	 *
	 * @return The object stored by the wrapper.
	 */
	public T getObject() {
		return obj;
	}
}
