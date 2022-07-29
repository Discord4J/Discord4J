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
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.Interaction;
import discord4j.gateway.ShardInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Dispatched when a user uses a chat input command (formerly "slash command").
 * <p>
 * You are required to respond to this interaction within a three-second window by using one of the following:
 * <ul>
 *     <li>{@link #reply()} to directly include a message</li>
 *     <li>{@link #deferReply()} to acknowledge without a message, typically to perform a background task and give the
 *     user a loading state until it is edited</li>
 *     <li>{@link #presentModal(String, String, Collection)} to pop a modal for the user to interact with</li>
 * </ul>
 * See {@link InteractionCreateEvent} for more details about valid operations.
 * <p>
 * This is not directly dispatched by Discord, but is a utility specialization of
 * {@link ApplicationCommandInteractionEvent}.
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
@Experimental
public class ChatInputInteractionEvent extends ApplicationCommandInteractionEvent {

    public ChatInputInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Convenience methods forwarding to ApplicationCommandInteraction methods.
    // We can assume these properties are present, because this is a chat input command interaction.

    /**
     * Gets the options of the invoked command.
     *
     * @return The options of the invoked command.
     */
    public /*~~>*/List<ApplicationCommandInteractionOption> getOptions() {
        return getInteraction().getCommandInteraction()
                .orElseThrow(IllegalStateException::new) // should always be present for chat input commands
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
                .orElseThrow(IllegalStateException::new) // should always be present for chat input commands
                .getOption(name);
    }
}
