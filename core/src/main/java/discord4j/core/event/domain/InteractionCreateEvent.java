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

package discord4j.core.event.domain;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.json.InteractionData;
import discord4j.discordjson.json.InteractionResponseData;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import discord4j.rest.interaction.InteractionOperations;
import discord4j.rest.interaction.InteractionResponse;
import discord4j.rest.util.InteractionResponseType;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class InteractionCreateEvent extends Event {

    private final InteractionData data;
    private final InteractionOperations operations;

    public InteractionCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, InteractionData data) {
        super(gateway, shardInfo);
        this.data = data;
        RestClient restClient = getClient().rest();
        this.operations = new InteractionOperations(restClient, data, restClient.getApplicationId());
    }

    public InteractionData getData() {
        return data;
    }

    public Interaction getInteraction() {
        return new Interaction(getClient(), data);
    }

    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    public Snowflake getChannelId() {
        return Snowflake.of(data.channelId().get());
    }

    // TODO: Mono<? extends ?> getChannel()

    public Optional<Snowflake> getGuildId() {
        return data.guildId().toOptional().map(Snowflake::of);
    }

    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    public Optional<Member> getMember() {
        return data.member().toOptional()
                .map(data -> new Member(getClient(), data, getGuildId().get().asLong()));
    }

    public ApplicationCommandInteraction getCommandInteraction() {
        return new ApplicationCommandInteraction(getClient(), data.data().get(),
                getGuildId().map(Snowflake::asLong).orElse(null));
    }

    public Snowflake getCommandId() {
        return Snowflake.of(operations.getCommandInteractionData().id());
    }

    public String getCommandName() {
        return operations.getCommandInteractionData().name();
    }

    private Mono<Void> createInteractionResponse(InteractionResponseData responseData) {
        long id = Snowflake.asLong(data.id());
        String token = data.token();

        return getClient().rest().getInteractionService()
                .createInteractionResponse(id, token, responseData)
                .then();
    }

    public Mono<Void> acknowledge() {
        return createInteractionResponse(InteractionResponseData.builder()
                .type(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                .data(InteractionApplicationCommandCallbackData.builder().build())
                .build());
    }

    public Mono<Void> reply(String content) {
        return reply(InteractionApplicationCommandCallbackData.builder().content(content).build());
    }

    public Mono<Void> reply(InteractionApplicationCommandCallbackData callbackData) {
        return createInteractionResponse(InteractionResponseData.builder()
                .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                .data(callbackData)
                .build());
    }

    public InteractionResponse getInteractionResponse() {
        return operations;
    }
}
