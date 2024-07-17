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
package discord4j.rest.util;

import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

public final class PaginationUtil {

    public static <T> Flux<T> paginateAfter(final Function<Map<String, Object>, Flux<T>> tProducer,
                                            final ToLongFunction<T> keyExtractor, final long startAt,
                                            final int pageSize) {

        return paginateWithQueryParams(tProducer, keyExtractor, startAt, pageSize, "after", false);
    }

    public static <T> Flux<T> paginateBefore(final Function<Map<String, Object>, Flux<T>> tProducer,
                                             final ToLongFunction<T> keyExtractor, final long startAt,
                                             final int pageSize) {

        return paginateWithQueryParams(tProducer, keyExtractor, startAt, pageSize, "before", true);
    }

    private static <T> Flux<T> paginateWithQueryParams(final Function<Map<String, Object>, Flux<T>> tProducer,
                                                       final ToLongFunction<T> keyExtractor, final long startAt,
                                                       final int pageSize, final String queryKey,
                                                       final boolean reverse) {

        final LongFunction<Flux<T>> nextPage = id -> {
            final Map<String, Object> parameters = new HashMap<>(2);
            parameters.put("limit", pageSize);
            parameters.put(queryKey, id);

            return tProducer.apply(parameters);
        };

        return paginate(nextPage, keyExtractor, startAt, reverse);
    }

    private static <T> Flux<T> paginate(final LongFunction<Flux<T>> nextPage, final ToLongFunction<T> keyExtractor,
                                        final long startAt, final boolean reverse) {

        final ToLongFunction<List<T>> updateLast = list ->
                list.isEmpty() ? startAt : keyExtractor.applyAsLong(list.get(list.size() - 1));

        final Comparator<T> comparator = Comparator.comparingLong(keyExtractor);
        final AtomicLong previousStart = new AtomicLong(startAt);

        return Flux.defer(() -> nextPage.apply(previousStart.get()))
                .sort(reverse ? comparator.reversed() : comparator)
                .collectList()
                .doOnNext(list -> previousStart.set(updateLast.applyAsLong(list)))
                .flatMapMany(Flux::fromIterable)
                .repeat(() -> previousStart.get() != startAt);
    }

    public static <T> Flux<T> paginateBefore(final Function<Map<String, Object>, Flux<T>> tProducer,
                                             final Function<T, String> keyExtractor, final String startAt,
                                             final int pageSize) {

        return paginateWithQueryParams(tProducer, keyExtractor, startAt, pageSize, "before", true);
    }

    private static <T> Flux<T> paginateWithQueryParams(final Function<Map<String, Object>, Flux<T>> tProducer,
                                                       final Function<T, String> keyExtractor, final String startAt,
                                                       final int pageSize, final String queryKey,
                                                       final boolean reverse) {

        final Function<String, Flux<T>> nextPage = id -> {
            final Map<String, Object> parameters = new HashMap<>(2);
            parameters.put("limit", pageSize);
            parameters.put(queryKey, id);

            return tProducer.apply(parameters);
        };

        return paginate(nextPage, keyExtractor, startAt, reverse);
    }

    private static <T> Flux<T> paginate(final Function<String, Flux<T>> nextPage, final Function<T, String> keyExtractor,
                                        final String startAt, final boolean reverse) {

        final Function<List<T>, String> updateLast = list ->
            list.isEmpty() ? null : keyExtractor.apply(list.get(list.size() - 1));

        final Comparator<T> comparator = Comparator.comparing(keyExtractor);
        final AtomicReference<String> previousStart = new AtomicReference<>(startAt);

        return Flux.defer(() -> nextPage.apply(previousStart.get()))
            .sort(reverse ? comparator.reversed() : comparator)
            .collectList()
            .doOnNext(list -> previousStart.set(updateLast.apply(list)))
            .flatMapMany(Flux::fromIterable)
            .repeat(() -> {
                return previousStart.get() != null;
            });
    }
}
