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
        /*~~>*/List<AuditLogEntry> entries = DiscordClient.create(System.getenv("token"))
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
