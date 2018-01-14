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
package discord4j.store.noop.primitive;

import discord4j.store.DataConnection;
import discord4j.store.primitive.LongObjDataConnection;
import discord4j.store.primitive.LongObjReactiveStore;
import reactor.core.publisher.Mono;

/**
 * Store implementation which does nothing.
 *
 * @see discord4j.store.noop.NoOpStoreService
 * @see discord4j.store.noop.NoOpReactiveStore
 */
public class NoOpLongObjReactiveStore<V> implements LongObjReactiveStore<V> {

    @Override
    public Mono<LongObjDataConnection<V>> openConnection(boolean lock) {
        return Mono.just(new NoOpLongObjDataConnection<>());
    }

    @Override
    public <C extends DataConnection<Long, V>> Mono<Void> closeConnection(C connection) {
        return Mono.empty();
    }
}
