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
package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.MemberData;
import reactor.core.publisher.Mono;

/**
 * A Discord guild member.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#guild-member-object">Guild Member Object</a>
 */
public final class Member extends PartialMember {

    /**
     * Constructs a {@code Member} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this user is associated to.
     */
    public Member(final GatewayDiscordClient gateway, final MemberData data, final long guildId) {
        super(gateway, data.user(), data, guildId);
    }

    @Override
    public Mono<Member> asMember(final Snowflake guildId) {
        return Mono.just(this)
                .filter(member -> member.getGuildId().equals(guildId))
                .switchIfEmpty(super.asMember(guildId));
    }

    /**
     * Gets the data of the member.
     *
     * @return The data of the member.
     */
    @Override
    public MemberData getMemberData() {
        return (MemberData) super.getMemberData();
    }

    @Override
    public Mono<Member> asFullMember() {
        return Mono.just(this);
    }

    /**
     * Gets whether the user has not yet passed the guild's Membership Screening requirements.
     *
     * @return Whether the user has not yet passed the guild's Membership Screening requirements.
     */
    public boolean isPending() {
        return getMemberData().pending().toOptional().orElse(false);
    }

    @Override
    public String toString() {
        return "Member{} " + super.toString();
    }
}
