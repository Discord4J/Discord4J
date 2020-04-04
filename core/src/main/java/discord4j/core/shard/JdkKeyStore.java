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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A repository for the entity keys tracked by each shard.
 *
 * @param <K> the type of the keys tracked
 */
public class JdkKeyStore<K extends Comparable<K>> implements KeyStore<K> {

    private final Map<Integer, Set<K>> keysByShard = new ConcurrentHashMap<>();

    @Override
    public void add(int shardId, K key) {
        keySet(shardId).add(key);
    }

    @Override
    public void remove(int shardId, K key) {
        keySet(shardId).remove(key);
    }

    @Override
    public void clear(int shardId) {
        keySet(shardId).clear();
    }

    @Override
    public Set<K> keys(int shardId) {
        return Collections.unmodifiableSet(keySet(shardId));
    }

    private Set<K> keySet(int shardId) {
        return keysByShard.computeIfAbsent(shardId, k -> ConcurrentHashMap.newKeySet());
    }
}
