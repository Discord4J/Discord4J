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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.User;
import discord4j.core.util.AuditLogUtil;
import discord4j.discordjson.json.AuditLogEntryData;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A single action recorded by an {@link AuditLogPart}.
 * <p>
 * Use {@link #getActionType()} to determine what kind of action occurred, and then {@link #getChange(ChangeKey)} to
 * get information about what changed. For example,
 * <pre>
 * {@code
 * if (entry.getActionType() == CHANNEL_UPDATE) {
 *     entry.getChange(ChangeKey.NAME)
 *         .flatMap(AuditLogChange::getCurrentValue)
 *         .ifPresent(newName -> System.out.println("The channel's new name is " + newName));
 * }
 * }
 * </pre>
 */
public class AuditLogEntry implements Entity {

    /** The maximum amount of characters that can be in an audit log reason. */
    public static final int MAX_REASON_LENGTH = 512;

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The audit log part this entry belongs to. */
    private final AuditLogPart auditLogPart;

    /** The raw data as represented by Discord. */
    private final AuditLogEntryData data;

    AuditLogEntry(final GatewayDiscordClient gateway, final AuditLogPart auditLogPart,
                  final AuditLogEntryData data) {
        this.gateway = gateway;
        this.auditLogPart = auditLogPart;
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
     * Gets the ID of the user who made the changes, if present.
     *
     * @return The ID of the user who made the changes, if present.
     */
    public Optional<Snowflake> getResponsibleUserId() {
        return data.userId().map(Snowflake::of);
    }

    /**
     * Gets the user who made the changes, if present.
     *
     * @return The user who made the changes, if present.
     */
    public Optional<User> getResponsibleUser() {
        return getResponsibleUserId()
                .map(id -> auditLogPart.getUserById(id)
                        .orElseThrow(() -> new NoSuchElementException("Audit log users does not contain responsible user ID.")));
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
     * Gets a change that was recorded by this entry. The possible changes correspond to each {@link ChangeKey}.
     *
     * @param changeKey The audit log change key.
     * @param <T> The type of the audit log change key.
     * @return The change made to the target, if present.
     */
    public <T> Optional<AuditLogChange<T>> getChange(ChangeKey<T> changeKey) {
        return data.changes().toOptional()
                .map(list -> list.stream().collect(AuditLogUtil.changeCollector()))
                .flatMap(map -> Optional.ofNullable(map.get(changeKey.getName())))
                .map(changeData -> {
                    T oldValue = changeData.oldValue().toOptional()
                            .map(v -> changeKey.parseValue(this, v))
                            .orElse(null);

                    T newValue = changeData.oldValue().toOptional()
                            .map(v -> changeKey.parseValue(this, v))
                            .orElse(null);

                    return new AuditLogChange<>(oldValue, newValue);
                });
    }

    /**
     * Gets one of the optional extra pieces of information recorded by an audit log entry. The possible options
     * correspond to each {@link OptionKey}.
     *
     * @param optionKey The option key.
     * @param <T> The type of the option key.
     * @return The option, if present.
     */
    public <T> Optional<T> getOption(OptionKey<T> optionKey) {
        return data.options().toOptional()
                .map(AuditLogUtil::createOptionMap)
                .map(map -> optionKey.parseValue(map.get(optionKey.getField())));
    }

    /**
     * Gets the {@link AuditLogPart audit log part} that this entry belongs to.
     *
     * @return The audit log part that this entry belongs to.
     */
    public AuditLogPart getParent() {
        return auditLogPart;
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
