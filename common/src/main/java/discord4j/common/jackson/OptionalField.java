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

/**
 * Represents a JSON property that is Optional <i>or</i> Optional <b>and</b> Nullable.
 * <p>
 * If the property is <i>only</i> Nullable, then a standard {@link java.util.Optional Optional} should be utilized.
 * Regular JSON properties (neither Nullable or Optional) do not require usage of either types.
 *
 * @param <T> JSON Property Type
 */
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

	/**
	 * Returns an instance of {@code OptionalField} with the included {@code value}.
	 *
	 * @param value The value of this JSON Property, may be null.
	 * @param <T> JSON Property Type
	 * @return An instance of {@code OptionalField} representing the included {@code value}.
	 * @see #isPresent()
	 */
	public static <T> OptionalField<T> of(T value) {
		return new OptionalField<>(value, false);
	}

	/**
	 * Returns an instance of {@code OptionalField} that represents an empty value of {@code <T>}.
	 *
	 * @param <T> JSON Property Type
	 * @return An instance of {@code OptionalField} representing an empty value.
	 * @see #isAbsent()
	 */
	@SuppressWarnings("unchecked")
	public static <T> OptionalField<T> absent() {
		return (OptionalField<T>) ABSENT;
	}

	/**
	 * Returns an instance of {@code OptionalField} that represents a null value of {@code <T>}.
	 *
	 * @param <T> JSON Property Type
	 * @return An instance of {@code OptionalField} representing a null value.
	 * @see #isNull()
	 */
	@SuppressWarnings("unchecked")
	public static <T> OptionalField<T> ofNull() {
		return (OptionalField<T>) NULL;
	}

	/**
	 * Returns an instance of {@code <T>}, if possible.
	 *
	 * @return An instance of {@code <T>}, guaranteed to never be null.
	 * @throws IllegalStateException If {@link #isPresent()} returns false.
	 */
	public T get() {
		if (!isPresent()) throw new IllegalStateException();
		return value;
	}

	/**
	 * Checks if the value is <i>present</i>.
	 * <p>
	 * A value is present if it was included (not Optional) and not null.
	 * @return True if the value is present, false otherwise.
	 */
	public boolean isPresent() {
		return value != null;
	}

	/**
	 * Checks if the value is <i>absent</i>.
	 * <p>
	 * A value is absent if it was not included. It is important to note that this does <b>not</b> necessarily imply
	 * nullability by the source.
	 * @return True if the value is absent, false otherwise.
	 */
	public boolean isAbsent() {
		return value == null && !isExplicitlyNull;
	}

	/**
	 * Checks if the value is <i>null</i>.
	 * <p>
	 * A value is null if it was included and had a null value. Unlike {@link #isAbsent()}, null is explicitly defined
	 * by the JSON source and intended to be processed as such.
	 * @return True if the value is null, false otherwise.
	 */
	public boolean isNull() {
		return value == null && isExplicitlyNull;
	}
}
