package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.audit.ActionType;
import discord4j.core.object.audit.AuditLogEntry;
import discord4j.discordjson.json.AuditLogData;
import discord4j.discordjson.json.AuditLogEntryData;
import discord4j.rest.util.PaginationUtil;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public final class AuditLogFlux extends Flux<AuditLogEntry> {


    private final GatewayDiscordClient gateway;
    private final long guildId;

    @Nullable
    private final Snowflake userId;
    @Nullable
    private final ActionType actionType;

    public AuditLogFlux(GatewayDiscordClient gateway, long guildId, @Nullable Snowflake userId,
                        @Nullable ActionType actionType) {
        this.gateway = gateway;
        this.guildId = guildId;
        this.userId = userId;
        this.actionType = actionType;
    }

    public AuditLogFlux(GatewayDiscordClient gateway, long guildId) {
        this(gateway, guildId, null, null);
    }

    public AuditLogFlux withResponsibleUser(final Snowflake userId) {
        return new AuditLogFlux(gateway, guildId, userId, actionType);
    }

    public AuditLogFlux withActionType(final ActionType actionType) {
        return new AuditLogFlux(gateway, guildId, userId, actionType);
    }

    @Override
    public void subscribe(CoreSubscriber<? super AuditLogEntry> actual) {
        final Function<Map<String, Object>, Flux<AuditLogData>> makeRequest = params -> {
            if (userId != null) {
                params.put("user_id", userId.asString());
            }
            if (actionType != null) {
                params.put("action_type", actionType.getValue());
            }

            return gateway.getRestClient().getAuditLogService()
                    .getAuditLog(guildId, params)
                    .flux();
        };

        final ToLongFunction<AuditLogData> getLastEntryId = response -> {
            final List<AuditLogEntryData> entries = response.auditLogEntries();
            return (entries.size() == 0) ? Long.MAX_VALUE :
                    Snowflake.asLong(entries.get(entries.size() - 1).id());
        };

        PaginationUtil.paginateBefore(makeRequest, getLastEntryId, Long.MAX_VALUE, 100)
                .flatMap(log -> Flux.fromIterable(log.auditLogEntries())
                        .map(data -> new AuditLogEntry(gateway, data)))
                .subscribe(actual);
    }
}
