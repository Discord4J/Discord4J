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
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateFields;
import discord4j.rest.interaction.GuildCommandRegistrar;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Example to showcase how to update an initial response through a button that also includes an attachment.
 */
public class ExampleButtonAttachment {

    private static final String token = System.getenv("token");
    private static final long guildId = Long.parseLong(System.getenv("guildId"));

    public static void main(String[] args) {
        DiscordClient.create(token)
                .withGateway(client -> {
                    String exampleName = "example";
                    ApplicationCommandRequest example = ApplicationCommandRequest.builder()
                            .name(exampleName)
                            .description("Create a button that adds an attachment to the initial response")
                            .build();
                    List<ApplicationCommandRequest> commands = Collections.singletonList(example);

                    // a button row we'll use later
                    String append = "append";
                    String replace = "replace";
                    String replaceFirst = "replace-first";
                    String clear = "clear";
                    ActionRow row = ActionRow.of(
                            Button.success(append, "Append attachment"),
                            Button.success(replace, "Replace all attachments"),
                            Button.success(replaceFirst, "Replace 1st attachment"),
                            Button.danger(clear, "Clear attachments"));

                    // a listener that captures our slash command interactions
                    // replies with content and a button
                    Publisher<?> onChatInputInteraction = client.on(ChatInputInteractionEvent.class, event -> {
                        if (exampleName.equals(event.getCommandName())) {
                            return event.reply()
                                    .withContent("Hey!")
                                    .withComponents(row);
                        }
                        return Mono.empty();
                    });

                    // a listener that captures button presses
                    // to update an interaction initial response with a new attachment
                    Publisher<?> onButtonInteraction = client.on(ButtonInteractionEvent.class, press -> {
                        if (append.equals(press.getCustomId())) {

                            Guild guild = press.getInteraction().getGuild().block();
                            System.out.println("Count Roles for " + guild.getName());
                            guild.getRoles().collectList().block().forEach(role -> {
                                System.out.println("Count for role " + role.getName() + " - " + role.getMemberCount().block());
                            });

                            Mono<Message> edit = press.editReply()
                                    .withContentOrNull("Added a new attachment")
                                    .withFiles(getFile())
                                    .withComponents(row);
                            return press.deferEdit().then(edit);

                        } else if (replace.equals(press.getCustomId())) {
                            Mono<Message> edit = press.editReply()
                                    .withContentOrNull("Replaced all attachments")
                                    .withFiles(getFile())
                                    .withComponents(row)
                                    .withAttachments();
                            return press.deferEdit().then(edit);

                        } else if (replaceFirst.equals(press.getCustomId())) {
                            Mono<Message> edit = press.getReply()
                                    .flatMap(reply -> press.editReply()
                                            .withContentOrNull("Replaced the first attachment")
                                            .withFiles(getFile())
                                            .withComponents(row)
                                            .withAttachmentsOrNull(reply.getAttachments()
                                                    .stream()
                                                    .skip(1)
                                                    .collect(Collectors.toList())));
                            return press.deferEdit().then(edit);

                        } else if (clear.equals(press.getCustomId())) {
                            Mono<Message> edit = press.editReply()
                                    .withContentOrNull("Removed all attachments")
                                    .withComponents(row)
                                    .withAttachments();
                            return press.deferEdit().then(edit);

                        }
                        return Mono.empty();
                    });

                    // register the command and then subscribe to multiple listeners, using Mono.when
                    return GuildCommandRegistrar.create(client.getRestClient(), commands)
                            .registerCommands(Snowflake.of(guildId))
                            .thenMany(Mono.when(onChatInputInteraction, onButtonInteraction));
                })
                .block();
    }

    private static MessageCreateFields.File getFile() {
        String content = "This is a text file created at " + LocalDateTime.now();
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return MessageCreateFields.File.of("content.txt", stream);
    }
}
