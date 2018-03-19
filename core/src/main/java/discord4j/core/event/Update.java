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
package discord4j.core.event;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public final class Update<T> {

    private final T current;
    private final T old;

    public static <T> Update<T> of(T current, @Nullable T old) {
        return new Update<>(current, old);
    }

    private Update(T current, @Nullable T old) {
        this.current = current;
        this.old = old;
    }

    public T getCurrent() {
        return current;
    }

    public Optional<T> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Update<?> update = (Update<?>) o;
        return Objects.equals(current, update.current) &&
                Objects.equals(old, update.old);
    }

    @Override
    public int hashCode() {
        return Objects.hash(current, old);
    }
}
