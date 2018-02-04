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
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.cache.LongMap;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A view of a guild's audit log at the time of its construction.
 *
 * @see IGuild#getAuditLog()
 */
public class AuditLog {

	private final LongMap<AuditLogEntry> entries;

	public AuditLog(LongMap<AuditLogEntry> entries) {
		this.entries = entries;
	}

	/**
	 * Gets the map of entries for the log.
	 *
	 * @return The map of entries for the log.
	 */
	public LongMap<AuditLogEntry> getEntryMap() {
		return entries;
	}

	/**
	 * Gets the entries for the log as a collection.
	 *
	 * @return The entries for the log as a collection.
	 */
	public Collection<AuditLogEntry> getEntries() {
		return entries.values();
	}

	/**
	 * Gets the entries of the log which have a target.
	 *
	 * @return The entries of the log which have a target.
	 * @see TargetedEntry
	 */
	public Collection<TargetedEntry> getTargetedEntries() {
		return entries.values().stream()
				.filter(TargetedEntry.class::isInstance)
				.map(TargetedEntry.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the entries of the log which have a target which is a Discord object.
	 *
	 * @return The entries of the log which have a target which is a Discord object.
	 * @see DiscordObjectEntry
	 */
	public Collection<DiscordObjectEntry<?>> getDiscordObjectEntries() {
		return entries.values().stream()
				.filter(DiscordObjectEntry.class::isInstance)
				.map(entry -> (DiscordObjectEntry<?>) entry)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the entries of the log which have a target which is of the given type.
	 *
	 * @param clazz The class of the type of target to search for.
	 * @param <T> The type of target to search for.
	 * @return The entries of the log which have a target which is of the given type.
	 */
	public <T extends IDiscordObject<T>> Collection<DiscordObjectEntry<T>> getDiscordObjectEntries(Class<T> clazz) {
		return getDiscordObjectEntries().stream()
				.filter(entry -> clazz.isAssignableFrom(entry.getTarget().getClass()))
				.map(entry -> (DiscordObjectEntry<T>) entry)
				.collect(Collectors.toList());
	}

	/**
	 * Gets an entry by its unique snowflake ID.
	 *
	 * @param id The ID of the desired entry.
	 * @return The entry with the given ID (or null if one was not found).
	 */
	public AuditLogEntry getEntryByID(long id) {
		return getEntryMap().get(id);
	}

	/**
	 * Gets the entries with the given action type.
	 *
	 * @param actionType The action type of the desired entries.
	 * @return The entries with the given action type.
	 */
	public List<AuditLogEntry> getEntriesByType(ActionType actionType) {
		return getEntries().stream()
				.filter(entry -> entry.getActionType() == actionType)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the entries which have a target which have the given unique snowflake ID.
	 *
	 * @param targetID The ID of the target of the desired entries.
	 * @return The entries which have a target which have the given unique snowflake ID.
	 */
	public List<TargetedEntry> getEntriesByTarget(long targetID) {
		return getEntries().stream()
				.filter(TargetedEntry.class::isInstance)
				.map(TargetedEntry.class::cast)
				.filter(entry -> entry.getTargetID() == targetID)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the entries with the given responsible user.
	 *
	 * @param user The user responsible for the desired entries.
	 * @return The entries with the given responsible user.
	 */
	public List<AuditLogEntry> getEntriesByResponsibleUser(IUser user) {
		return getEntries().stream()
				.filter(entry -> entry.getResponsibleUser().equals(user))
				.collect(Collectors.toList());
	}
}
