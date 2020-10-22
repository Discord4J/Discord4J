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

package discord4j.common.store.impl;

import java.util.Collection;
import java.util.function.Function;

class IdentityStorage<T> extends Storage<T, T> {

    IdentityStorage(Function<T, Long> idGetter) {
        super(idGetter, Function.identity(), Function.identity(), (a, b) -> b);
    }

    @Override
    T nodeForId(long id) {
        throw new UnsupportedOperationException("Use find instead");
    }

    @Override
    Collection<T> nodes() {
        throw new UnsupportedOperationException("Use findAll instead");
    }
}
