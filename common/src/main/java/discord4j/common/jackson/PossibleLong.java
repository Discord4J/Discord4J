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

import java.util.NoSuchElementException;

public class PossibleLong {

    private static final PossibleLong ABSENT = new PossibleLong();

    private final boolean isAbsent;
    private final long value;

    private PossibleLong() {
        this.isAbsent = true;
        this.value = 0;
    }

    private PossibleLong(long value) {
        this.isAbsent = false;
        this.value = value;
    }

    public static PossibleLong of(long value) {
        return new PossibleLong(value);
    }

    public static PossibleLong absent() {
        return ABSENT;
    }

    public long get() {
        if (isAbsent()) {
            throw new NoSuchElementException();
        }
        return value;
    }

    public boolean isAbsent() {
        return isAbsent;
    }

    @Override
    public int hashCode() {
        return isAbsent ? 0 : Long.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PossibleLong possible = (PossibleLong) o;

        return isAbsent && possible.isAbsent || value == possible.value;
    }
}
