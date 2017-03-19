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
 * This is used to ensure a value is accurately and lazily updated.
 *
 * @param <T> The value type this represents.
 */
public class TimedValue<T> extends Lazy<T> {
	private final long timeToInvalidate;
	private long time;

	public TimedValue(long timeToInvalidate, Supplier<T> supplier) {
		super(supplier);
		this.timeToInvalidate = timeToInvalidate;
	}

	@Override
	public T get() {
		if (obj == null || System.currentTimeMillis() - time >= timeToInvalidate) {
			obj = supplier.get();
			time = System.currentTimeMillis();
		}
		return obj;
	}
}
