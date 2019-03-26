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
package discord4j.core.event.domain.guild;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a user joins a guild.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-member-add">Guild Member Add</a>
 */
public class MemberJoinEvent extends GuildEvent {

    private final Member member;
    private final long guildId;

    public MemberJoinEvent(DiscordClient client, Member member, long guildId) {
        super(client);
        this.member = member;
        this.guildId = guildId;
    }

    /**
     * Gets the member that has joined the Guild in this event.
     * @return The Member that has joined
     */
    public Member getMember() {
        return member;
    }

    /**
     * Gets the Snowflake ID of the guild the member has joined in this event.
     * @return The ID of the guild.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Gets the Guild the member has joined in this event.
     * @return The guild the member has joined
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public String toString() {
        return "MemberJoinEvent{" +
                "member=" + member +
                ", guildId=" + guildId +
                '}';
    }
}
