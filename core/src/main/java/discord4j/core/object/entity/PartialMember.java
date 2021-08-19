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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.PartialMemberData;
import discord4j.discordjson.json.UserWithMemberData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

/** A partial Discord guild member. */
public class PartialMember extends User {

    private final PartialMemberData memberData;

    private final long guildId;

    public PartialMember(GatewayDiscordClient gateway, UserWithMemberData data, long guildId) {
        super(gateway, data);
        this.memberData = data.member().get();
        this.guildId = guildId;
    }

    /**
     * Returns the ID of the guild this user is associated to, if present
     *
     * @return an optional {@link Snowflake}
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the guild this user is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this user is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * Requests to retrieve the guild this user is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this user is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getGuildId()).flatMap(id -> getClient()
            .withRetrievalStrategy(retrievalStrategy).getGuildById(id));
    }

    /**
     * Returns the member part, if present.
     *
     * @return an optional {@link MemberPart}
     */
    public Optional<MemberPart> getMember() {
        return Optional.ofNullable(memberData)
            .map(data -> new MemberPart(data, getId().asLong(),
                Objects.requireNonNull(guildId)));
    }

    @Override
    public String toString() {
        return "UserWithMember{" +
            "memberData=" + memberData +
            ", guildId=" + guildId +
            "} " + super.toString();
    }
}
