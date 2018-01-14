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

import discord4j.store.DataConnection;
import discord4j.store.ReactiveStore;
import reactor.core.publisher.Mono;

/**
 * An implementation of {@link LongObjReactiveStore} which creates primitive connections which delegate to another,
 * generic connection.
 *
 * @see LongObjReactiveStore
 */
public class ForwardingReactiveStore<V> implements LongObjReactiveStore<V> {

    private final ReactiveStore<Long, V> toForward;

    /**
     * Constructs the store.
     *
     * @param toForward The generic store to forward to.
     */
    public ForwardingReactiveStore(ReactiveStore<Long, V> toForward) {
        this.toForward = toForward;
    }

    @Override
    public Mono<LongObjDataConnection<V>> openConnection(boolean lock) {
        return toForward.openConnection(lock).map(ForwardingDataConnection::new);
    }

    @Override
    public <C extends DataConnection<Long, V>> Mono<Void> closeConnection(C connection) {
        return toForward.closeConnection(((ForwardingDataConnection<V>) connection).getOriginal());
    }
}
