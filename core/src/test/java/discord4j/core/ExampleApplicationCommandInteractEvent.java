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

import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.util.ApplicationCommandOptionType;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Random;

public class ExampleApplicationCommandInteractEvent {

    private static final Logger log = Loggers.getLogger(ExampleApplicationCommandInteractEvent.class);

    private static final String token = System.getenv("token");
    private static final String guildId = System.getenv("guildId");

    public static void main(String[] args) {
        GatewayDiscordClient client = DiscordClient.create(token)
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

        RestClient restClient = client.getRestClient();

        long applicationId = restClient.getApplicationId().block();

        restClient.getApplicationService()
                .createGuildApplicationCommand(applicationId, Long.parseLong(guildId), randomCommand)
                .doOnError(e -> log.warn("Unable to create guild command", e))
                .onErrorResume(e -> Mono.empty())
                .block();

        client.on(new ReactiveEventAdapter() {

            private final Random random = new Random();

            @Override
            public Publisher<?> onSlashCommand(SlashCommandEvent event) {
                if (event.getCommandName().equals("random")) {
                    String result = result(random, event.getInteraction().getCommandInteraction().get());
                    return event.reply(result);
                }
                return Mono.empty();
            }
        }).blockLast();
    }

    private static String result(Random random, ApplicationCommandInteraction acid) {
        long digits = acid.getOption("digits")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .orElse(1L);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.max(1, Math.min(20, digits)); i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }
}
