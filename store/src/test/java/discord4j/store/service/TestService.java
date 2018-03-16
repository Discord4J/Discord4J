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

import com.google.auto.service.AutoService;
import discord4j.store.Store;
import discord4j.store.primitive.LongObjStore;

import java.io.Serializable;

@AutoService(StoreService.class)
public class TestService implements StoreService {

    @Override
    public boolean hasGenericStores() {
        return true;
    }

    @Override
    public <K extends Comparable<K>, V extends Serializable> Store<K, V> provideGenericStore(Class<K> keyClass,
                                                                                             Class<V> valueClass) {
        return new MapStore<>();
    }

    @Override
    public boolean hasLongObjStores() {
        return false;
    }

    @Override
    public <V extends Serializable> LongObjStore<V> provideLongObjStore(Class<V> valueClass) {
        throw new UnsupportedOperationException();
    }
}
