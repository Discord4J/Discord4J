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

import sx.blah.discord.api.internal.json.objects.audit.AuditLogEntryObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A map of {@link OptionKey OptionKeys} to objects.
 *
 * <p>This is a utility to ensure type-safe access of options. As long as only {@link #put(OptionKey, Object)} is used
 * to write to the map, and only the pre-defined {@link OptionKey OptionKeys} are used, type-safe access is guaranteed.
 */
@SuppressWarnings("unchecked")
public class OptionMap {

	private final Map<OptionKey<?>, Object> backing;

	public OptionMap(AuditLogEntryObject.Options options) {
		this(new HashMap<>());
		if (options != null) {
			if (options.delete_member_days != null) put(OptionKey.DELETE_MEMBER_DAYS, Integer.parseInt(options.delete_member_days));
			if (options.members_removed != null) put(OptionKey.MEMBERS_REMOVED, Integer.parseInt(options.members_removed));
			if (options.channel_id != null) put(OptionKey.CHANNEL_ID, Long.parseUnsignedLong(options.channel_id));
			if (options.count != null) put(OptionKey.COUNT, Integer.parseInt(options.count));
			if (options.id != null) put(OptionKey.ID, Long.parseUnsignedLong(options.id));
			if (options.type != null) put(OptionKey.TYPE, options.type);
			if (options.role_name != null) put(OptionKey.ROLE_NAME, options.role_name);
		}
	}

	private OptionMap(Map<OptionKey<?>, Object> backing) {
		this.backing = backing;
	}

	public <V> V get(OptionKey<V> key) {
		return (V) backing.get(key);
	}

	private <V> V put(OptionKey<V> key, V value) {
		return (V) backing.put(key, value);
	}
}
