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

package discord4j.core.object.command;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.ResolvedChannelData;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A Discord channel that was resolved in a command.
 *
 * @see
 * <a href="https://discord.com/developers/docs/interactions/slash-commands#interaction-object-application-command-interaction-data-resolved-structure">
 * Application Command Interaction Data Resolved Object
 * </a>
 */
@Experimental
public class ResolvedChannel implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ResolvedChannelData data;

    /**
     * Constructs a {@code ResolvedChannel} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ResolvedChannel(final GatewayDiscordClient gateway, final ResolvedChannelData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    public ResolvedChannelData getData() {
        return data;
    }

    /**
     * Gets the id of the channel.
     *
     * @return The id of the channel.
     */
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the name of the channel.
     *
     * @return The name of the channel.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the type of the channel.
     *
     * @return The type of the channel.
     */
    public Channel.Type getType() {
        return Channel.Type.of(data.type());
    }

    /**
     * Gets the computed permissions for the invoking user in the channel, including overwrites.
     *
     * @return The permissions of the channel.
     */
    public PermissionSet getEffectivePermissions() {
        return PermissionSet.of(data.permissions());
    }

    /**
     * Retrieves the full {@link Channel} instance corresponding to this resolved channel.
     *
     * @return a {@link Mono} where, upon successful completion, emits the full {@link Channel} instance corresponding
     * to this resolved channel. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Channel> asChannel() {
        return gateway.getChannelById(getId());
    }

    /**
     * Retrieves the full {@link Channel} instance corresponding to this resolved channel, using the given retrieval
     * strategy.
     *
     * @return a {@link Mono} where, upon successful completion, emits the full {@link Channel} instance corresponding
     * to this resolved channel. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Channel> asChannel(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(getId());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public String toString() {
        return "ResolvedChannel{" +
                "data=" + data +
                '}';
    }
}
