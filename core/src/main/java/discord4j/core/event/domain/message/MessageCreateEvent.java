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
package discord4j.core.event.domain.message;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Optional;

public class MessageCreateEvent extends MessageEvent {

    private final Message message;
    @Nullable
    private final Long guildId;
    @Nullable
    private final Member member;

    public MessageCreateEvent(DiscordClient client, Message message, @Nullable Long guildId, @Nullable Member member) {
        super(client);
        this.message = message;
        this.guildId = guildId;
        this.member = member;
    }

    public Message getMessage() {
        return message;
    }

    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    public Optional<Member> getMember() {
        return Optional.ofNullable(member);
    }
}
