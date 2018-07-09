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

import discord4j.store.Store;
import discord4j.store.noop.NoOpStoreService;
import discord4j.store.primitive.LongObjStore;
import discord4j.store.util.StoreContext;
import reactor.core.publisher.Mono;

import java.io.Serializable;

/**
 * This represents a java service which provides stores. This can be loaded via
 * {@link discord4j.store.service.StoreServiceLoader} or it may be loaded manually.
 *
 * @see java.util.ServiceLoader
 * @see <a href="https://github.com/google/auto/tree/master/service">Google AutoService</a>
 * @see StoreServiceLoader
 * @see NoOpStoreService
 */
public interface StoreService {

    /**
     * Returns an arbitrary priority for this service. This is used for automated service discovery in the case
     * that multiple services are present. Conventions: 0 = neutral priority,
     * {@link Short#MIN_VALUE} = lowest priority, {@link Short#MAX_VALUE}.
     *
     * @return The priority of this service, 0 by default.
     */
    default short priority() {
        return 0;
    }

    /**
     * This is used to check if this service can provide generic stores.
     *
     * @return True if possible, else false.
     * @see Store
     */
    boolean hasGenericStores();

    /**
     * This is called to provide a new store instance for the provided configuration.
     *
     * @param keyClass The class of the keys.
     * @param valueClass The class of the values.
     * @param <K> The key type which provides a 1:1 mapping to the value type. This type is also expected to be
     * {@link Comparable} in order to allow for range operations.
     * @param <V> The value type, these follow
     * <a href="https://en.wikipedia.org/wiki/JavaBeans#JavaBean_conventions">JavaBean</a> conventions.
     * @return The instance of the store.
     */
    <K extends Comparable<K>, V extends Serializable> Store<K, V> provideGenericStore(Class<K> keyClass, Class<V>
            valueClass);

    /**
     * This is used to check if this service can provide long-object stores.
     *
     * @return True if possible, else false.
     * @see LongObjStore
     */
    boolean hasLongObjStores();

    /**
     * This is called to provide a new store instance with a long key and object values.
     *
     * @param valueClass The class of the values.
     * @param <V> The value type, these follow
     * <a href="https://en.wikipedia.org/wiki/JavaBeans#JavaBean_conventions">JavaBean</a> conventions.
     * @return The instance of the store.
     */
    <V extends Serializable> LongObjStore<V> provideLongObjStore(Class<V> valueClass);

    /**
     * This is a lifecycle method called to signal that a store should allocate any necessary resources.
     *
     * @param context Some context about the environment which this service is being utilized in.
     * @return A mono, whose completion signals that resources have been allocated successfully.
     */
    Mono<Void> init(StoreContext context);

    /**
     * This is a lifecycle method called to signal that a store should dispose of any resources due to
     * an abrupt close (hard reconnect or disconnect).
     *
     * @return A mono, whose completion signals that resources have been released successfully.
     */
    Mono<Void> dispose();
}
