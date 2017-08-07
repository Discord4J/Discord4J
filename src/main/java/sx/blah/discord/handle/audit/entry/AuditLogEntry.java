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

package sx.blah.discord.handle.audit.entry;

import sx.blah.discord.handle.audit.ActionType;
import sx.blah.discord.handle.audit.entry.change.AuditLogChange;
import sx.blah.discord.handle.audit.entry.change.ChangeKey;
import sx.blah.discord.handle.audit.entry.change.ChangeMap;
import sx.blah.discord.handle.audit.entry.option.OptionKey;
import sx.blah.discord.handle.audit.entry.option.OptionMap;
import sx.blah.discord.handle.obj.IIDLinkedObject;
import sx.blah.discord.handle.obj.IUser;

import java.util.Optional;

/**
 * An entry in an {@link sx.blah.discord.handle.audit.AuditLog}.
 */
public class AuditLogEntry implements IIDLinkedObject {

	private final long id;
	private final IUser user;
	private final ChangeMap changes;
	private final String reason;
	private final ActionType actionType;
	private final OptionMap options;

	public AuditLogEntry(long id, IUser user, ChangeMap changes, String reason, ActionType actionType, OptionMap options) {
		this.id = id;
		this.user = user;
		this.changes = changes;
		this.reason = reason;
		this.actionType = actionType;
		this.options = options;
	}

	@Override
	public long getLongID() {
		return id;
	}

	/**
	 * Gets the user responsible for the action represented by the entry.
	 *
	 * @return The user responsible for the action represented by the entry.
	 */
	public IUser getResponsibleUser() {
		return user;
	}

	/**
	 * Gets the map of changes made in the entry.
	 *
	 * @return The map of changes made in the entry.
	 */
	public ChangeMap getChanges() {
		return changes;
	}

	/**
	 * Gets the reason given for the entry, if it exists.
	 *
	 * @return The reason given for the entry, if it exists.
	 */
	public Optional<String> getReason() {
		return Optional.ofNullable(reason);
	}

	/**
	 * Gets the type of action represented by the entry.
	 *
	 * @return The type of action represented by the entry.
	 */
	public ActionType getActionType() {
		return actionType;
	}

	/**
	 * Gets the map of options for the entry.
	 *
	 * @return The map of options for the entry.
	 */
	public OptionMap getOptions() {
		return options;
	}

	/**
	 * Gets a change for the given key from the changes map.
	 *
	 * @param key The key to get the change for.
	 * @param <T> The type of the change.
	 * @return The change for the given key.
	 */
	public <T> AuditLogChange<T> getChangeByKey(ChangeKey<T> key) {
		return getChanges().get(key);
	}

	/**
	 * Gets an option for the given key from the options map.
	 *
	 * @param key The key to get the option for.
	 * @param <T> The type of the option.
	 * @return The option for the given key.
	 */
	public <T> T getOptionByKey(OptionKey<T> key) {
		return getOptions().get(key);
	}
}
