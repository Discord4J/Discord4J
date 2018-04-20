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
package discord4j.core.util;

import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public final class PaginationUtil {

    public static <T> Flux<T> paginateAfter(Function<Map<String, Object>, Flux<T>> tProducer,
                                            Function<T, Long> keyExtractor, long startAt, int pageSize) {
        return paginateWithQueryParams(tProducer, keyExtractor, startAt, pageSize, "after");
    }

    public static <T> Flux<T> paginateBefore(Function<Map<String, Object>, Flux<T>> tProducer,
                                             Function<T, Long> keyExtractor, long startAt, int pageSize) {
        return paginateWithQueryParams(tProducer, keyExtractor, startAt, pageSize, "before");
    }

    public static <T> Flux<T> paginateWithQueryParams(Function<Map<String, Object>, Flux<T>> tProducer,
                                                      Function<T, Long> keyExtractor, long startAt, int pageSize,
                                                      String queryKey) {
        final Function<Long, Flux<T>> nextPage = id -> {
            final Map<String, Object> parameters = new HashMap<>(2);
            parameters.put("limit", pageSize);
            parameters.put(queryKey, id);

            return tProducer.apply(parameters);
        };

        return paginate(nextPage, keyExtractor, startAt);
    }

    public static <K, T> Flux<T> paginate(Function<K, Flux<T>> nextPage, Function<T, K> keyExtractor, K startAt) {
        Function<List<T>, K> updateLast = list ->
                list.isEmpty() ? startAt : keyExtractor.apply(list.get(list.size() - 1));

        AtomicReference<K> previousStart = new AtomicReference<>(startAt);

        return Flux.defer(() -> nextPage.apply(previousStart.get()))
                .collectList()
                .doOnNext(list -> previousStart.set(updateLast.apply(list)))
                .flatMapMany(Flux::fromIterable)
                .repeat(() -> !previousStart.get().equals(startAt));
    }
}
