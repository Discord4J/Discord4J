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
package discord4j.core.event.domain.guild.member;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a user joins a guild.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-member-add">Guild Member Add</a>
 */
public class MemberJoinEvent extends AbstractMemberEvent {

    private final Member member;

    public MemberJoinEvent(DiscordClient client, Member member, long guildId) {
        super(client, guildId, member.getId().asLong());
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public String toString() {
        return "MemberJoinEvent{" +
                "member=" + member +
                '}';
    }
}
