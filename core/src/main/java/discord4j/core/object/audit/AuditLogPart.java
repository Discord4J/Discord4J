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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuditLogPart {

    private final long guildId;

    private final List<Webhook> webhooks;

    private final List<User> users;

    private final List<AuditLogEntry> entries;

    public AuditLogPart(long guildId, GatewayDiscordClient gateway, AuditLogData data) {
        this.guildId = guildId;

        this.webhooks = data.webhooks().stream()
                .map(webhookData -> new Webhook(gateway, webhookData))
                .collect(Collectors.toList());

        this.users = data.users().stream()
                .map(userData -> new User(gateway, userData))
                .collect(Collectors.toList());

        this.entries = data.auditLogEntries()
                .stream().map(auditLogEntryData -> new AuditLogEntry(gateway, this, auditLogEntryData))
                .collect(Collectors.toList());
    }

    private AuditLogPart(long guildId, List<Webhook> webhooks, List<User> users, List<AuditLogEntry> entries) {
        this.guildId = guildId;
        this.webhooks = webhooks;
        this.users = users;
        this.entries = entries;
    }

    public List<Webhook> getWebhooks() {
        return webhooks;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<AuditLogEntry> getEntries() {
        return entries;
    }

    public Optional<Webhook> getWebhookById(Snowflake webhookId) {
        return webhooks.stream()
                .filter(webhook -> webhook.getId().equals(webhookId))
                .findFirst();
    }

    public Optional<User> getUserById(Snowflake userId) {
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    public AuditLogPart combine(AuditLogPart other) {
        if (other.guildId != this.guildId) {
            throw new IllegalArgumentException("Cannot combine audit log parts from two different guilds.");
        }

        List<Webhook> combinedWebhooks = new ArrayList<>(this.webhooks.size() + other.webhooks.size());
        combinedWebhooks.addAll(this.webhooks);
        combinedWebhooks.addAll(other.webhooks);

        List<User> combinedUsers = new ArrayList<>(this.users.size() + other.users.size());
        combinedUsers.addAll(this.users);
        combinedUsers.addAll(other.users);

        List<AuditLogEntry> combinedEntries = new ArrayList<>(this.entries.size() + other.entries.size());
        combinedEntries.addAll(this.entries);
        combinedEntries.addAll(other.entries);

        return new AuditLogPart(guildId, combinedWebhooks, combinedUsers, combinedEntries);
    }
}
