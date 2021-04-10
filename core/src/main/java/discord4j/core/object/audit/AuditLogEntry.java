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
package discord4j.core.object.audit;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.util.AuditLogUtil;
import discord4j.discordjson.json.AuditLogEntryData;
import discord4j.common.util.Snowflake;

import java.util.Optional;

public class AuditLogEntry implements Entity {

    /** The maximum amount of characters that can be in an audit log reason. */
    public static final int MAX_REASON_LENGTH = 512;

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    private final AuditLogEntryData data;

    public AuditLogEntry(final GatewayDiscordClient gateway, final AuditLogEntryData data) {
        this.gateway = gateway;
        this.data = data;
    }

    /**
     * Gets the data of the audit log entry.
     *
     * @return The data of the audit log entry.
     */
    public AuditLogEntryData getData() {
        return data;
    }

    /**
     * Gets the id of the affected entity (webhook, user, role, etc.), if present.
     *
     * @return The id of the affected entity (webhook, user, role, etc.), if present.
     */
    public Optional<Snowflake> getTargetId() {
        return data.targetId()
            .filter(it -> !it.equals("0"))
            .map(Snowflake::of);
    }

    /**
     * Gets the user who made the changes.
     *
     * @return The user who made the changes.
     * @deprecated Use {@link AuditLogEntry#getUserId}
     */
    public Snowflake getResponsibleUserId() {
        return getUserId().orElse(null);
    }

    /**
     * Gets the user who made the changes, if present.
     *
     * @return The user who made the changes, if present.
     */
    public Optional<Snowflake> getUserId() {
        return data.userId().map(Snowflake::of);
    }

    /**
     * Gets the reason for the change, if present.
     *
     * @return The reason for the change, if present.
     */
    public Optional<String> getReason() {
        return data.reason().toOptional();
    }

    /**
     * Gets type of action that occurred.
     *
     * @return The type of action that occurred.
     */
    public ActionType getActionType() {
        return ActionType.of(data.actionType());
    }

    /**
     * Gets the changes made to the target id, if present.
     *
     * @param changeKey The audit log change key.
     * @param <T> The type of the audit log change key.
     * @return The changes made to the target id, if present.
     */
    public <T> Optional<AuditLogChange<T>> getChange(ChangeKey<T> changeKey) {
        return data.changes().toOptional()
                .map(list -> list.stream().collect(AuditLogUtil.changeCollector()))
                .map(map -> map.get(changeKey.getName()))
                .map(AuditLogEntry::cast);
    }

    @SuppressWarnings("unchecked")
    private static <T> AuditLogChange<T> cast(AuditLogChange<?> strategy) {
        return (AuditLogChange<T>) strategy;
    }

    public <T> Optional<T> getOption(OptionKey<T> optionKey) {
        return data.options().toOptional()
                .map(AuditLogUtil::createOptionMap)
                .map(map -> map.get(optionKey.getField()))
                .map(AuditLogEntry::cast);
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value) {
        return (T) value;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public String toString() {
        return "AuditLogEntry{" +
                "data=" + data +
                '}';
    }
}
