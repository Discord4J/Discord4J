/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.common.jackson;

import java.util.Optional;

public class PossibleOptional<T> {

	private static final PossibleOptional<?> ABSENT = new PossibleOptional<>(null);
	private static final PossibleOptional<?> EMPTY = new PossibleOptional<>(Optional.empty());

	private final Optional<T> value;

	private PossibleOptional(Optional<T> value) {
		this.value = value;
	}

	public static <T> PossibleOptional<T> of(T value) {
		return new PossibleOptional<>(Optional.ofNullable(value));
	}

	@SuppressWarnings("unchecked")
	public static <T> PossibleOptional<T> absent() {
		return (PossibleOptional<T>) ABSENT;
	}

	@SuppressWarnings("unchecked")
	public static <T> PossibleOptional<T> empty() {
		return (PossibleOptional<T>) EMPTY;
	}

	public T get() {
		if (!isPresent()) {
			throw new IllegalStateException();
		}
		return value.get();
	}

	public boolean isAbsent() {
		return value == null;
	}

	public boolean isPresent() {
		return !isAbsent() && value.isPresent();
	}

	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PossibleOptional<?> that = (PossibleOptional<?>) o;

		return value != null ? value.equals(that.value) : that.value == null;
	}

	@Override
	public String toString() {
		if (isAbsent()) {
			return "PossibleOptional.absent";
		}
		if (!isPresent()) {
			return "PossibleOptional.empty";
		}
		return "PossibleOptional[" + value.get() + "]";
	}
}
