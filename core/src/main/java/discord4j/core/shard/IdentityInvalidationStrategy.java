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
 * An {@link InvalidationStrategy} that falls through underlying {@link Store#invalidate()} implementation and
 * doesn't modify any {@link StoreService}.
 */
class IdentityInvalidationStrategy implements InvalidationStrategy {

    @Override
    public StoreService adaptStoreService(StoreService storeService) {
        return storeService;
    }

    @Override
    public Mono<Void> invalidate(ShardInfo shardInfo, StateHolder stateHolder) {
        return stateHolder.invalidateStores();
    }
}
