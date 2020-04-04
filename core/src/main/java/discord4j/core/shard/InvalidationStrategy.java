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

import discord4j.core.state.StateHolder;
import discord4j.gateway.ShardInfo;
import discord4j.store.api.Store;
import discord4j.store.api.service.StoreService;
import reactor.core.publisher.Mono;

/**
 * A contract to determine how a Store invalidation should happen on a shard session terminating. Use one of the
 * following factories to create a strategy:
 * <ul>
 *     <li>Disable any shard store invalidation using {@link #disable()}</li>
 *     <li>Do not provide any additional behavior using {@link #identity()}. Recommended for single shard
 *     configurations</li>
 *     <li>Perform shard invalidation using a JDK registry using {@link #withJdkRegistry()}</li>
 *     <li>Perform shard invalidation using a custom registry using {@link #withCustomRegistry(KeyStoreRegistry)}</li>
 * </ul>
 */
public interface InvalidationStrategy {

    /**
     * Obtain a transformed {@link StoreService} that is capable of supporting this invalidation strategy.
     *
     * @param storeService the original configured {@link StoreService}
     * @return a new {@link StoreService} that can run this strategy
     */
    StoreService adaptStoreService(StoreService storeService);

    /**
     * Invalidate the contents of the given {@link StateHolder} depending on the triggering {@link ShardInfo shard}.
     *
     * @param shardInfo the shard that triggered this invalidation
     * @param stateHolder the entity with access to all {@link Store} instances used by this strategy
     * @return a {@link Mono}
     */
    Mono<Void> invalidate(ShardInfo shardInfo, StateHolder stateHolder);

    /**
     * Create an {@link InvalidationStrategy} that disables any action on shard invalidation.
     *
     * @return a {@link NoInvalidationStrategy}
     */
    static NoInvalidationStrategy disable() {
        return new NoInvalidationStrategy();
    }

    /**
     * Create an {@link InvalidationStrategy} that falls through the underlying {@link Store#invalidate()}
     * implementation to invalidate entries on shard session closing.
     *
     * @return a {@link IdentityInvalidationStrategy}
     */
    static IdentityInvalidationStrategy identity() {
        return new IdentityInvalidationStrategy();
    }

    /**
     * Create an {@link InvalidationStrategy} that uses a {@link JdkKeyStoreRegistry} to keep track of stored keys
     * and invalidate them on shard session closing. This enables up-to-date cached entities at the cost of
     * additional memory footprint.
     *
     * @return a {@link KeyStoreInvalidationStrategy} using a JDK based registry
     */
    static KeyStoreInvalidationStrategy withJdkRegistry() {
        return new KeyStoreInvalidationStrategy(new JdkKeyStoreRegistry());
    }

    /**
     * Create an {@link InvalidationStrategy} that uses a custom {@link KeyStoreRegistry} to keep track of stored keys
     * and invalidate them on shard session closing.
     *
     * @param keyStoreRegistry the registry used to store keys
     * @return a {@link KeyStoreInvalidationStrategy} using a custom registry
     */
    static KeyStoreInvalidationStrategy withCustomRegistry(KeyStoreRegistry keyStoreRegistry) {
        return new KeyStoreInvalidationStrategy(keyStoreRegistry);
    }
}
