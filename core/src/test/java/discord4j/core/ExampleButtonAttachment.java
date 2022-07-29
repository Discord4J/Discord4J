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

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.support.GuildCommandRegistrar;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

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
                    /*~~>*/List<ApplicationCommandRequest> commands = Collections.singletonList(example);

                    // a button row we'll use later
                    String editAttach = "edit-attach";
                    ActionRow row = ActionRow.of(Button.success(editAttach, "Edit with attachment"));

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
                        if (editAttach.equals(press.getCustomId())) {
                            Mono<?> edit = press.editReply()
                                    .withContentOrNull("Wow, a new attachment!")
                                    .withFiles(getFile())
                                    .withComponents(row);
                            return press.deferEdit().then(edit);
                        }
                        return Mono.empty();
                    });

                    // register the command and then subscribe to multiple listeners, using Mono.when
                    return GuildCommandRegistrar.create(client.getRestClient(), guildId, commands)
                            .registerCommands()
                            .thenMany(Mono.when(onChatInputInteraction, onButtonInteraction));
                })
                .block();
    }

    private static MessageCreateFields.File getFile() {
        InputStream stream = ExampleButtonAttachment.class.getClassLoader().getResourceAsStream("logback.xml");
        return MessageCreateFields.File.of("logback.xml", stream);
    }
}
