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

import discord4j.discordjson.json.ImmutableUserData;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

class WithUser<T> {

    private final T value;
    @Nullable
    private final AtomicReference<ImmutableUserData> ref;
    private final BiFunction<T, ImmutableUserData, T> setter;

    public WithUser(T value, @Nullable AtomicReference<ImmutableUserData> ref,
                    BiFunction<T, ImmutableUserData, T> setter) {
        this.value = value;
        this.ref = ref;
        this.setter = setter;
    }

    T get() {
        return ref == null ? value : setter.apply(value, ref.get());
    }

    @Nullable
    AtomicReference<ImmutableUserData> userRef() {
        return ref;
    }

    WithUser<T> update(UnaryOperator<T> operator) {
        return new WithUser<>(operator.apply(value), ref, setter);
    }
}
