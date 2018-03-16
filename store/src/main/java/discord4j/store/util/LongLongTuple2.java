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

import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Copy of Reactor's Tuple2 but accepting two longs.
 *
 * @see LongLongTuple2#of(long, long)
 * @see Tuple2
 */
public class LongLongTuple2 implements Iterable<Long>, Serializable, Comparable<LongLongTuple2> { //Methods copied
    // from reactor.util.function.Tuple2, it would be extended instead but it has a private constructor

    /**
     * Create a {@link LongLongTuple2} with the given objects.
     *
     * @param t1 The first value in the tuple. Not null.
     * @param t2 The second value in the tuple. Not null.
     * @return The new {@link LongLongTuple2}.
     */
    public static LongLongTuple2 of(long t1, long t2) {
        return new LongLongTuple2(t1, t2);
    }

    /**
     * Converts a {@link Tuple2} to a {@link LongLongTuple2}.
     *
     * @param tuple2 The {@link Tuple2} to convert.
     * @return The new converted {@link LongLongTuple2}.
     */
    public static LongLongTuple2 from(Tuple2<Long, Long> tuple2) {
        return of(tuple2.getT1(), tuple2.getT2());
    }

    /**
     * Converts a {@link LongLongTuple2} to a {@link Tuple2}.
     *
     * @param tuple The {@link LongLongTuple2} to convert.
     * @return The new converted {@link Tuple2}.
     */
    public static Tuple2<Long, Long> convert(LongLongTuple2 tuple) {
        return Tuples.of(tuple.getT1(), tuple.getT2());
    }

    private static final long serialVersionUID = 6977984978741213834L;

    final long t1;
    final long t2;


    LongLongTuple2(long t1, long t2) {
        this.t1 = t1;
        this.t2 = t2;
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
    public long getT2() {
        return t2;
    }


    /**
     * Get the object at the given index.
     *
     * @param index The index of the object to retrieve. Starts at 0.
     * @return The object or throws {@link IndexOutOfBoundsException} if out of bounds.
     * @throws IndexOutOfBoundsException
     */
    public long get(int index) {
        switch (index) {
            case 0:
                return t1;
            case 1:
                return t2;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Turn this {@literal Tuples} into a plain Object list.
     *
     * @return A new Object list.
     */
    public List<Long> toList() {
        List<Long> list = new ArrayList<>();
        list.add(t1);
        list.add(t2);
        return list;
    }

    /**
     * Turn this {@literal Tuples} into a plain Object array.
     *
     * @return A new Object array.
     */
    public long[] toArray() {
        return new long[]{t1, t2};
    }

    @Override
    public Iterator<Long> iterator() {
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

            return tuple2.getT1().equals(t1) && tuple2.getT2().equals(t2);
        } else {
            LongLongTuple2 tuple2 = (LongLongTuple2) o;

            return tuple2.t1 == t1 && tuple2.t2 == t2;
        }
    }

    @Override
    public int hashCode() {
        int result = size();
        result = 31 * result + Long.hashCode(t1);
        result = 31 * result + Long.hashCode(t2);
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
     *
     * @return the Tuple String representation
     */
    @Override
    public final String toString() {
        return tupleStringRepresentation(t1, t2).insert(0, '[').append(']').toString();
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

    @Override
    public int compareTo(LongLongTuple2 o) { //Considers first object to have priority
        if (o.t1 == t1 && o.t2 == t2) {
            return 0;
        }

        if (o.t1 != t1) {
            return t1 < o.t1 ? -1 : 1;
        }

        return t2 < o.t2 ? -1 : 1;
    }
}
