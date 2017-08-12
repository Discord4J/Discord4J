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

package sx.blah.discord.handle.audit.entry.change;

import java.util.Optional;

/**
 * A change of a value in an {@link sx.blah.discord.handle.audit.entry.AuditLogEntry}.
 *
 * @param <T> The type of the object changed.
 */
public class AuditLogChange<T> {

	private final T oldValue;
	private final T newValue;

	public AuditLogChange(T oldValue, T newValue) {
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * The old value, if it existed.
	 *
	 * <p>This will not be present on action types like entity creations.
	 *
	 * @return The old value, if it existed.
	 */
	public Optional<T> getOldValue() {
		return Optional.ofNullable(oldValue);
	}

	/**
	 * The new value, if it exists.
	 *
	 * <p>This will not be present on action types like entity deletion.
	 *
	 * @return The new value, if it exists.
	 */
	public Optional<T> getNewValue() {
		return Optional.ofNullable(newValue);
	}
}
