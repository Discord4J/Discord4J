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

	public IUser getResponsibleUser() {
		return user;
	}

	public ChangeMap getChanges() {
		return changes;
	}

	public Optional<String> getReason() {
		return Optional.ofNullable(reason);
	}

	public ActionType getActionType() {
		return actionType;
	}

	public OptionMap getOptions() {
		return options;
	}

	public <T> AuditLogChange<T> getChangeByKey(ChangeKey<T> key) {
		return getChanges().get(key);
	}

	public <T> T getOptionByKey(OptionKey<T> key) {
		return getOptions().get(key);
	}
}
