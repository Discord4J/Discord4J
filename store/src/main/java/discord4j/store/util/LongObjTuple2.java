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
package discord4j.store.util;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.util.*;

/**
 * Copy of Reactor's Tuple2 but accepting a long as the first object.
 *
 * @param <T> The second object type.
 *
 * @see LongObjTuple2#of(long, Object)
 * @see Tuple2
 */
public class LongObjTuple2<T> implements Iterable<Object>, Serializable { //Methods copied from reactor.util.function.Tuple2, it would be extended instead but it has a private constructor

    /**
     * Create a {@link LongObjTuple2} with the given objects.
     *
     * @param t1   The first value in the tuple. Not null.
     * @param t2   The second value in the tuple. Not null.
     * @param <T> The type of the second value.
     * @return The new {@link LongObjTuple2}.
     */
    public static <T> LongObjTuple2<T> of(long t1, T t2) {
        return new LongObjTuple2<>(t1, t2);
    }

    /**
     * Converts a {@link Tuple2} to a {@link LongObjTuple2}.
     *
     * @param tuple2 The {@link Tuple2} to convert.
     * @param <T> The type of the second value.
     * @return The new converted {@link LongObjTuple2}.
     */
    public static <T> LongObjTuple2<T> from(Tuple2<Long, T> tuple2) {
        return of(tuple2.getT1(), tuple2.getT2());
    }

    /**
     * Converts a {@link LongObjTuple2} to a {@link Tuple2}.
     *
     * @param tuple The {@link LongObjTuple2} to convert.
     * @param <T> The type of the second value.
     * @return The new converted {@link Tuple2}.
     */
    public static <T> Tuple2<Long, T> convert(LongObjTuple2<T> tuple) {
        return Tuples.of(tuple.getT1(), tuple.getT2());
    }

    private static final long serialVersionUID = 6977984978741213834L;

    final long t1;
    @NonNull final T t2;


    LongObjTuple2(long t1, T t2) {
        this.t1 = t1;
        this.t2 = Objects.requireNonNull(t2, "t2");
    }

    /**
     * Type-safe way to get the fist object of this {@link Tuples}.
     *
     * @return The first object
     */
    public long getT1() {
        return t1;
    }

    /**
     * Type-safe way to get the second object of this {@link Tuples}.
     *
     * @return The second object
     */
    public T getT2() {
        return t2;
    }


    /**
     * Get the object at the given index.
     *
     * @param index The index of the object to retrieve. Starts at 0.
     * @return The object or {@literal null} if out of bounds.
     */
    @Nullable
    public Object get(int index) {
        switch (index) {
            case 0:
                return t1;
            case 1:
                return t2;
            default:
                return null;
        }
    }

    /**
     * Turn this {@literal Tuples} into a plain Object list.
     *
     * @return A new Object list.
     */
    public List<Object> toList() {
        return Arrays.asList(toArray());
    }

    /**
     * Turn this {@literal Tuples} into a plain Object array.
     *
     * @return A new Object array.
     */
    public Object[] toArray() {
        return new Object[]{t1, t2};
    }

    @Override
    public Iterator<Object> iterator() {
        return Collections.unmodifiableList(toList()).iterator();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || (getClass() != o.getClass() && !o.getClass().equals(Tuple2.class))) {
            return false;
        }

        if (o.getClass().equals(Tuple2.class)) {
            Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;

            return tuple2.getT1().equals(t1) && t2.equals(tuple2.getT2());
        } else {
            LongObjTuple2<?> tuple2 = (LongObjTuple2<?>) o;

            return tuple2.t1 == t1 && t2.equals(tuple2.getT2());
        }
    }

    @Override
    public int hashCode() {
        int result = size();
        result = 31 * result + Long.hashCode(t1);
        result = 31 * result + t2.hashCode();
        return result;
    }

    /**
     * Return the number of elements in this {@literal Tuples}.
     *
     * @return The size of this {@literal Tuples}.
     */
    public int size() {
        return 2;
    }

    /**
     * A Tuple String representation is the comma separated list of values, enclosed
     * in square brackets.
     * @return the Tuple String representation
     */
    @Override
    public final String toString() {
        return tupleStringRepresentation(toArray()).insert(0, '[').append(']').toString();
    }

    /**
     * Prepare a string representation of the values suitable for a Tuple of any
     * size by accepting an array of elements. This builds a {@link StringBuilder}
     * containing the String representation of each object, comma separated. It manages
     * nulls as well by putting an empty string and the comma.
     *
     * @param values the values of the tuple to represent
     * @return a {@link StringBuilder} initialized with the string representation of the
     * values in the Tuple.
     *
     * @see Tuples#tupleStringRepresentation(Object...)
     */
    static StringBuilder tupleStringRepresentation(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            Object t = values[i];
            if (i != 0) {
                sb.append(',');
            }
            if (t != null) {
                sb.append(t);
            }
        }
        return sb;
    }
}
