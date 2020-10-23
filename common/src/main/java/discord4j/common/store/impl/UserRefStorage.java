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

import com.github.benmanes.caffeine.cache.Caffeine;
import discord4j.discordjson.json.UserData;
import reactor.util.annotation.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

class UserRefStorage<T> extends IdentityStorage<T> {

    private final IdentityStorage<AtomicReference<UserData>> userStorage;
    private final BiFunction<T, UserData, UserData> userUpdater;
    private final BiFunction<T, AtomicReference<UserData>, T> userRefDataAdapter;
    private final UnaryOperator<T> toImmutable;

    UserRefStorage(Caffeine<Object, Object> caffeine, Function<T, Long> idGetter,
                   BiFunction<T, UserData, UserData> userUpdater,
                   BiFunction<T, AtomicReference<UserData>, T> userRefDataAdapter, UnaryOperator<T> toImmutable,
                   IdentityStorage<AtomicReference<UserData>> userStorage) {
        super(caffeine, idGetter);
        this.userStorage = userStorage;
        this.userUpdater = userUpdater;
        this.userRefDataAdapter = userRefDataAdapter;
        this.toImmutable = toImmutable;
    }

    @Override
    Optional<T> find(long id) {
        return super.find(id).map(toImmutable);
    }

    @Override
    Collection<T> findAll() {
        return super.findAll().stream().map(toImmutable).collect(Collectors.toList());
    }

    @Override
    Optional<T> update(long id, UnaryOperator<T> updateFunction) {
        Capture<Optional<T>> captureOldImmutable = new Capture<>();
        super.update(id, oldData -> {
            captureOldImmutable.capture(Optional.ofNullable(oldData).map(toImmutable));
            T newData = updateFunction.apply(oldData);
            AtomicReference<UserData> ref = updateAndGetUserRef(id, newData);
            if (ref != null) {
                return userRefDataAdapter.apply(newData, ref);
            }
            return oldData;
        });
        return Objects.requireNonNull(captureOldImmutable.get());
    }

    @Override
    Optional<T> delete(long id) {
        return super.delete(id).map(toImmutable);
    }

    private @Nullable AtomicReference<UserData> updateAndGetUserRef(long id, T newData) {
        Optional<AtomicReference<UserData>> existing = userStorage.find(id);
        AtomicReference<UserData> ref;
        if (!existing.isPresent()) {
            UserData newUser = userUpdater.apply(newData, null);
            if (newUser == null) {
                return null;
            } else {
                ref = new AtomicReference<>(newUser);
                userStorage.insert(ref);
            }
        } else {
            ref = existing.get();
            UserData newUser = userUpdater.apply(newData, ref.get());
            if (newUser != null) {
                ref.set(newUser);
            }
        }
        return ref;
    }
}
