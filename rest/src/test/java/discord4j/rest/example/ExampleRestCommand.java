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

package discord4j.rest.example;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandInteractionData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.interaction.Interactions;
import reactor.netty.http.server.HttpServer;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Collections;
import java.util.Random;

public class ExampleRestCommand {

    private static final Logger log = Loggers.getLogger(ExampleRestCommand.class);

    public static void main(String[] args) {
        RestClient restClient = RestClient.create(System.getenv("token"));

        ApplicationCommandRequest randomCommand = ApplicationCommandRequest.builder()
                .name("random")
                .description("Send a random number")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("digits")
                        .description("Number of digits (1-20)")
                        //ApplicationCommandOption.Type.INTEGER.getValue()
                        .type(4)
                        .required(false)
                        .build())
                .build();

        Random random = new Random();

        Interactions interactions = Interactions.create()
                .onGuildCommand(randomCommand, Snowflake.of(208023865127862272L),
                        interaction -> interaction.acknowledge()
                                .withFollowup(it -> it.createFollowupMessage(
                                        result(random, interaction.getCommandInteractionData()))));

        // Create commands before starting server
        interactions.createCommands(restClient).block();

        // Start server
        HttpServer.create()
                .port(8889)
                .route(routes -> routes.post("/", interactions.buildReactorNettyHandler(restClient)))
                .bindUntilJavaShutdown(Duration.ofMillis(Long.MAX_VALUE), facade -> {
                    log.info("*************************************************************");
                    log.info("Server started at {}:{}", facade.host(), facade.port());
                    log.info("*************************************************************");
                });
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
