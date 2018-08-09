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

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Represents a JSON property that may be absent, but never null if it's present.
 *
 * @param <T> JSON Property Type
 */
public class Possible<T> {

    private static final Possible<?> ABSENT = new Possible<>(null);

    @Nullable
    private final T value;

    private Possible(@Nullable T value) {
        this.value = value;
    }

    /**
     * Returns a {@code Possible} with a non-null, present value.
     *
     * @param value A non-null value for a new {@code Possible} to represent.
     * @param <T> JSON Property Type
     * @return An instance of {@code Possible} whose value is always present and never null.
     * @throws NullPointerException If {@code value} is null.
     */
    public static <T> Possible<T> of(T value) {
        Objects.requireNonNull(value);
        return new Possible<>(value);
    }

    /**
     * Returns a {@code Possible} with an absent value.
     *
     * @param <T> JSON Property Type
     * @return An instance of {@code Possible} whose value is absent, but not necessarily null.
     */
    @SuppressWarnings("unchecked")
    public static <T> Possible<T> absent() {
        return (Possible<T>) ABSENT;
    }

    /**
     * Returns an instance of {@code T} if this instance of {@code Possible} represents a non-absent value.
     *
     * @return An instance of {@code T}, if it is present. Guaranteed to never be null.
     * @throws NoSuchElementException If the value is {@link #isAbsent() absent}.
     */
    @Nullable
    public T get() {
        if (isAbsent()) {
            throw new NoSuchElementException();
        }
        return value;
    }

    /**
     * Checks whether the instance of this {@code Possible} represents an absent value.
     *
     * @return True is the value is absent, false otherwise.
     */
    public boolean isAbsent() {
        return value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Possible<?> possible = (Possible<?>) o;

        return value != null ? value.equals(possible.value) : possible.value == null;
    }

    @Override
    public String toString() {
        if (isAbsent()) {
            return "Possible.absent";
        }
        return "Possible[" + value + "]";
    }
}
