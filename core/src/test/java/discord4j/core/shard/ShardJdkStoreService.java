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
import discord4j.store.api.util.StoreContext;
import discord4j.store.jdk.JdkStore;
import discord4j.store.jdk.JdkStoreService;
import reactor.core.publisher.Mono;

import java.io.Serializable;

public class ShardJdkStoreService extends JdkStoreService {

    private final ShardStoreRegistry registry;

    volatile Class<?> messageClass;
    volatile int shardId;

    public ShardJdkStoreService(ShardStoreRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <K extends Comparable<K>, V extends Serializable> Store<K, V> provideGenericStore(Class<K> keyClass,
            Class<V> valueClass) {
        if (!registry.containsStore(valueClass)) {
            registry.putStore(valueClass, new JdkStore<K, V>(!valueClass.equals(messageClass)));
        }
        return new ShardAwareStore<>(registry.getValueStore(keyClass, valueClass), registry.getKeyStore(valueClass, shardId));
    }

    @Override
    public Mono<Void> init(StoreContext context) {
        messageClass = context.getMessageClass();
        shardId = context.getShard();
        return Mono.empty();
    }
}
