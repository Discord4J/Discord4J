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

import discord4j.store.Store;
import discord4j.store.primitive.ForwardingStoreService;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class StoreTests {

    private final StoreServiceLoader provider = new StoreServiceLoader();

    @SuppressWarnings("ConstantConditions")
    private Store<String, String> newStore() {
        return provider.newGenericStore(String.class, String.class).block();
    }

    @Test
    public void testServiceDiscovery() {
        assertEquals(TestService.class, provider.getGenericStoreProvider().getClass());
    }

    @Test
    public void testGenericFallback() {
        assertEquals(ForwardingStoreService.class, provider.getLongObjStoreProvider().getClass());
        assertEquals(TestService.class, ((ForwardingStoreService) provider.getLongObjStoreProvider()).getOriginal().getClass());
    }

    @Test
    public void testGenericStore() {
        Store<String, String> store = newStore();
        new StoreTest(Objects.requireNonNull(store)).test();
    }

    class StoreTest {

        private Store<String, String> connection;

        StoreTest(Store<String, String> connection) {
            this.connection = connection;
        }

        <T> boolean lenientListEquals(List<T> l1, List<T> l2) { //We don't really care about order, lists were only used in this test because of laziness
            if (l1.size() != l2.size())
                return false;

            boolean equals = true;
            for (T x : l1) {
                equals = l2.contains(x) && equals;
            }
            return equals;
        }

        void test() {
            testPut();
            testFind();
            testExists();
            assertEquals(6L, (long) connection.count().block());
            assertTrue(lenientListEquals(Arrays.asList("hello", "hello1", "hello2", "hello3", "hello4", "hello5"),
                    connection.keys().collectList().block()));
            assertTrue(lenientListEquals(Arrays.asList("world", "world1", "world2", "world3", "world4", "world5"),
                    connection.values().collectList().block()));
            assertTrue(lenientListEquals(Arrays.asList(Tuples.of("hello", "world"), Tuples.of("hello1", "world1"),
                    Tuples.of("hello2", "world2"), Tuples.of("hello3", "world3"), Tuples.of("hello4", "world4"),
                    Tuples.of("hello5", "world5")),
                    connection.entries().collectList().block()));
            testDelete();
        }

        void testPut() {
            connection.store("hello", "world").block();
            connection.store(Mono.defer(() -> Mono.just(Tuples.of("hello1", "world1")))).block();
            connection.store(Flux.fromArray(new Tuple2[]{Tuples.of("hello2", "world2"), Tuples.of("hello3", "world3")})).block();
            connection.store(Arrays.asList(Tuples.of("hello4", "world4"), Tuples.of("hello5", "world5"))).block();
        }

        void testFind() {
            assertEquals("world", connection.find("hello").block());
            assertEquals("world1", connection.findAll(Mono.just("hello1")).blockFirst());
            assertTrue(lenientListEquals(Arrays.asList("world2", "world3"), connection.findAll(Flux.fromArray(new String[]{"hello2", "hello3"})).collectList().block()));
            assertTrue(lenientListEquals(Arrays.asList("world4", "world5"), connection.findAll(Arrays.asList("hello4", "hello5")).collectList().block()));
            assertTrue(lenientListEquals(Arrays.asList("world", "world1", "world2", "world3", "world4", "world5"), connection.findAll().collectList().block()));
            assertTrue(lenientListEquals(Arrays.asList("world1", "world2"), connection.findInRange("hello1", "hello3").collectList().block()));
        }

        void testExists() {
            assertTrue(connection.exists("hello").block());
            assertFalse(connection.exists("olleh").block());
            assertTrue(connection.exists(Mono.defer(() -> Mono.just("hello1"))).block());
            assertFalse(connection.exists(Mono.defer(() -> Mono.just("1olleh"))).block());
            assertTrue(connection.exists(Flux.fromIterable(Arrays.asList("hello2", "hello3", "hello4"))).block());
            assertFalse(connection.exists(Flux.fromIterable(Arrays.asList("hello4", "hello5", "hello6"))).block());
        }

        void testDelete() {
            connection.delete("hello").block();
            connection.delete(Mono.defer(() -> Mono.just("hello1"))).block();
            connection.delete(Flux.fromIterable(Arrays.asList("hello2", "hello3"))).block();
            connection.delete(Tuples.of("hello4", "w")).block();
            assertNotEquals(1L, (long) connection.count().block());
            connection.deleteAll(Arrays.asList(Tuples.of("hello4", "world4"), Tuples.of("hello5", "world5"))).block();
            assertEquals(0L, (long) connection.count().block());
            testPut();
            connection.deleteAll(Flux.fromIterable(Arrays.asList(Tuples.of("hello", "world"), Tuples.of("hello1", "world1")))).block();
            assertEquals(4L, (long) connection.count().block());
            connection.deleteInRange("hello2", "hello4").block();
            assertEquals(2L, (long) connection.count().block());
            connection.deleteAll().block();
            assertEquals(0L, (long) connection.count().block());
        }
    }
}
