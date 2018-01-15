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
package discord4j.store.util;

import org.junit.Test;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static org.junit.Assert.*;

public class LongObjTuple2Test {

    private final long key = 12345L;
    private final String obj = "hello world";

    @Test
    public void testConstruction() {
        LongObjTuple2<String> tuple = LongObjTuple2.of(key, obj);
        assertNotNull(tuple);
        assertEquals(key, tuple.getT1());
        assertEquals(obj, tuple.getT2());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void testNullObj() {
        LongObjTuple2.of(key, null);
    }

    @Test
    public void testConversion() {
        Tuple2<Long, String> original = Tuples.of(key, obj);
        LongObjTuple2<String> converted = LongObjTuple2.from(original);
        assertNotNull(converted);
        assertEquals((long) original.getT1(), converted.getT1());
        assertEquals(original.getT2(), converted.getT2());
    }

    @Test
    public void testReversion() {
        LongObjTuple2<String> original = LongObjTuple2.of(key, obj);
        Tuple2<Long, String> converted = LongObjTuple2.convert(original);
        assertNotNull(converted);
        assertEquals((Long) original.getT1(), converted.getT1());
        assertEquals(original.getT2(), converted.getT2());
    }
}
