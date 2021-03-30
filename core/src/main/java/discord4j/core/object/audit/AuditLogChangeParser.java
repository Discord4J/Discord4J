package discord4j.core.object.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.discordjson.json.OverwriteData;
import discord4j.rest.util.PermissionSet;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public interface AuditLogChangeParser<T> extends BiFunction<AuditLogEntry, JsonNode, T> {

    AuditLogChangeParser<String> STRING_PARSER = (entry, node) -> node.asText();

    AuditLogChangeParser<Integer> INTEGER_PARSER = (entry, node) -> node.asInt();

    AuditLogChangeParser<Boolean> BOOLEAN_PARSER = (entry, node) -> node.asBoolean();

    AuditLogChangeParser<Snowflake> SNOWFLAKE_PARSER = (entry, node) -> Snowflake.of(node.asText());

    AuditLogChangeParser<PermissionSet> PERMISSION_SET_PARSER = (entry, node) -> PermissionSet.of(node.asText());

    AuditLogChangeParser<Set<AuditLogRole>> AUDIT_LOG_ROLES_PARSER = (entry, node) -> {
        Set<AuditLogRole> roles = new HashSet<>();
        for (JsonNode obj : node) {
            roles.add(new AuditLogRole(Snowflake.asLong(obj.get("id").asText()), obj.get("name").asText()));
        }
        return roles;
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
