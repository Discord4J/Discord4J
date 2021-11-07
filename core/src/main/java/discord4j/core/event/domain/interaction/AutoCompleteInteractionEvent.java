/*
 *  This file is part of Discord4J.
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
import discord4j.core.object.command.Interaction;
import discord4j.gateway.ShardInfo;

/**
 * This event is a placeholder for future functionality from discord.
 * Please use {@link ChatInputAutoCompleteEvent} instead.
 * <p>
 * Dispatched when a user starts an auto complete interaction
 * <p>
 * You should use one of the following interaction-specific events to access interaction-specific methods:
 * <ul>
 *     <li>{@link ChatInputAutoCompleteEvent} dispatched when a user starts chat command auto complete</li>
 * </ul>
 * <p>
 * This is not directly dispatched by Discord, but is a utility specialization of {@link InteractionCreateEvent}.
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
@Experimental
public class AutoCompleteInteractionEvent extends InteractionCreateEvent {

    public AutoCompleteInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }
}
