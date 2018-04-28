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

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaginationUtilTest {

    @Test
    public void testAfter() {
        Function<Map<String, Object>, Flux<Long>> makeRequest = PageSource::getPage;
        Flux<Long> actual = PaginationUtil.paginateAfter(makeRequest, Function.identity(), 0L, 100)
                .limitRequest(300);

        Iterable<Long> expected = Stream.iterate(1L, it -> it + 1).limit(300).collect(Collectors.toList());

        StepVerifier.create(actual)
                .expectNextSequence(expected)
                .expectComplete()
                .verify();
    }

    @Test
    public void testBefore() {
        Function<Map<String, Object>, Flux<Long>> makeRequest = PageSource::getPage;
        Flux<Long> actual = PaginationUtil.paginateBefore(makeRequest, Function.identity(), 300L, 100)
                .limitRequest(300);

        Iterable<Long> expected = Stream.iterate(299L, it -> it - 1).limit(300).collect(Collectors.toList());

        StepVerifier.create(actual)
                .expectNextSequence(expected)
                .expectComplete()
                .verify();
    }

    private static class PageSource {
        static Flux<Long> getPage(Map<String, Object> params) {
            if (params.containsKey("after")) {
                return getPageAfter((Long) params.get("after"), (Integer) params.get("limit"));
            } else {
                return getPageBefore((Long) params.get("before"), (Integer) params.get("limit"));
            }
        }

        static Flux<Long> getPageAfter(long after, int limit) {
            return Flux.<Long, Long>generate(() -> after, (state, sink) -> {
                sink.next(state + 1);
                return state + 1;
            }).limitRequest(limit);
        }

        static Flux<Long> getPageBefore(long before, int limit) {
            return Flux.<Long, Long>generate(() -> before, (state, sink) -> {
                sink.next(state - 1);
                return state - 1;
            }).limitRequest(limit);
        }
    }
}
