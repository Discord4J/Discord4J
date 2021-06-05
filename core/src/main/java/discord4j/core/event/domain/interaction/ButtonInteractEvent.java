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
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.json.InteractionResponseData;
import discord4j.discordjson.json.WebhookMessageEditRequest;
import discord4j.gateway.ShardInfo;
import discord4j.rest.interaction.FollowupHandler;
import discord4j.rest.util.InteractionResponseType;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Experimental
public class ButtonInteractEvent extends InteractionCreateEvent {

    public ButtonInteractEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    public String getCustomId() {
        return getInteraction().getCommandInteraction() // yes, this is getCommandInteraction for buttons... thanks Discord
                .flatMap(ApplicationCommandInteraction::getCustomId)
                .orElseThrow(IllegalStateException::new); // should always be present for buttons
    }

    /* TODO: not sure what type to use here
    public Mono<FollowupHandler> edit(Consumer<? super ...> spec) {
        return Mono.defer(
                () -> {

                })
    }
    */

    @Override
    public Mono<FollowupHandler> deferResponse() {
        return respond(InteractionResponseType.DEFERRED_UPDATE_MESSAGE, null);
    }

    @Override
    public Mono<FollowupHandler> deferResponseEphemeral() {
        InteractionApplicationCommandCallbackData data = InteractionApplicationCommandCallbackData.builder()
                .flags(Message.Flag.EPHEMERAL.getFlag())
                .build();

        return respond(InteractionResponseType.DEFERRED_UPDATE_MESSAGE, data);
    }
}
