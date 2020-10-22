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

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

class WeakStorage<T> extends Storage<WeakReference<T>, T> {

    static final int DEFAULT_CLEANUP_CYCLE_COUNT = 8192;

    private final AtomicInteger insertionsLeftBeforeCleanup;
    private final int cleanupCycleCount;

    WeakStorage(Function<T, Long> idGetter, int cleanupCycleCount) {
        super(idGetter, WeakReference::new, WeakReference::get, (oldWrapper, newData) -> new WeakReference<>(newData));
        this.insertionsLeftBeforeCleanup = new AtomicInteger(cleanupCycleCount);
        this.cleanupCycleCount = cleanupCycleCount;
    }

    WeakStorage(Function<T, Long> idGetter) {
        this(idGetter, DEFAULT_CLEANUP_CYCLE_COUNT);
    }

    @Override
    void insert(T data) {
        // Cleanup dead refs every cleanupCycleCount insertions
        if (insertionsLeftBeforeCleanup.updateAndGet(v -> v > 0 ? v - 1 : cleanupCycleCount) == 0) {
            map.values().removeIf(ref -> ref.get() == null);
        }
        super.insert(data);
    }
}
