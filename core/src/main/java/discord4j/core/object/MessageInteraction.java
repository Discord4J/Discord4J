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

package discord4j.core.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.MessageInteractionData;

import java.util.Objects;

/**
 * A Discord Message Interaction.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#messageinteraction">
 * Message Interaction Object</a>
 */
public class MessageInteraction implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final MessageInteractionData data;

    /**
     * Constructs a {@code MessageInteraction} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public MessageInteraction(final GatewayDiscordClient gateway, final MessageInteractionData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets the id of the interaction.
     *
     * @return The id of the interaction.
     */
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the type of interaction.
     *
     * @return The type of interaction
     */
    public ApplicationCommandOption.Type getType() {
        return ApplicationCommandOption.Type.of(data.type());
    }

    /**
     * Gets the name of the {@link discord4j.core.object.command.ApplicationCommand}.
     *
     * @return The name of the {@link discord4j.core.object.command.ApplicationCommand}.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the user who invoked the interaction.
     *
     * @return The user who invoked the interaction.
     */
    public User getUser() {
        return new User(gateway, data.user());
    }

    @Override
    public final GatewayDiscordClient getClient() {
        return gateway;
    }

}
