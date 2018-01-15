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

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class MappingIterableTest {

    private static Iterable<Integer> makeIterable() {
        return Arrays.asList(1, 2, 3, 4, 5);
    }

    private static MappingIterable<Integer, String> map(Iterable<Integer> original) {
        return new MappingIterable<>(String::valueOf, original);
    }

    @Test
    public void testMapping() {
        Iterable<Integer> original = makeIterable();
        MappingIterable<Integer, String> mappingIterable = map(original);
        StringBuilder collection = new StringBuilder();
        for (String s : mappingIterable)
            collection.append(s);
        assertEquals("12345", collection.toString());
    }
}
