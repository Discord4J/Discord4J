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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.discordjson.json.AuditLogPartialRoleData;
import discord4j.discordjson.json.OverwriteData;
import discord4j.rest.util.PermissionSet;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A function used by each {@link ChangeKey} to parse the {@link JsonNode} held by a
 * {@link discord4j.discordjson.json.AuditLogChangeData} to a higher-level type.
 *
 * @param <T> The type the parser produces
 */
interface AuditLogChangeParser<T> extends BiFunction<AuditLogEntry, JsonNode, T> {

    AuditLogChangeParser<String> STRING_PARSER = (entry, node) -> node.asText();

    AuditLogChangeParser<Integer> INTEGER_PARSER = (entry, node) -> node.asInt();

    AuditLogChangeParser<Boolean> BOOLEAN_PARSER = (entry, node) -> node.asBoolean();

    AuditLogChangeParser<Snowflake> SNOWFLAKE_PARSER = (entry, node) -> Snowflake.of(node.asText());

    AuditLogChangeParser<PermissionSet> PERMISSION_SET_PARSER = (entry, node) -> PermissionSet.of(node.asText());

    AuditLogChangeParser<Instant> INSTANT_PARSER = (entry, node) -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(node.asText(), Instant::from);

    AuditLogChangeParser<Set<AuditLogRole>> AUDIT_LOG_ROLES_PARSER = (entry, node) -> {
        try {
            ObjectMapper mapper = entry.getClient().getCoreResources().getJacksonResources().getObjectMapper();
            List<AuditLogPartialRoleData> roles = mapper.readerForListOf(AuditLogPartialRoleData.class).readValue(node);

            return roles.stream()
                    .map(AuditLogRole::new)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("Could not parse audit log roles");
        }
    };

    AuditLogChangeParser<Set<ExtendedPermissionOverwrite>> OVERWRITES_PARSER = (entry, node) -> {
        try {
            GatewayDiscordClient client = entry.getClient();
            ObjectMapper mapper = client.getCoreResources().getJacksonResources().getObjectMapper();
            List<OverwriteData> overwrites = mapper.readerForListOf(OverwriteData.class).readValue(node);

            long guildId = entry.getParent().getGuildId().asLong();
            long channelId = entry.getTargetId()
                    .orElseThrow(() -> new NoSuchElementException("Audit log entry has no target ID"))
                    .asLong();

            return overwrites.stream()
                    .map(overwriteData -> new ExtendedPermissionOverwrite(client, overwriteData, guildId, channelId))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("Could not parse audit log overwrite data");
        }
    };
}
