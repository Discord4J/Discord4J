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
        PageSource source = new PageSource(Integer.MAX_VALUE);

        Function<Map<String, Object>, Flux<Long>> makeRequest = source::getPage;
        Flux<Long> actual = PaginationUtil.paginateAfter(makeRequest, box -> box, 0L, 100)
                .limitRequest(300);

        Iterable<Long> expected = Stream.iterate(1L, it -> it + 1).limit(300).collect(Collectors.toList());

        StepVerifier.create(actual)
                .expectNextSequence(expected)
                .expectComplete()
                .verify();
    }

    @Test
    public void testBefore() {
        PageSource source = new PageSource(Integer.MAX_VALUE);

        Function<Map<String, Object>, Flux<Long>> makeRequest = source::getPage;
        Flux<Long> actual = PaginationUtil.paginateBefore(makeRequest, box -> box, 300L, 100)
                .limitRequest(300);

        Iterable<Long> expected = Stream.iterate(299L, it -> it - 1).limit(300).collect(Collectors.toList());

        StepVerifier.create(actual)
                .expectNextSequence(expected)
                .expectComplete()
                .verify();
    }

    @Test
    public void testAfterRequestMoreThanAvailable() {
        PageSource source = new PageSource(150);

        Function<Map<String, Object>, Flux<Long>> makeRequest = source::getPage;
        Flux<Long> actual = PaginationUtil.paginateAfter(makeRequest, box -> box, 0L, 100)
                .limitRequest(300);

        Iterable<Long> expected = Stream.iterate(1L, it -> it + 1).limit(150).collect(Collectors.toList());

        StepVerifier.create(actual)
                .expectNextSequence(expected)
                .expectComplete()
                .verify();
    }

    @Test
    public void testBeforeRequestMoreThanAvailable() {
        PageSource source = new PageSource(150);

        Function<Map<String, Object>, Flux<Long>> makeRequest = source::getPage;
        Flux<Long> actual = PaginationUtil.paginateBefore(makeRequest, box -> box, 300L, 100)
                .limitRequest(150);

        Iterable<Long> expected = Stream.iterate(299L, it -> it - 1).limit(150).collect(Collectors.toList());

        StepVerifier.create(actual)
                .expectNextSequence(expected)
                .expectComplete()
                .verify();
    }

    private static class PageSource {

        private final int numberOfItems;
        private int emitted = 0;

        private PageSource(int numberOfItems) {
            this.numberOfItems = numberOfItems;
        }

        Flux<Long> getPage(Map<String, Object> params) {
            if (params.containsKey("after")) {
                return getPageAfter((Long) params.get("after"), (Integer) params.get("limit"));
            } else {
                return getPageBefore((Long) params.get("before"), (Integer) params.get("limit"));
            }
        }

        Flux<Long> getPageAfter(long after, int limit) {
            return Flux.<Long, Long>generate(() -> after, (state, sink) -> {
                if (emitted++ >= numberOfItems) {
                    sink.complete();
                    return state;
                } else {
                    sink.next(state + 1);
                    return state + 1;
                }
            }).limitRequest(limit);
        }

        Flux<Long> getPageBefore(long before, int limit) {
            return Flux.<Long, Long>generate(() -> before, (state, sink) -> {
                if (emitted++ >= numberOfItems) {
                    sink.complete();
                    return state;
                } else {
                    sink.next(state - 1);
                    return state - 1;
                }
            }).limitRequest(limit);
        }
    }
}
