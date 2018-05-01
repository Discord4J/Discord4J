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

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.util.Snowflake;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class AuditLogEntry implements Entity {

    private final DiscordClient client;
    private final long id;
    private final long targetId;
    private final long responsibleUserId;
    private final String reason;
    private final ActionType actionType;
    private final Map<String, AuditLogChange<?>> changes;
    private final Map<String, ?> options;

    public AuditLogEntry(DiscordClient client, long id, long targetId, long responsibleUserId, @Nullable String reason,
                         ActionType actionType, Map<String, AuditLogChange<?>> changes, Map<String, ?> options) {
        this.client = client;
        this.id = id;
        this.targetId = targetId;
        this.responsibleUserId = responsibleUserId;
        this.reason = reason;
        this.actionType = actionType;
        this.changes = changes;
        this.options = options;
    }

    public Optional<Snowflake> getTargetId() {
        return targetId == 0 ? Optional.empty() : Optional.of(Snowflake.of(targetId));
    }

    public Snowflake getResponsibleUserId() {
        return Snowflake.of(responsibleUserId);
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    public ActionType getActionType() {
        return actionType;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<AuditLogChange<T>> getChange(ChangeKey<T> changeKey) {
        return Optional.ofNullable((AuditLogChange<T>) changes.get(changeKey.getName()));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOption(OptionKey<T> optionKey) {
        return Optional.ofNullable((T) options.get(optionKey.getField()));
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    @Override
    public DiscordClient getClient() {
        return client;
    }
}
