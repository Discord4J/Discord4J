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
package discord4j.core.object;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
		PermissionSet permSet = PermissionSet.of(Permission.ADD_REACTIONS, Permission.MANAGE_ROLES);
		assertEquals(2, permSet.size());
		assertTrue(permSet.contains(Permission.ADD_REACTIONS));
		assertTrue(permSet.contains(Permission.MANAGE_ROLES));
	}

}
