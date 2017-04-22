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
 * This represents an object which can wrap objects which themselves don't have an id but can be associated with one.
 */
public class IDLinkedObjectWrapper<T> implements IIDLinkedObject {

	private final long id;
	private final T obj;

	public IDLinkedObjectWrapper(long id, T obj) {
		this.id = id;
		this.obj = obj;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLongID() {
		return id;
	}

	/**
	 * Gets the object associated with this wrapper.
	 *
	 * @return The object.
	 */
	public T getObject() {
		return obj;
	}
}
