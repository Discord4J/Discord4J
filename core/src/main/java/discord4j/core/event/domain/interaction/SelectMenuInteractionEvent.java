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
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.SelectMenu;
import discord4j.gateway.ShardInfo;

import java.util.Collection;
import java.util.List;

/**
 * Dispatched when a user interacts with a {@link SelectMenu} the bot has sent.
 * <p>
 * You are required to respond to this interaction within a three-second window by using one of the following:
 * <ul>
 *     <li>{@link #reply()} to directly include a message</li>
 *     <li>{@link #deferReply()} to acknowledge without a message, typically to perform a background task and give the
 *     user a loading state until it is edited</li>
 *     <li>{@link #edit()} to modify the message the component is on</li>
 *     <li>{@link #deferEdit()} to acknowledge without a message, will not display a loading state and allows later
 *     modifications to the message the component is on</li>
 *     <li>{@link #presentModal(String, String, Collection)} to pop a modal for the user to interact with</li>
 * </ul>
 * See {@link InteractionCreateEvent} for more details about valid operations.
 * <p>
 * This is not directly dispatched by Discord, but is a utility specialization of {@link InteractionCreateEvent}.
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
@Experimental
public class SelectMenuInteractionEvent extends ComponentInteractionEvent {

    public SelectMenuInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /**
     * Get the values selected in the menu.
     *
     * @return The values selected in the menu.
     * @see SelectMenu.Option#getValue()
     */
    public /*~~>*/List<String> getValues() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getValues)
                .orElseThrow(IllegalStateException::new); // should always be present for select menus
    }
}
