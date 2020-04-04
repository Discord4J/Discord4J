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

import java.util.Set;

public interface KeyStore<K extends Comparable<K>> {

    /**
     * Add the given key under a shard index.
     *
     * @param shardId the shard index a key will be associated under
     * @param key the actual key to store
     */
    void add(int shardId, K key);

    /**
     * Remove the given key from a shard index.
     *
     * @param shardId the shard index a key will be removed from
     * @param key the actual key to remove
     */
    void remove(int shardId, K key);

    /**
     * Removes all keys stored under a given shard index.
     *
     * @param shardId the shard index to remove all keys from
     */
    void clear(int shardId);

    /**
     * Return an unmodifiable {@link Set} of keys for the given shard index.
     *
     * @param shardId the shard index to obtain its keys from
     * @return a {@link Set} with keys
     */
    Set<K> keys(int shardId);
}
