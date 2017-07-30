/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.audit.entry.option;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class OptionMap {

	private final Map<OptionKey<?>, Object> backing;

	public OptionMap() {
		this(new HashMap<>());
	}

	private OptionMap(Map<OptionKey<?>, Object> backing) {
		this.backing = backing;
	}

	public <V> V get(OptionKey<V> key) {
		return (V) backing.get(key);
	}

	public <V> V put(OptionKey<V> key, V value) {
		return (V) backing.put(key, value);
	}
}
