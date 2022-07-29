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
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.Webhook;
import discord4j.discordjson.json.AuditLogData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A part of a guild's audit logs. This holds all of the webhooks, users, and audit log entries for a portion of
 * a guild's audit log.
 */
public class AuditLogPart {

    private final long guildId;

    private final Set<Webhook> webhooks;

    private final Set<User> users;

    private final /*~~>*/List<AuditLogEntry> entries;

    public AuditLogPart(long guildId, GatewayDiscordClient gateway, AuditLogData data) {
        this.guildId = guildId;

        this.webhooks = data.webhooks().stream()
                .map(webhookData -> new Webhook(gateway, webhookData))
                .collect(Collectors.toSet());

        this.users = data.users().stream()
                .map(userData -> new User(gateway, userData))
                .collect(Collectors.toSet());

        this.entries = data.auditLogEntries()
                .stream().map(auditLogEntryData -> new AuditLogEntry(gateway, this, auditLogEntryData))
                .collect(Collectors.toList());
    }

    private AuditLogPart(long guildId, Set<Webhook> webhooks, Set<User> users, /*~~>*/List<AuditLogEntry> entries) {
        this.guildId = guildId;
        this.webhooks = webhooks;
        this.users = users;
        this.entries = entries;
    }

    /**
     * Get the webhooks that are involved in the entries of this portion of the audit log.
     *
     * @return The webhooks that are involved in the entries of this portion of the audit log.
     */
    public Set<Webhook> getWebhooks() {
        return webhooks;
    }

    /**
     * Get the users that are involved in the entries of this portion of the audit log.
     *
     * @return The users that are involved in the entries of this portion of the audit log.
     */
    public Set<User> getUsers() {
        return users;
    }

    /**
     * Gets the entries in this portion of the audit log.
     *
     * @return The entries in this portion of the audit log.
     */
    public /*~~>*/List<AuditLogEntry> getEntries() {
        return entries;
    }

    /**
     * Gets a webhook involved in the entries of this portion of the audit log by ID.
     *
     * @param webhookId The ID of the webhook.
     * @return The webhook with the given ID, if present.
     */
    public Optional<Webhook> getWebhookById(Snowflake webhookId) {
        return webhooks.stream()
                .filter(webhook -> webhook.getId().equals(webhookId))
                .findFirst();
    }

    /**
     * Gets a user involved in the entries of this portion of the audit log by ID.
     *
     * @param userId The ID of the user.
     * @return The user with the given ID, if present.
     */
    public Optional<User> getUserById(Snowflake userId) {
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    /**
     * Gets the ID of the guild associated with this audit log.
     *
     * @return The ID of the guild associated with this audit log.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Combines this portion of the audit log with another portion.
     *
     * @param other The other portion to combine with.
     * @return A new {@link AuditLogPart} that contains all of the webhooks, users, and entries from both parts.
     */
    public AuditLogPart combine(AuditLogPart other) {
        if (other.guildId != this.guildId) {
            throw new IllegalArgumentException("Cannot combine audit log parts from two different guilds.");
        }

        Set<Webhook> combinedWebhooks = new HashSet<>(this.webhooks.size() + other.webhooks.size());
        combinedWebhooks.addAll(this.webhooks);
        combinedWebhooks.addAll(other.webhooks);

        Set<User> combinedUsers = new HashSet<>(this.users.size() + other.users.size());
        combinedUsers.addAll(this.users);
        combinedUsers.addAll(other.users);

        List<AuditLogEntry> combinedEntries = new ArrayList<>(this.entries.size() + other.entries.size());
        combinedEntries.addAll(this.entries);
        combinedEntries.addAll(other.entries);

        return new AuditLogPart(guildId, combinedWebhooks, combinedUsers, combinedEntries);
    }
}
