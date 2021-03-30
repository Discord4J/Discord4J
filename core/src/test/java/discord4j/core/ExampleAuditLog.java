package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.audit.AuditLogEntry;
import discord4j.core.object.audit.AuditLogPart;
import discord4j.core.object.audit.ChangeKey;
import discord4j.core.object.entity.Guild;

import java.util.List;

public class ExampleAuditLog {

    public static void main(String[] args) {
        List<AuditLogEntry> entries = DiscordClient.create(System.getenv("token"))
                .login()
                .flatMapMany(client -> client.on(GuildCreateEvent.class))
                .filter(gce -> gce.getGuild().getId().equals(Snowflake.of(System.getenv("guildId"))))
                .map(GuildCreateEvent::getGuild)
                .next()
                .flatMapMany(Guild::getAuditLog)
                .take(10)
                .reduce(AuditLogPart::combine)
                .map(AuditLogPart::getEntries)
                .block();

        System.out.println(entries);

        Snowflake snowflake = entries.get(0).getChange(ChangeKey.INVITE_CHANNEL_ID).get().getCurrentValue().get();
        System.out.println(snowflake.asLong());

    }
}
