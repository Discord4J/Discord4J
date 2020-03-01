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

import discord4j.discordjson.json.AuditLogEntryData;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.util.Snowflake;

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

    public Optional<Snowflake> getTargetId() {
        return data.targetId()
            .filter(it -> !it.equals("0"))
            .map(Snowflake::of);
    }

    public Snowflake getResponsibleUserId() {
        return Snowflake.of(data.userId());
    }

    public Optional<String> getReason() {
        return data.reason().toOptional();
    }

    public ActionType getActionType() {
        return ActionType.of(data.actionType());
    }

    // FIXME: reimplement these
//    @SuppressWarnings("unchecked")
//    public <T> Optional<AuditLogChange<T>> getChange(ChangeKey<T> changeKey) {
//        return Optional.ofNullable((AuditLogChange<T>) data.changes().get(changeKey.getName()));
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T> Optional<T> getOption(OptionKey<T> optionKey) {
//        return Optional.ofNullable((T) data.getOptions().get(optionKey.getField()));
//    }

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
