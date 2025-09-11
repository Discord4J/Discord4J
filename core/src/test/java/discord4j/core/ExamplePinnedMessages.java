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
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.PinnedMessageReference;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.poll.Poll;
import discord4j.core.object.entity.poll.PollAnswer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ExamplePinnedMessages {

    private static final String token = System.getenv("token");
    private static final String guildId = System.getenv("guildId");
    private static final String channelId = System.getenv("channelId");

    public static void main(String[] args) {
        DiscordClient.create(token)
            .withGateway(gw -> {
                Flux<PinnedMessageReference> pinnedMessageReferences = gw.on(GuildCreateEvent.class)
                    .filter(e -> e.getGuild().getId().asString().equals(guildId))
                    .next()
                    .flatMap(e -> e.getGuild().getChannelById(Snowflake.of(channelId)))
                    .ofType(TextChannel.class)
                    .flatMapMany(MessageChannel::getPinnedMessages)
                    .map(pinnedMessageReference -> {
                        System.out.println(pinnedMessageReference.getData());
                        return pinnedMessageReference;
                    });

                return pinnedMessageReferences.then();
            })
            .block();
    }
}
