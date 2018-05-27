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
package discord4j.store.util;

/**
 * This is a simple context object, containing various information about the environment the store is being
 * invoked in.
 */
public class StoreContext {

    private final int shard;
    private final Class<?> messageClass;

    public StoreContext(int shard, Class<?> messageClass) {
        this.shard = shard;
        this.messageClass = messageClass;
    }

    /**
     * This gets the shard index which the client is currently operating on.
     *
     * @return The shard id.
     */
    public int getShard() {
        return shard;
    }

    /**
     * This gets the message class used by the client. It may be useful to occasionally evict objects of
     * this type from their respective stores due to the high likelihood of unbounded memory usage leading
     * to eventual {@link java.lang.OutOfMemoryError}s.
     *
     * @return The class which represents a message.
     */
    public Class<?> getMessageClass() {
        return messageClass;
    }
}
