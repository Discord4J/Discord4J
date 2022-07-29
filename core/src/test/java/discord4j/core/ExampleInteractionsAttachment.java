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

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Attachment;
import discord4j.core.support.GuildCommandRegistrar;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Example to showcase how to work with attachment option type with chat input interactions.
 */
public class ExampleInteractionsAttachment {

    private static final String token = System.getenv("token");
    private static final long guildId = Long.parseLong(System.getenv("guildId"));

    private static final String CHAT_INPUT_COMMAND_NAME = "submit";

    public static void main(String[] args) {
        DiscordClient.create(token)
                .withGateway(client -> {
                    /*~~>*/List<ApplicationCommandRequest> commands = Collections.singletonList(
                            ApplicationCommandRequest.builder()
                                    .name(CHAT_INPUT_COMMAND_NAME)
                                    .description("Example command to submit an attachment")
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .name("attachment")
                                            .description("a file to be included in the report")
                                            .type(ApplicationCommandOption.Type.ATTACHMENT.getValue())
                                            .build())
                                    .build()
                    );

                    Publisher<?> onChatInput = client.on(ChatInputInteractionEvent.class, event -> {
                        if (CHAT_INPUT_COMMAND_NAME.equals(event.getCommandName())) {
                            String attachmentsInfo = event.getInteraction().getCommandInteraction()
                                    .flatMap(ApplicationCommandInteraction::getResolved)
                                    .map(resolved -> resolved.getAttachments()
                                            .values()
                                            .stream()
                                            .map(Attachment::toString)
                                            .collect(Collectors.joining("\n")))
                                    .orElse("Command run without attachments");

                            return event.reply()
                                    .withContent("Thanks for submitting your report:\n" + attachmentsInfo);
                        }
                        return Mono.empty();
                    });

                    // register the command and then subscribe to multiple listeners, using Mono.when
                    return GuildCommandRegistrar.create(client.getRestClient(), guildId, commands)
                            .registerCommands()
                            .thenMany(onChatInput);
                })
                .block();
    }
}
