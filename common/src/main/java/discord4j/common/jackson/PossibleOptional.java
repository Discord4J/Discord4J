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
import java.util.Optional;

@JsonSerialize(using = PossibleOptionalSerializer.class)
public class PossibleOptional<T> {

	private static final PossibleOptional<?> ABSENT = new PossibleOptional<>(null);

	private final Optional<T> value;

	private PossibleOptional(T value) {
		this.value = Optional.ofNullable(value);
	}

	public static <T> PossibleOptional<T> of(T value) {
		return new PossibleOptional<>(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> PossibleOptional<T> absent() {
		return (PossibleOptional<T>) ABSENT;
	}

	public T get() {
		if (isAbsent()) throw new IllegalStateException();
		return value.get();
	}

	public boolean isAbsent() {
		return !value.isPresent();
	}
}
