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
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.component.TextInput;
import discord4j.core.spec.InteractionPresentModalSpec;
import discord4j.core.support.GuildCommandRegistrar;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public class ExampleModal {

    private static final String token = System.getenv("token");
    private static final long guildId = Long.parseLong(System.getenv("guildId"));

    static final String CHAT_INPUT_COMMAND_NAME = "example";
    static final String MODAL_CUSTOM_ID = "my-modal";
    static final String SELECT_CUSTOM_ID = "my-select";
    static final String INPUT_CUSTOM_ID = "my-input";

    public static void main(String[] args) {
        DiscordClient.create(token)
                .withGateway(client -> {
                    ApplicationCommandRequest example = ApplicationCommandRequest.builder()
                            .name(CHAT_INPUT_COMMAND_NAME)
                            .description("A command example")
                            .build();

                    /*~~>*/List<ApplicationCommandRequest> commands = Collections.singletonList(example);

                    Publisher<?> onChatInput = client.on(ChatInputInteractionEvent.class, event -> {
                        if (CHAT_INPUT_COMMAND_NAME.equals(event.getCommandName())) {
                            return event.presentModal(InteractionPresentModalSpec.builder()
                                    .title("Example modal")
                                    .customId(MODAL_CUSTOM_ID)
                                    .addComponent(ActionRow.of(SelectMenu.of(SELECT_CUSTOM_ID,
                                                    SelectMenu.Option.of("one", "1"),
                                                    SelectMenu.Option.of("two", "2"),
                                                    SelectMenu.Option.of("three", "3"))
                                            .withMinValues(0)))
                                            .addComponent(ActionRow.of(TextInput.small(
                                                    INPUT_CUSTOM_ID, "Something else to add?")
                                                    .required(false)))
                                    .build());
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onModal = client.on(ModalSubmitInteractionEvent.class, event -> {
                        if (MODAL_CUSTOM_ID.equals(event.getCustomId())) {
                            String comments = "";
                            for (TextInput component : event.getComponents(TextInput.class)) {
                                if (INPUT_CUSTOM_ID.equals(component.getCustomId())) {
                                    comments = component.getValue().orElse("");
                                }
                            }
                            for (SelectMenu component : event.getComponents(SelectMenu.class)) {
                                if (SELECT_CUSTOM_ID.equals(component.getCustomId())) {
                                    return event.reply("You selected: " +
                                                    component.getValues().orElse(emptyList()) +
                                                    (comments.isEmpty() ? "" : "\nwith a comment: " + comments))
                                            .withEphemeral(true);
                                }
                            }
                        }
                        return Mono.empty();
                    });

                    return GuildCommandRegistrar.create(client.getRestClient(), guildId, commands)
                            .registerCommands()
                            .thenMany(Mono.when(onChatInput, onModal));
                })
                .block();
    }
}
