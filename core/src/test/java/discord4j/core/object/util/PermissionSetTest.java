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
package discord4j.core.object.util;

import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import org.junit.jupiter.api.Test;

import static discord4j.rest.util.Permission.*;
import static org.junit.jupiter.api.Assertions.*;

public class PermissionSetTest {

    @Test
    public void testNone() {
        PermissionSet permSet = PermissionSet.none();
        assertEquals(0, permSet.size());
    }

    @Test
    public void testAll() {
        PermissionSet permSet = PermissionSet.all();
        assertEquals(Permission.values().length, permSet.size());
    }

    @Test
    public void testCustom() {
        PermissionSet permSet = PermissionSet.of(ADD_REACTIONS, MANAGE_ROLES);
        assertEquals(2, permSet.size());
        assertTrue(permSet.contains(ADD_REACTIONS));
        assertTrue(permSet.contains(MANAGE_ROLES));
        assertFalse(permSet.contains(BAN_MEMBERS));
    }

    @Test
    public void testAnd() {
        PermissionSet set0 = PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS);
        PermissionSet set1 = PermissionSet.of(KICK_MEMBERS);
        PermissionSet result = set0.and(set1);

        assertEquals(1, result.size());
        assertEquals(PermissionSet.of(KICK_MEMBERS), result);
    }

    @Test
    public void testOr() {
        PermissionSet set0 = PermissionSet.of(KICK_MEMBERS);
        PermissionSet set1 = PermissionSet.of(BAN_MEMBERS);
        PermissionSet result = set0.or(set1);

        assertEquals(2, result.size());
        assertEquals(PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS), result);
    }

    @Test
    public void testXor() {
        PermissionSet set0 = PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS, ATTACH_FILES);
        PermissionSet set1 = PermissionSet.of(ATTACH_FILES, CONNECT);
        PermissionSet result = set0.xor(set1);

        assertEquals(3, result.size());
        assertEquals(PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS, CONNECT), result);
    }

    @Test
    public void testAndNot() {
        PermissionSet set0 = PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS, ATTACH_FILES);
        PermissionSet set1 = PermissionSet.of(BAN_MEMBERS, ATTACH_FILES, CONNECT);
        PermissionSet result = set0.andNot(set1);

        assertEquals(1, result.size());
        assertEquals(PermissionSet.of(KICK_MEMBERS), result);
    }

    @Test
    public void testNot() {
        PermissionSet set = PermissionSet.none();
        PermissionSet result = set.not();

        assertEquals(Permission.values().length, result.size());
        assertEquals(PermissionSet.all(), result);
    }
}
