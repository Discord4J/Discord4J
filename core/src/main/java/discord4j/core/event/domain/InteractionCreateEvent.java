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
import discord4j.discordjson.json.*;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import discord4j.rest.interaction.InteractionOperations;
import discord4j.rest.interaction.InteractionResponse;
import discord4j.rest.util.InteractionResponseType;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class InteractionCreateEvent extends Event {

    private static final Logger log = Loggers.getLogger(InteractionCreateEvent.class);

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

    public Snowflake getId() {
        return operations.getId();
    }

    public Snowflake getGuildId() {
        return operations.getGuildId();
    }

    public Snowflake getChannelId() {
        return operations.getChannelId();
    }

    public MemberData getMemberData() {
        return operations.getMemberData();
    }

    public ApplicationCommandInteractionData getCommandInteractionData() {
        return operations.getCommandInteractionData();
    }

    public Snowflake getCommandId() {
        return Snowflake.of(getCommandInteractionData().id());
    }

    public String getCommandName() {
        return getCommandInteractionData().name();
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
                .type(InteractionResponseType.ACKNOWLEDGE.getValue())
                .data(InteractionApplicationCommandCallbackData.builder().build())
                .build());
    }

    public Mono<Void> acknowledge(boolean withSource) {
        return createInteractionResponse(InteractionResponseData.builder()
                .type(withSource ? InteractionResponseType.ACKNOWLEDGE_WITH_SOURCE.getValue() :
                        InteractionResponseType.ACKNOWLEDGE.getValue())
                .data(InteractionApplicationCommandCallbackData.builder().build())
                .build());

    }

    public Mono<Void> reply(String content, boolean withSource) {
        return reply(InteractionApplicationCommandCallbackData.builder().content(content).build(), withSource);
    }

    public Mono<Void> reply(InteractionApplicationCommandCallbackData callbackData, boolean withSource) {
        return createInteractionResponse(InteractionResponseData.builder()
                .type(withSource ? InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getValue() :
                        InteractionResponseType.CHANNEL_MESSAGE.getValue())
                .data(callbackData)
                .build());
    }

    public InteractionResponse getInteractionResponse() {
        return operations;
    }
}
