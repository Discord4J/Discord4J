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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.shard;

import discord4j.store.api.Store;
import discord4j.store.api.primitive.ForwardingStore;
import discord4j.store.api.primitive.LongObjStore;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.util.StoreContext;
import reactor.core.publisher.Mono;

/**
 * Factory that delegates the creation of the store to a backing factory and then wraps it into a
 * {@link ShardAwareStore}.
 */
public class ShardAwareStoreService implements StoreService {

    private final KeyStoreRegistry registry;
    private final StoreService backingStoreService;

    public ShardAwareStoreService(KeyStoreRegistry registry, StoreService backingStoreService) {
        this.registry = registry;
        this.backingStoreService = backingStoreService;
    }

    @Override
    public boolean hasGenericStores() {
        return backingStoreService.hasGenericStores();
    }

    @Override
    public <K extends Comparable<K>, V> Store<K, V> provideGenericStore(Class<K> keyClass, Class<V> valueClass) {
        if (!registry.containsStore(valueClass)) {
            registry.putStore(valueClass, backingStoreService.provideGenericStore(keyClass, valueClass));
        }
        return new ShardAwareStore<>(registry.getValueStore(keyClass, valueClass), registry.getKeyStore(valueClass));
    }

    @Override
    public boolean hasLongObjStores() {
        return true;
    }

    @Override
    public <V> LongObjStore<V> provideLongObjStore(Class<V> valueClass) {
        return new ForwardingStore<>(provideGenericStore(Long.class, valueClass));
    }

    @Override
    public void init(StoreContext context) {
        backingStoreService.init(context);
    }

    @Override
    public Mono<Void> dispose() {
        return backingStoreService.dispose();
    }
}
