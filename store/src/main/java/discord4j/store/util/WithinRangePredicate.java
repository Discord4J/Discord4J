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

import java.util.function.Predicate;

/**
 * This is a predicate implementation which tests whether a provided object is within range of the starting and
 * ending objects.
 *
 * @param <T> The (comparable) type to use.
 */
public class WithinRangePredicate<T extends Comparable<T>> implements Predicate<T> {

    private final T start, end;

    /**
     * Constructs the predicate.
     *
     * @param start The starting object (inclusive).
     * @param end The ending object (exclusive).
     */
    public WithinRangePredicate(T start, T end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean test(T t) {
        return start.compareTo(t) <= 0 && end.compareTo(t) > 0;
    }
}
