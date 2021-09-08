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

import java.util.List;

/**
 * Dispatched when a user interacts with a {@link SelectMenu} the bot has sent.
 * <p>
 * This is not directly dispatched by Discord, but is a utility specialization of {@link InteractionCreateEvent}.
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
    public List<String> getValues() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getValues)
                .orElseThrow(IllegalStateException::new); // should always be present for select menus
    }
}
