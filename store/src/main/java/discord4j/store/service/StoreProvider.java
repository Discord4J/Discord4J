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
package discord4j.store.service;

import discord4j.store.ReactiveStore;
import discord4j.store.primitive.LongObjReactiveStore;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Vector;

public final class StoreProvider {

    private static final List<StoreService> services = new Vector<>();

    static {
        ServiceLoader<StoreService> serviceLoader = ServiceLoader.load(StoreService.class);

        serviceLoader.iterator().forEachRemaining(services::add);

        services.add(new NoOpStoreService()); //No-op is lowest priority
    }

    public static StoreService getGenericStoreProvider() {
        return services.stream().filter(StoreService::hasGenericStores).findFirst().get();
    }

    public static StoreService getPrimitiveStoreProvider() {
        return services.stream().filter(StoreService::hasPrimitiveStores).findFirst().get();
    }

    public static <K, V> Mono<ReactiveStore<K, V>> newGenericStore(Class<K> keyClass, Class<V> valueClass) {
        return getGenericStoreProvider().provideGenericStore(keyClass, valueClass);
    }

    public static <V> Mono<LongObjReactiveStore<V>> newPrimitiveStore(Class<V> valueClass) {
        return getPrimitiveStoreProvider().providePrimitiveStore(valueClass);
    }
}
