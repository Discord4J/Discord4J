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
import discord4j.store.dsl.Property;
import discord4j.store.primitive.ForwardingStoreService;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class StoreTests {

    private final StoreServiceLoader provider = new StoreServiceLoader();

    @SuppressWarnings("ConstantConditions")
    private Store<String, String> newStore() {
        return provider.newGenericStore(String.class, String.class);
    }

    @Test
    public void testServiceDiscovery() {
        assertEquals(TestService.class, provider.getGenericStoreProvider().getClass());
    }

    @Test
    public void testGenericFallback() {
        assertEquals(ForwardingStoreService.class, provider.getLongObjStoreProvider().getClass());
        assertEquals(TestService.class,
                ((ForwardingStoreService) provider.getLongObjStoreProvider()).getOriginal().getClass());
    }

    @Test
    public void testGenericStore() {
        Store<String, String> store = newStore();
        new StoreTest(Objects.requireNonNull(store)).test();
    }

    @Test
    public void testQuery() {
        Store<String, TestBean> store = provider.newGenericStore(String.class, TestBean.class);
        store.save("hello", new TestBean("hello", "world", 1))
             .then(store.save("hello2", new TestBean("hello2", "world2", 0)))
             .subscribe();
        TestBean value = store.query()
        .filter(f -> {
            return f.match(new Property<>(TestBean.class, "key"), "hello")
                    .and(f.match(new Property<>(TestBean.class, "value"), "world"))
                    .and(f.within(new Property<>(TestBean.class, "arbitrary"), 1, 100));
        })
        .selectOne()
        .block();
        assertEquals("world", value.getValue());
        assertEquals(1, value.getArbitrary());
    }

    public class TestBean implements Serializable {

        private String key;
        private String value;
        private int arbitrary;

        public TestBean(String key, String value, int arbitrary) {
            this.key = key;
            this.value = value;
            this.arbitrary = arbitrary;
        }

        public TestBean() {}

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getArbitrary() {
            return arbitrary;
        }

        public void setArbitrary(int arbitrary) {
            this.arbitrary = arbitrary;
        }
    }

    class StoreTest {

        private Store<String, String> connection;

        StoreTest(Store<String, String> connection) {
            this.connection = connection;
        }

        // We don't really care about order, lists were only used in this test because of laziness
        <T> boolean lenientListEquals(List<T> l1, List<T> l2) {
            if (l1.size() != l2.size()) {
                return false;
            }

            boolean equals = true;
            for (T x : l1) {
                equals = l2.contains(x) && equals;
            }
            return equals;
        }

        void test() {
            testPut();
            testFind();
            assertEquals(4L, (long) connection.count().block());
            assertTrue(lenientListEquals(Arrays.asList("hello", "hello1", "hello2", "hello3"),
                    connection.keys().collectList().block()));
            assertTrue(lenientListEquals(Arrays.asList("world", "world1", "world2", "world3"),
                    connection.values().collectList().block()));
            assertTrue(lenientListEquals(Arrays.asList(Tuples.of("hello", "world"), Tuples.of("hello1", "world1"),
                    Tuples.of("hello2", "world2"), Tuples.of("hello3", "world3")),
                    connection.entries().collectList().block()));
            testDelete();
        }

        void testPut() {
            connection.save("hello", "world").block();
            connection.save(Mono.defer(() -> Mono.just(Tuples.of("hello1", "world1")))).block();
            connection.save(Flux.fromArray(new Tuple2[]{Tuples.of("hello2", "world2"), Tuples.of("hello3", "world3")}))
                    .block();
        }

        void testFind() {
            assertEquals("world", connection.find("hello").block());
            assertTrue(lenientListEquals(Arrays.asList("world", "world1", "world2", "world3"),
                    connection.values().collectList().block()));
            assertTrue(lenientListEquals(Arrays.asList("world1", "world2"),
                    connection.findInRange("hello1", "hello3").collectList().block()));
        }

        void testDelete() {
            connection.delete("hello").block();
            connection.delete(Mono.defer(() -> Mono.just("hello1"))).block();
            connection.delete(Flux.fromIterable(Arrays.asList("hello2", "hello3"))).block();
            assertNotEquals(1L, (long) connection.count().block());
            assertEquals(0L, (long) connection.count().block());
            testPut();
            assertEquals(4L, (long) connection.count().block());
            connection.deleteInRange("hello2", "hello4").block();
            assertEquals(2L, (long) connection.count().block());
            connection.deleteAll().block();
            assertEquals(0L, (long) connection.count().block());
        }
    }
}
