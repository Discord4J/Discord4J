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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

@JsonSerialize(using = PossibleSerializer.class)
public class Possible<T> {

	private static final Possible<?> ABSENT = new Possible<>(null);

	private final T value;

	private Possible(T value) {
		this.value = value;
	}

	public static <T> Possible<T> of(T value) {
		Objects.requireNonNull(value);
		return new Possible<>(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> Possible<T> absent() {
		return (Possible<T>) ABSENT;
	}

	public T get() {
		if (isAbsent()) throw new IllegalStateException();
		return value;
	}

	public boolean isAbsent() {
		return value == null;
	}
}

