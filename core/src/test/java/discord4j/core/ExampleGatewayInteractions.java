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

package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.interaction.GatewayInteractions;
import discord4j.discordjson.json.ApplicationCommandInteractionData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.interaction.Interactions;
import discord4j.rest.util.ApplicationCommandOptionType;

import java.util.Collections;
import java.util.Random;

import static discord4j.rest.interaction.Interactions.createHandler;

public class ExampleGatewayInteractions {

    public static void main(String[] args) {
        GatewayDiscordClient client = DiscordClient.create(System.getenv("token"))
                .login()
                .block();

        ApplicationCommandRequest randomCommand = ApplicationCommandRequest.builder()
                .name("random")
                .description("Send a random number")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("digits")
                        .description("Number of digits (1-20)")
                        .type(ApplicationCommandOptionType.INTEGER.getValue())
                        .required(false)
                        .build())
                .build();

        ApplicationCommandRequest pingCommand = ApplicationCommandRequest.builder()
                .name("ping")
                .description("Get a pong!")
                .build();

        Random random = new Random();

        Interactions interactions = Interactions.create()
                .onGuildCommand(randomCommand, Snowflake.of(208023865127862272L),
                        interaction -> interaction.acknowledge()
                                .withFollowup(it -> it.createFollowupMessage(
                                        result(random, interaction.getCommandInteractionData()))))
                .onGlobalCommand(pingCommand,
                        createHandler()
                                .guild(interaction -> interaction.acknowledge(true)
                                        .withFollowup(it -> it.createFollowupMessage("Pong!")))
                                .direct(interaction -> interaction.reply("Direct Pong!", false))
                                .build());

        interactions.createCommands(client.getRestClient()).block();

        client.on(GatewayInteractions.create(interactions)).blockLast();
    }

    private static String result(Random random, ApplicationCommandInteractionData acid) {
        long digits = acid.options()
                .toOptional()
                .orElse(Collections.emptyList())
                .stream()
                .filter(option -> option.name().equals("digits"))
                .map(option -> option.value().toOptional()
                        .map(Long::parseLong).orElse(1L))
                .findFirst()
                .orElse(1L);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.max(1, Math.min(20, digits)); i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }
}
