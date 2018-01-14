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
import discord4j.store.util.ForwardingStoreService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

public class StoreProvider {

    private final List<StoreService> services = new Vector<>();

    private final AtomicReference<StoreService> genericService = new AtomicReference<>();
    private final AtomicReference<StoreService> longObjService = new AtomicReference<>();

    public StoreProvider() {
        ServiceLoader<StoreService> serviceLoader = ServiceLoader.load(StoreService.class);

        serviceLoader.iterator().forEachRemaining(services::add);

        services.add(new NoOpStoreService()); //No-op is lowest priority
    }

    public StoreService getGenericStoreProvider() {
        if (genericService.get() == null) {
            services.stream().filter(StoreService::hasGenericStores).findFirst().ifPresent(genericService::set);
        }
        return genericService.get();
    }

    public StoreService getLongObjStoreProvider() {
        boolean firstRetrieval = false;

        if (longObjService.get() == null) {
            services.stream().filter(StoreService::hasLongObjStores).findFirst().ifPresent(longObjService::set);
            firstRetrieval = true;
        }

        if (firstRetrieval) { //Fallback to boxed impl if one is present
            if (longObjService.get().getClass().equals(NoOpStoreService.class)
                    && !getGenericStoreProvider().getClass().equals(NoOpStoreService.class))
                longObjService.set(new ForwardingStoreService(getGenericStoreProvider()));
        }

        return longObjService.get();
    }

    public <K extends Comparable<K>, V> Mono<ReactiveStore<K, V>> newGenericStore(Class<K> keyClass, Class<V> valueClass) {
        return getGenericStoreProvider().provideGenericStore(keyClass, valueClass);
    }

    public <V> Mono<LongObjReactiveStore<V>> newLongObjStore(Class<V> valueClass) {
        return getLongObjStoreProvider().provideLongObjStore(valueClass);
    }
}
