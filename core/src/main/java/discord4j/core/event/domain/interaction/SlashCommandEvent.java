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
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.Interaction;
import discord4j.gateway.ShardInfo;

import java.util.List;
import java.util.Optional;

/**
 * Dispatched when a user in a guild uses a Slash Command.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#interaction-create">Interaction Create</a>
 */
@Experimental
public class SlashCommandEvent extends InteractionCreateEvent {

    public SlashCommandEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Convenience methods forwarding to ApplicationCommandInteraction methods.
    // We can assume these properties are present, because this is a slash command interaction.

    /**
     * Gets the ID of the invoked command.
     *
     * @return The ID of the invoked command.
     */
    public Snowflake getCommandId() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getId)
                .orElseThrow(IllegalStateException::new); // should always be present for slash commands
    }

    /**
     * Gets the name of the invoked command.
     *
     * @return The name of the invoked command.
     */
    public String getCommandName() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getName)
                .orElseThrow(IllegalStateException::new); // should always be present for slash commands
    }

    /**
     * Gets the options of the invoked command.
     *
     * @return The options of the invoked command.
     */
    public List<ApplicationCommandInteractionOption> getOptions() {
        return getInteraction().getCommandInteraction()
                .orElseThrow(IllegalStateException::new) // should always be present for slash commands
                .getOptions();
    }

    /**
     * Gets the option corresponding to the provided name, if present.
     *
     * @param name The name of the option.
     * @return The option corresponding to the provided name, if present.
     */
    public Optional<ApplicationCommandInteractionOption> getOption(final String name) {
        return getInteraction().getCommandInteraction()
                .orElseThrow(IllegalStateException::new) // should always be present for slash commands
                .getOption(name);
    }
}
