package discord4j.core.event.domain;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildEvent;
import discord4j.core.object.audit.AuditLogEntry;
import discord4j.core.object.entity.Guild;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Dispatched when an Entry of an Audit Log is created in a guild.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#guild-audit-log-entry-create">Guild Audit Log Entry Create</a>
 */
public class AuditLogEntryCreateEvent extends GuildEvent {

    private final long guildId;
    private final AuditLogEntry auditLogEntry;

    public AuditLogEntryCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId, AuditLogEntry auditLogEntry) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.auditLogEntry = auditLogEntry;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event.
     *
     * @return The ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} whose the entry was created.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }


    /**
     * Get the {@link AuditLogEntry} related to this event.
     *
     * @return a {@link AuditLogEntry}.
     */
    public AuditLogEntry getAuditLogEntry() {
        return this.auditLogEntry;
    }

    @Override
    public String toString() {
        return "AuditLogEntryCreateEvent{" +
                "guildId=" + guildId +
                ", auditLogEntry=" + auditLogEntry +
                '}';
    }
}
