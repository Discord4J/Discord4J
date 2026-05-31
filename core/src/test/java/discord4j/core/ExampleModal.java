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
import discord4j.core.object.component.usage.ICanBeUsedInContainerComponent;
import discord4j.core.object.component.impl.FileUpload;
import discord4j.core.object.component.impl.Label;
import discord4j.core.object.component.impl.TextDisplay;
import discord4j.core.object.component.impl.TextInput;
import discord4j.core.object.component.impl.Thumbnail;
import discord4j.core.object.component.impl.item.UnfurledMediaItem;
import discord4j.core.object.component.impl.layout.Container;
import discord4j.core.object.component.impl.layout.Section;
import discord4j.core.object.component.impl.option.StringSelectOption;
import discord4j.core.object.component.impl.selectmenu.StringSelectMenu;
import discord4j.core.object.component.impl.selectmenu.UserSelectMenu;
import discord4j.core.object.entity.Attachment;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionPresentModalSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.interaction.GuildCommandRegistrar;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class ExampleModal {

    private static final String token = System.getenv("token");
    private static final long guildId = Long.parseLong(System.getenv("guildId"));

    static final String CHAT_INPUT_COMMAND_NAME = "example";
    static final String MODAL_CUSTOM_ID = "my-modal";
    static final String PARAGRAPHINPUT_CUSTOM_ID = "my-paragraph-input";
    static final String INPUT_CUSTOM_ID = "my-input";
    static final String SELECT_STRING_CUSTOM_ID = "my-select-string";
    static final String SELECT_USER_CUSTOM_ID = "my-select-user";
    static final String FILE_UPLOAD_CUSTOM_ID = "my-file-upload";

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
                                            Label.of("A title?", "Add a title for this",
                                                    TextInput.small(INPUT_CUSTOM_ID).withRequired(false)),
                                            Label.of("Tell us something...",
                                                    TextInput.paragraph(PARAGRAPHINPUT_CUSTOM_ID, 250,
                                                            928).withPlaceholder("...in more than 250 characters but " +
                                                            "less " +
                                                            "than 928").withRequired(false)),
                                            Label.of("Type", "What type the text is",
                                                    StringSelectMenu.of(SELECT_STRING_CUSTOM_ID)
                                                            .withOptions(
                                                                    StringSelectOption.ofDefault("Other", "other"),
                                                                    StringSelectOption.of("Novel", "Novel"),
                                                                    StringSelectOption.of("Fable", "Fable"),
                                                                    StringSelectOption.of("Poetry", "Poetry")
                                                            )),
                                            Label.of("Attach the author", UserSelectMenu.of(SELECT_USER_CUSTOM_ID)),
                                            Label.of("Add a thumbnail",
                                                    FileUpload.of(FILE_UPLOAD_CUSTOM_ID).required(true))
                                    ))
                                    .build());
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onModal = client.on(ModalSubmitInteractionEvent.class, event -> {
                        if (MODAL_CUSTOM_ID.equals(event.getCustomId())) {
                            String title = "";
                            String comments = "";
                            String type = "";
                            String attachedUser = "";
                            Attachment attachment = null;

                            List<TextInput> textInputComponents = event.getComponents(TextInput.class);
                            List<StringSelectMenu> stringSelectComponents = event.getComponents(StringSelectMenu.class);
                            List<UserSelectMenu> userSelectComponents = event.getComponents(UserSelectMenu.class);
                            List<FileUpload> fileUploadComponents = event.getComponents(FileUpload.class);

                            if (textInputComponents.isEmpty() && stringSelectComponents.isEmpty() && userSelectComponents.isEmpty() && fileUploadComponents.isEmpty()) {
                                return event.reply("No components found!");
                            }

                            for (TextInput component : textInputComponents) {
                                if (INPUT_CUSTOM_ID.equals(component.getCustomId())) {
                                    title = component.getValue().orElse("untiteled");
                                } else if (PARAGRAPHINPUT_CUSTOM_ID.equals(component.getCustomId())) {
                                    comments = component.getValue().orElse("");
                                }
                            }

                            for (StringSelectMenu component : stringSelectComponents) {
                                if (SELECT_STRING_CUSTOM_ID.equals(component.getCustomId())) {
                                    type = Optional.of(component.getValues()).map(values -> {
                                        StringJoiner stringJoiner = new StringJoiner(", ");
                                        values.forEach(stringJoiner::add);
                                        return stringJoiner.toString();
                                    }).orElse("other?");
                                }
                            }

                            for (UserSelectMenu component : userSelectComponents) {
                                if (SELECT_USER_CUSTOM_ID.equals(component.getCustomId())) {
                                    attachedUser = Optional.of(component.getValues()).map(values -> values.get(0)).orElse("none");
                                }
                            }

                            for (FileUpload component : fileUploadComponents) {
                                if (FILE_UPLOAD_CUSTOM_ID.equals(component.getCustomId())) {
                                    Snowflake attachmentId =
                                            component.getValues().orElse(Collections.emptyList()).get(0);
                                    attachment =
                                            event.getResolved().orElseThrow(RuntimeException::new).getAttachments().get(attachmentId);
                                }
                            }

                            List<ICanBeUsedInContainerComponent> firstComponents = new ArrayList<>();
                            firstComponents.add(TextDisplay.of(String.format("<@%s> Wrote a `%s` named %s",
                                    attachedUser, type, title)));
                            if (attachment != null) {
                                firstComponents.add(Section.of(Thumbnail.of(UnfurledMediaItem.of(attachment.getUrl())), TextDisplay.of("The Thumbnail")));
                            } else {
                                firstComponents.add(Section.of(Thumbnail.of(UnfurledMediaItem.of("https://upload" +
                                        ".wikimedia.org/wikipedia/commons/thumb/5/59/Minecraft_missing_texture_block" +
                                        ".svg/32px-Minecraft_missing_texture_block.svg.png")), TextDisplay.of("The " +
                                        "Thumbnail")));
                            }
                            firstComponents.add(TextDisplay.of(String.format("Comments: %s", comments)));


                            Container container = Container.of(firstComponents);

                            return event.reply(InteractionApplicationCommandCallbackSpec.builder().addComponent(container).build());
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
