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
import discord4j.core.object.ApplicationTeamMember;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.ApplicationTeamData;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @see <a href="https://discord.com/developers/docs/topics/teams#data-models-team-object">Team Resource</a>
 */
public final class ApplicationTeam implements Entity {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationTeamData data;

    /**
     * Constructs a {@code ApplicationTeam} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationTeam(final GatewayDiscordClient gateway, final ApplicationTeamData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the members of the team.
     *
     * @return The members of the team.
     */
    public /*~~>*/List<ApplicationTeamMember> getMembers() {
        return data.members().stream()
                .map(data -> new ApplicationTeamMember(gateway, data))
                .collect(Collectors.toList());
    }

    /**
     * Gets the name of the team.
     *
     * @return The name of the team.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the user id of the current team owner.
     *
     * @return The user id of the current team owner.
     */
    public Snowflake getOwnerId() {
        return Snowflake.of(data.ownerUserId().asLong());
    }

    /**
     * Requests to retrieve the current team owner.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} associated with the current
     * team owner. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getOwner() {
        return gateway.getUserById(getOwnerId());
    }

    /**
     * Requests to retrieve the current team owner, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the user
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} associated with the current
     * team owner. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getOwner(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getUserById(getOwnerId());
    }

    @Override
    public String toString() {
        return "ApplicationTeam{" +
                "data=" + data +
                '}';
    }
}
