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
import discord4j.core.event.domain.interaction.*;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.component.*;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionPresentModalSpec;
import discord4j.rest.interaction.GuildCommandRegistrar;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Example to showcase how {@link DeferrableInteractionEvent#deferReply()}, {@link DeferrableInteractionEvent#reply()},
 * {@link ComponentInteractionEvent#deferEdit()}, {@link ComponentInteractionEvent#edit()} and
 * {@link ComponentInteractionEvent#presentModal(String, String, Collection)} work.
 * <p>
 * Creates an /example command in a given guildId environment variable and two context menu commands (user and message).
 * The chat input command allows to configure how to react on its created button press, including presenting a modal.
 */
public class ExampleInteractions {

    private static final String token = System.getenv("token");
    private static final long guildId = Long.parseLong(System.getenv("guildId"));

    public static final String CHAT_INPUT_COMMAND_NAME = "example";
    public static final String REPLY_MODE_SELECT = "reply-mode";
    public static final String DEFER_REPLY = "deferReply";
    public static final String REPLY = "reply";
    public static final String DEFER_EDIT = "deferEdit";
    public static final String EDIT = "edit";
    public static final String MODAL = "modal";
    public static final String MODAL_CUSTOM_ID = "modal-step";
    public static final String ACTION_BUTTON = "action";
    public static final String USER_COMMAND_NAME = "Show user info";
    public static final String MESSAGE_COMMAND_NAME = "Approve";

    public static void main(String[] args) {
        DiscordClient.create(token)
                .withGateway(client -> {
                    ApplicationCommandRequest example = ApplicationCommandRequest.builder()
                            .name(CHAT_INPUT_COMMAND_NAME)
                            .description("Create a set of components showcasing ways of responding")
                            .build();
                    ApplicationCommandRequest userInfo = ApplicationCommandRequest.builder()
                            .name(USER_COMMAND_NAME)
                            .type(ApplicationCommand.Type.USER.getValue())
                            .build();
                    ApplicationCommandRequest approve = ApplicationCommandRequest.builder()
                            .name(MESSAGE_COMMAND_NAME)
                            .type(ApplicationCommand.Type.MESSAGE.getValue())
                            .build();
                    List<ApplicationCommandRequest> commands = Arrays.asList(example, userInfo, approve);

                    Map<Snowflake, String> modeByUser = new ConcurrentHashMap<>();

                    Publisher<?> onChatInput = client.on(ChatInputInteractionEvent.class, event -> {
                        if (CHAT_INPUT_COMMAND_NAME.equals(event.getCommandName())) {
                            return event.reply()
                                    .withContent("Choose how the button works and test it")
                                    .withComponents(getComponents(false));
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onUser = client.on(UserInteractionEvent.class, event -> {
                        if (USER_COMMAND_NAME.equals(event.getCommandName())) {
                            return event.getTargetUser()
                                    .flatMap(u -> u.asMember(event.getInteraction().getGuildId().orElseThrow(RuntimeException::new)))
                                    .flatMap(user -> event.reply()
                                            .withEmbeds(EmbedCreateSpec.create()
                                                    .withFields(
                                                            EmbedCreateFields.Field.of("Name", user.getUsername(),
                                                                    false),
                                                            EmbedCreateFields.Field.of("Display Name", user.getDisplayName(),
                                                                    false),
                                                            EmbedCreateFields.Field.of("Global Name", user.getGlobalName().orElse("none"),
                                                                    false),
                                                            EmbedCreateFields.Field.of("Avatar URL",
                                                                    user.getAvatarUrl(), false))
                                                    .withImage(user.getAvatarUrl())));
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onMessage = client.on(MessageInteractionEvent.class, event -> {
                        if (MESSAGE_COMMAND_NAME.equals(event.getCommandName())) {
                            return event.deferReply()
                                    .then(event.getTargetMessage())
                                    .flatMap(it -> it.addReaction(ReactionEmoji.unicode("✅")))
                                    .then(event.editReply("Done!"));
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onSelect = client.on(SelectMenuInteractionEvent.class, event -> {
                        if (REPLY_MODE_SELECT.equals(event.getCustomId())) {
                            Snowflake user = event.getUser().getId();
                            String selected = event.getValues().get(0);
                            String previous = modeByUser.put(user, selected);
                            if (previous == null) {
                                return event.reply("You have selected `" + selected + "`, now try the button!");
                            } else {
                                return event.reply("Updated your choice from `" + previous + "` to `" + selected + "`");
                            }
                        }
                        return Mono.empty();
                    });

                    // create a listener that handles the button click
                    Publisher<?> onButton = client.on(ButtonInteractionEvent.class, event -> {
                        if (ACTION_BUTTON.equals(event.getCustomId())) {
                            Snowflake user = event.getUser().getId();
                            String mode = modeByUser.getOrDefault(user, DEFER_REPLY);
                            switch (mode) {
                                case DEFER_REPLY:
                                    return event.deferReply()
                                            .then(Mono.delay(Duration.ofSeconds(2)))
                                            .then(event.editReply("`deferReply` shows a loading state and " +
                                                    "`editReply` gives content to a **new** message after two seconds"))
                                            .then();
                                case REPLY:
                                    return event.reply("`reply` immediately displays a **new** message");
                                case DEFER_EDIT:
                                    return event.deferEdit()
                                            .then(Mono.delay(Duration.ofSeconds(2)))
                                            .then(event.editReply("`deferEdit` does not show a loading state " +
                                                            "and `editReply` is used to disable the components from " +
                                                            "the original message after two seconds")
                                                    .withComponentsOrNull(getComponents(true)))
                                            .then();
                                case EDIT:
                                    return event.edit("`edit` immediately disables the components from the " +
                                                    "original message")
                                            .withComponents(getComponents(true));
                                case MODAL:
                                    return event.presentModal(InteractionPresentModalSpec.builder()
                                                    .title("Type deferReply, reply, deferEdit or edit")
                                                    .customId(MODAL_CUSTOM_ID)
                                                    .addComponent(getModalComponent())
                                                    .build());
                            }
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onModal = client.on(ModalSubmitInteractionEvent.class, event -> {
                        if (MODAL_CUSTOM_ID.equals(event.getCustomId())) {
                            for (TextInput input : event.getComponents(TextInput.class)) {
                                if (REPLY_MODE_SELECT.equals(input.getCustomId())) {
                                    switch (input.getValue().orElse("")) {
                                        case DEFER_REPLY:
                                            return event.deferReply()
                                                    .then(Mono.delay(Duration.ofSeconds(2)))
                                                    .then(event.editReply("`deferReply` shows a loading state and " +
                                                            "`editReply` gives content to a **new** message after two" +
                                                            " seconds"))
                                                    .then();
                                        case REPLY:
                                            return event.reply("`reply` immediately displays a **new** message");
                                        case DEFER_EDIT:
                                            return event.deferEdit()
                                                    .then(Mono.delay(Duration.ofSeconds(2)))
                                                    .then(event.editReply("`deferEdit` does not show a loading state " +
                                                                    "and `editReply` is used to disable the " +
                                                                    "components from " +
                                                                    "the original message after two seconds")
                                                            .withComponentsOrNull(getComponents(true)))
                                                    .then();
                                        case EDIT:
                                            return event.edit("`edit` immediately disables the components from the " +
                                                            "original message")
                                                    .withComponents(getComponents(true));
                                        case MODAL:
                                            // this option will fail with an UnsupportedOperationException
                                            // Modal submit interactions cannot present other modals
                                            return event.presentModal();
                                    }
                                }
                            }
                        }
                        return event.reply("No valid type input: must be one of: " +
                                "`deferReply`, `reply`, `deferEdit` or `edit`");
                    });

                    // register the command and then subscribe to multiple listeners, using Mono.when
                    return GuildCommandRegistrar.create(client.getRestClient(), commands)
                            .registerCommands(Snowflake.of(guildId))
                            .thenMany(Mono.when(onChatInput, onUser, onMessage, onSelect, onButton, onModal));
                })
                .block();
    }

    private static Iterable<LayoutComponent> getComponents(boolean disabled) {
        return Arrays.asList(ActionRow.of(SelectMenu.of(REPLY_MODE_SELECT,
                                SelectMenu.Option.ofDefault("Deferred reply", DEFER_REPLY),
                                SelectMenu.Option.of("Reply", REPLY),
                                SelectMenu.Option.of("Deferred edit", DEFER_EDIT),
                                SelectMenu.Option.of("Edit", EDIT),
                                SelectMenu.Option.of("Modal", MODAL))
                        .withMaxValues(1)
                        .disabled(disabled)),
                ActionRow.of(Button.primary(ACTION_BUTTON, "Click me!").disabled(disabled)));
    }

    private static LayoutComponent getModalComponent() {
        return ActionRow.of(TextInput.small(REPLY_MODE_SELECT, "What should happen next?", 4, 10).required());
    }
}
