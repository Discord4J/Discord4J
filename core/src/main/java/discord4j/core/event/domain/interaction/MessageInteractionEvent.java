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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.event.domain.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Message;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a user uses a message command (message context menu)
 * <p>
 *
 * This is not directly dispatched by Discord, but is a utility specialization of
 * {@link ApplicationCommandInteractionEvent}.
 *
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
@Experimental
public class MessageInteractionEvent extends ApplicationCommandInteractionEvent {

    public MessageInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Convenience methods forwarding to ApplicationCommandInteraction methods.
    // We can assume these properties are present, because this is a message command interaction.

    /**
     * Gets the ID of the targeted Message.
     *
     * @return The ID of the targeted Message.
     */
    public Snowflake getTargetId() {
        return getInteraction().getCommandInteraction()
            .flatMap(ApplicationCommandInteraction::getTargetId)
            .orElseThrow(IllegalStateException::new); // should always be present for context menu commands
    }

    /**
     * Requests to retrieve the targeted Message.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} targeted by the user.
     * If an error is received, it is emitted through the Mono.
     */
    public Mono<Message> getTargetMessage() {
        return getClient().getMessageById(getInteraction().getChannelId(), getTargetId());
    }
}
