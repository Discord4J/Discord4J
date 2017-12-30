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

import java.util.*;
import java.util.stream.Collectors;

public class PermissionSet extends AbstractSet<Permission> {

	private static final PermissionSet ALL = new PermissionSet(0x7ff7fcff);
	private static final PermissionSet NONE = new PermissionSet(0x00000000);

	public static PermissionSet all() {
		return ALL;
	}

	public static PermissionSet none() {
		return NONE;
	}

	public static PermissionSet of(long raw) {
		return new PermissionSet(raw);
	}

	public static PermissionSet of(Permission... perms) {
		return new PermissionSet(Arrays.stream(perms).map(Permission::getValue).reduce(0, (acc, v) -> acc | v));
	}

	private final long raw;

	private PermissionSet(long raw) {
		this.raw = raw;
	}

	public EnumSet<Permission> asEnumSet() {
		List<Permission> list = Arrays.stream(Permission.values()).filter(this::contains).collect(Collectors.toList());
		return EnumSet.copyOf(list);
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Permission)) {
			return false;
		}

		Permission permission = (Permission) o;
		return (raw & permission.getValue()) > 0;
	}

	@Override
	public Iterator<Permission> iterator() {
		return asEnumSet().iterator();
	}

	@Override
	public int size() {
		return Long.bitCount(raw);
	}
}
