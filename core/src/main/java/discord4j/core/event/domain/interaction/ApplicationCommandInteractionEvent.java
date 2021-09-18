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

package discord4j.core.event.domain.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommand.ApplicationCommandType;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.gateway.ShardInfo;

/**
 * Dispatched when a user uses an Application Command.
 * <p>
 * This is not directly dispatched by Discord, but is a utility specialization of {@link InteractionCreateEvent}.
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
@Experimental
public class ApplicationCommandInteractionEvent extends InteractionCreateEvent {

    public ApplicationCommandInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo,
                                              Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Convenience methods forwarding to ApplicationCommandInteraction methods.
    // We can assume these properties are present, because this is an application command interaction.

    /**
     * Gets the ID of the invoked command.
     *
     * @return The ID of the invoked command.
     */
    public Snowflake getCommandId() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getId)
                .orElseThrow(IllegalStateException::new); // should always be present for application commands
    }

    /**
     * Gets the name of the invoked command.
     *
     * @return The name of the invoked command.
     */
    public String getCommandName() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getName)
                .orElseThrow(IllegalStateException::new); // should always be present for application commands
    }

    /**
     * Gets the type of the invoked command.
     *
     * @return The type of the invoked command.
     */
    public ApplicationCommandType getCommandType() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getApplicationCommandType)
                .orElseThrow(IllegalStateException::new); // should always be present for application commands
    }
}
