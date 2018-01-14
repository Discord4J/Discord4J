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
import discord4j.store.noop.NoOpStoreService;
import discord4j.store.primitive.LongObjReactiveStore;
import discord4j.store.primitive.ForwardingStoreService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A factory-esque object which provides store objects from {@link StoreService}s.
 *
 * @see StoreService
 */
public class StoreProvider {

    private final List<StoreService> services = new Vector<>();

    private final AtomicReference<StoreService> genericService = new AtomicReference<>();
    private final AtomicReference<StoreService> longObjService = new AtomicReference<>();

    /**
     * Creates a reusable instance of the provider, service discovery occurs at this point!
     */
    public StoreProvider() {
        ServiceLoader<StoreService> serviceLoader = ServiceLoader.load(StoreService.class);

        serviceLoader.iterator().forEachRemaining(services::add);

        services.add(new NoOpStoreService()); //No-op is lowest priority
    }

    /**
     * Gets the service which will be used to provide generic stores.
     *
     * @return The generic store providing service.
     */
    public StoreService getGenericStoreProvider() {
        if (genericService.get() == null) {
            services.stream().filter(StoreService::hasGenericStores).findFirst().ifPresent(genericService::set);
        }
        return genericService.get();
    }

    /**
     * Gets the service which will be used to provide long-object stores.
     *
     * @return The long-object store providing service.
     */
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

    /**
     * Generates a new generic store instance from the most appropriate service.
     *
     * @param keyClass The class of the keys.
     * @param valueClass The class of the values.
     * @param <K> The key type which provides a 1:1 mapping to the value type. This type is also expected to be
     *           {@link Comparable} in order to allow for range operations.
     * @param <V> The value type.
     * @return A mono which provides a store instance.
     */
    public <K extends Comparable<K>, V> Mono<ReactiveStore<K, V>> newGenericStore(Class<K> keyClass, Class<V> valueClass) {
        return getGenericStoreProvider().provideGenericStore(keyClass, valueClass);
    }

    /**
     * Generates a new long-object store instance from the most appropriate service.
     *
     * @param valueClass The class of the values.
     * @param <V> The value type.
     * @return A mono which provides a store instance.
     */
    public <V> Mono<LongObjReactiveStore<V>> newLongObjStore(Class<V> valueClass) {
        return getLongObjStoreProvider().provideLongObjStore(valueClass);
    }
}
