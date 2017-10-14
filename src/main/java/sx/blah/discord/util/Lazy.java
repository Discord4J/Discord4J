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

import java.util.function.Supplier;

/**
 * A wrapper which lazily-initializes an object using the given supplier.
 *
 * @param <T> The type of the object to be initialized.
 */
public class Lazy<T> {
	protected T obj;
	protected final Supplier<T> supplier;

	public Lazy(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	/**
	 * Gets the inner object. If {@link #obj} is null, {@link #supplier} is invoked.
	 *
	 * @return The inner object.
	 */
	public T get() {
		if (obj == null) obj = supplier.get();
		return obj;
	}
}
