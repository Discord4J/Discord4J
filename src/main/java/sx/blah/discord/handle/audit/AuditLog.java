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

package sx.blah.discord.handle.audit;

import sx.blah.discord.handle.audit.entry.AuditLogEntry;
import sx.blah.discord.handle.audit.entry.DiscordObjectEntry;
import sx.blah.discord.handle.audit.entry.TargetedEntry;
import sx.blah.discord.handle.audit.entry.option.OptionKey;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IWebhook;
import sx.blah.discord.util.cache.LongMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {
	private final LongMap<AuditLogEntry> entries;

	public AuditLog(LongMap<AuditLogEntry> entries) {
		this.entries = entries;
	}

	public LongMap<AuditLogEntry> getEntryMap() {
		return entries;
	}

	public Collection<AuditLogEntry> getEntries() {
		return entries.values();
	}

	public Collection<TargetedEntry> getTargetedEntries() {
		return entries.values().stream()
				.filter(TargetedEntry.class::isInstance)
				.map(TargetedEntry.class::cast)
				.collect(Collectors.toList());
	}

	public Collection<DiscordObjectEntry<?>> getDiscordObjectEntries() {
		return entries.values().stream()
				.filter(DiscordObjectEntry.class::isInstance)
				.map(entry -> (DiscordObjectEntry<?>) entry)
				.collect(Collectors.toList());
	}

	public <T extends IDiscordObject<T>> Collection<DiscordObjectEntry<T>> getDiscordObjectEntries(Class<T> clazz) {
		return getDiscordObjectEntries().stream()
				.filter(entry -> clazz.isAssignableFrom(entry.getTarget().getClass()))
				.map(entry -> (DiscordObjectEntry<T>) entry)
				.collect(Collectors.toList());
	}

	public AuditLogEntry getEntryByID(long id) {
		return getEntryMap().get(id);
	}

	public List<AuditLogEntry> getEntriesByType(ActionType actionType) {
		return getEntries().stream()
				.filter(entry -> entry.getActionType() == actionType)
				.collect(Collectors.toList());
	}

	public List<TargetedEntry> getEntriesByTarget(long targetID) {
		return getEntries().stream()
				.filter(TargetedEntry.class::isInstance)
				.map(TargetedEntry.class::cast)
				.filter(entry -> entry.getTargetID() == targetID)
				.collect(Collectors.toList());
	}

	public List<AuditLogEntry> getEntriesByResponsibleUser(IUser user) {
		return getEntries().stream()
				.filter(entry -> entry.getResponsibleUser().equals(user))
				.collect(Collectors.toList());
	}
}
