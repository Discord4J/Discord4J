package discord4j.core.event.dispatch;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.monetization.EntitlementCreateEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.EntitlementData;
import discord4j.discordjson.json.gateway.EntitlementCreate;
import discord4j.discordjson.json.gateway.InteractionCreate;
import discord4j.rest.RestClient;
import reactor.core.publisher.Mono;

public class MonetizationDispatchHandlers {

    static Mono<EntitlementCreateEvent> entitlementCreate(DispatchContext<EntitlementCreate, Void> context) {
        EntitlementData entitlementData = context.getDispatch().entitlement();

        return Mono.just(new EntitlementCreateEvent(
            context.getGateway(),
            context.getShardInfo(),
            entitlementData.id().asLong(),
            entitlementData.skuId().asLong(),
            entitlementData.subscriptionId().toOptional().map(Snowflake::asLong),

            );
    }

}
