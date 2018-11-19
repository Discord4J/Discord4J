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

package discord4j.core;

import discord4j.store.jdk.JdkStore;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Map;

public class DirtyJdkStore<K extends Comparable<K>, V extends Serializable> extends JdkStore<K, V> {

    public DirtyJdkStore(Map<K, V> map) {
        super(map);
    }

    public DirtyJdkStore(boolean persist) {
        super(persist);
    }

    @Override
    public Mono<Void> invalidate() {
        return Mono.empty();
    }

    @Override
    public String toString() {
        return "DirtyJdkStore@" + Integer.toHexString(hashCode()) + "{" +
                "map=" + getMap().getClass().getCanonicalName() +
                '}';
    }
}
