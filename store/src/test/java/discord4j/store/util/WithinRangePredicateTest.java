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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WithinRangePredicateTest {

    private final String strStart = "a", strEnd = "m";

    @Test
    public void test() {
        WithinRangePredicate<String> predicate = new WithinRangePredicate<>(strStart, strEnd);
        assertTrue(predicate.test("a"));
        assertTrue(predicate.test("c"));
        assertFalse(predicate.test("z"));
        assertFalse(predicate.test("m"));
    }
}
