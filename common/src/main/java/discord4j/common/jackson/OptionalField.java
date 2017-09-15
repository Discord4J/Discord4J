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

@JsonSerialize(using = OptionalFieldSerializer.class)
public class OptionalField<T> {

	private static final OptionalField<?> ABSENT = new OptionalField<>(null, false);
	private static final OptionalField<?> NULL = new OptionalField<>(null, true);

	private final T value;
	private final boolean isExplicitlyNull;

	private OptionalField(T value, boolean isExplicitlyNull) {
		this.value = value;
		this.isExplicitlyNull = isExplicitlyNull;
	}

	public static <T> OptionalField<T> of(T value) {
		return new OptionalField<>(value, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> OptionalField<T> absent() {
		return (OptionalField<T>) ABSENT;
	}

	@SuppressWarnings("unchecked")
	public static <T> OptionalField<T> ofNull() {
		return (OptionalField<T>) NULL;
	}

	public T get() {
		if (!isPresent()) throw new IllegalStateException();
		return value;
	}

	public boolean isPresent() {
		return value != null;
	}

	public boolean isAbsent() {
		return value == null && !isExplicitlyNull;
	}

	public boolean isNull() {
		return value == null && isExplicitlyNull;
	}
}

