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
package discord4j.store.primitive;

import discord4j.store.Store;
import reactor.core.publisher.Mono;

/**
 * An implementation of {@link LongObjStore} which creates primitive connections which delegate to another,
 * generic connection.
 *
 * @see LongObjStore
 */
public class ForwardingStore<V> implements LongObjStore<V> {

    private final Store<Long, V> toForward;

    /**
     * Constructs the store.
     *
     * @param toForward The generic store to forward to.
     */
    public ForwardingStore(Store<Long, V> toForward) {
        this.toForward = toForward;
    }

    @Override
    public Mono<LongObjStoreOperations<V>> getConnection(boolean lock) {
        return toForward.getConnection(lock).map(ForwardingStoreOperations::new);
    }
}
