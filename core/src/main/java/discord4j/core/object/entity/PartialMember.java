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
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** A partial Discord guild member. */
public class PartialMember extends User {

    private final PartialMemberData data;

    private final long guildId;

    public PartialMember(GatewayDiscordClient gateway, UserWithMemberData data, long guildId) {
        super(gateway, data);
        this.data = data.member().get();
        this.guildId = guildId;
    }

    /**
     * Gets the ID of the guild this user is associated to.
     *
     * @return The ID of the guild this user is associated to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the guild this user is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this user is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Requests to retrieve the guild this user is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this user is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    /**
     * Gets the data of the partial member.
     *
     * @return The data of partial member.
     */
    public PartialMemberData getMemberData() {
        return data;
    }

    /**
     * Gets the user's guild nickname (if one is set).
     *
     * @return The user's guild nickname (if one is set).
     */
    public Optional<String> getNickname() {
        return Possible.flatOpt(data.nick());
    }

    /**
     * Gets the user's guild roles' IDs.
     *
     * @return The user's guild roles' IDs.
     */
    public Set<Snowflake> getRoleIds() {
        return data.roles().stream()
            .map(Snowflake::of)
            .collect(Collectors.toSet());
    }

    /**
     * Gets when the user joined the guild.
     *
     * @return When the user joined the guild.
     */
    public Instant getJoinTime() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.joinedAt(), Instant::from);
    }

    /**
     * Gets when the user started boosting the server, if present.
     *
     * @return When the user started boosting the server, if present.
     */
    public Optional<Instant> getPremiumTime() {
        return Possible.flatOpt(data.premiumSince())
            .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets whether this user is deafened by the server.
     *
     * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
     */
    public boolean isDeaf() {
        return data.deaf();
    }

    /**
     * Gets whether this user is muted by the server.
     *
     * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
     */
    public boolean isMute() {
        return data.mute();
    }

    @Override
    public String toString() {
        return "Partial{" +
            "data=" + data +
            ", guildId=" + guildId +
            "} " + super.toString();
    }
}
