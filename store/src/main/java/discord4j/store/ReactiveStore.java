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
package discord4j.store;

import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * Represents a reactive store. This acts as a symbolic connection to a data source, however it is not active
 * until a connection is opened {@link #openConnection(boolean)}.
 *
 * @param <K> The key type which provides a 1:1 mapping to the value type. This type is also expected to be
 *           {@link Comparable} in order to allow for range operations.
 * @param <V> The value type.
 *
 * @see discord4j.store.primitive.LongObjReactiveStore
 */
public interface ReactiveStore<K extends Comparable<K>, V> {

    /**
     * This is used to open an active connection to the data source.
     *
     * @param lock When true, the data source should lock itself while the newly opened connection is active.
     *             This allows for doing operations which require priority to ensure consistency.
     * @return A mono which is expected to provide an open connection as soon as possible.
     */
    Mono<? extends DataConnection<K, V>> openConnection(boolean lock);

    /**
     * This is used to close an active connection to the data source, allowing for resources to be deallocated.
     *
     * @param connection The connection to close.
     * @return A mono which signals when the connection has been closed.
     */
    <C extends DataConnection<K, V>> Mono<Void> closeConnection(C connection);
}
