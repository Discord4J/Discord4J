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
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.object.component.Label;
import discord4j.core.object.component.TextInput;
import discord4j.core.spec.InteractionPresentModalSpec;
import discord4j.rest.interaction.GuildCommandRegistrar;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExampleModal {

    private static final String token = System.getenv("token");
    private static final long guildId = Long.parseLong(System.getenv("guildId"));

    static final String CHAT_INPUT_COMMAND_NAME = "example";
    static final String MODAL_CUSTOM_ID = "my-modal";
    static final String PARAGRAPHINPUT_CUSTOM_ID = "my-paragraph-input";
    static final String INPUT_CUSTOM_ID = "my-input";

    public static void main(String[] args) {
        DiscordClient.create(token)
                .withGateway(client -> {
                    ApplicationCommandRequest example = ApplicationCommandRequest.builder()
                            .name(CHAT_INPUT_COMMAND_NAME)
                            .description("A command example")
                            .build();

                    List<ApplicationCommandRequest> commands = Collections.singletonList(example);

                    Publisher<?> onChatInput = client.on(ChatInputInteractionEvent.class, event -> {
                        if (CHAT_INPUT_COMMAND_NAME.equals(event.getCommandName())) {
                            return event.presentModal(InteractionPresentModalSpec.builder()
                                    .title("Example modal")
                                    .customId(MODAL_CUSTOM_ID)
                                    .addAllComponents(Arrays.asList(
                                            Label.of("A title?", "A Desc...", TextInput.small(INPUT_CUSTOM_ID).required(false)),
                                            Label.of("Tell us something...", TextInput.paragraph(PARAGRAPHINPUT_CUSTOM_ID, 250, 928).placeholder("...in more than 250 characters but less than 928").required(false))
                                    ))
                                    .build());
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onModal = client.on(ModalSubmitInteractionEvent.class, event -> {
                        if (MODAL_CUSTOM_ID.equals(event.getCustomId())) {
                            String story = "";
                            String comments = "";

                            List<TextInput> components = event.getComponents(TextInput.class);

                            if (components.isEmpty()) {
                                return event.reply("No components found!");
                            }

                            for (TextInput component : components) {
                                if (PARAGRAPHINPUT_CUSTOM_ID.equals(component.getCustomId())) {
                                    story = component.getValue().orElse("untiteled");
                                } else if (INPUT_CUSTOM_ID.equals(component.getCustomId())) {
                                    comments = component.getValue().orElse("");
                                }
                            }

                            return event.reply("You wrote: " + story + "\n\nComments: " + comments);
                        }
                        return Mono.empty();
                    });

                    return GuildCommandRegistrar.create(client.getRestClient(), commands)
                            .registerCommands(Snowflake.of(guildId))
                            .thenMany(Mono.when(onChatInput, onModal));
                })
                .block();
    }
}
