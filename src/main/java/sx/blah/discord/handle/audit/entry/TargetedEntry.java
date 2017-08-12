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
import sx.blah.discord.handle.audit.entry.change.ChangeMap;
import sx.blah.discord.handle.audit.entry.option.OptionMap;
import sx.blah.discord.handle.obj.IUser;

/**
 * An audit log entry which has a target.
 */
public class TargetedEntry extends AuditLogEntry {

	private final long targetID;

	public TargetedEntry(long id, IUser user, ChangeMap changes, String reason, ActionType actionType, OptionMap options, long targetID) {
		super(id, user, changes, reason, actionType, options);
		this.targetID = targetID;
	}

	/**
	 * Gets the ID of the target.
	 *
	 * @return The ID of the target.
	 */
	public long getTargetID() {
		return targetID;
	}
}
