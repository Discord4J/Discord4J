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
import discord4j.core.internal.ServiceMediator;
import discord4j.core.internal.data.AuditLogEntryBean;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.util.Snowflake;

import java.util.Optional;

public class AuditLogEntry implements Entity {

    private final ServiceMediator serviceMediator;
    private final AuditLogEntryBean data;

    public AuditLogEntry(final ServiceMediator serviceMediator, final AuditLogEntryBean data) {
        this.serviceMediator = serviceMediator;
        this.data = data;
    }

    public Optional<Snowflake> getTargetId() {
        return data.getTargetId() == 0 ? Optional.empty() : Optional.of(Snowflake.of(data.getTargetId()));
    }

    public Snowflake getResponsibleUserId() {
        return Snowflake.of(data.getResponsibleUserId());
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(data.getReason());
    }

    public ActionType getActionType() {
        return ActionType.of(data.getActionType());
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<AuditLogChange<T>> getChange(ChangeKey<T> changeKey) {
        return Optional.ofNullable((AuditLogChange<T>) data.getChanges().get(changeKey.getName()));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOption(OptionKey<T> optionKey) {
        return Optional.ofNullable((T) data.getOptions().get(optionKey.getField()));
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    @Override
    public String toString() {
        return "AuditLogEntry{" +
                "data=" + data +
                '}';
    }
}
