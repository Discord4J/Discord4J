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
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.support.GuildCommandRegistrar;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Example to showcase how {@link InteractionCreateEvent#deferReply()}, {@link InteractionCreateEvent#reply()},
 * {@link ComponentInteractionEvent#deferEdit()} and {@link ComponentInteractionEvent#edit()} work for chat input, user,
 * message commands and component interactions.
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
                                    .flatMap(user -> event.reply()
                                            .withEmbeds(EmbedCreateSpec.create()
                                                    .withFields(
                                                            EmbedCreateFields.Field.of("Name", user.getUsername(), false),
                                                            EmbedCreateFields.Field.of("Avatar URL", user.getAvatarUrl(), false))
                                                    .withImage(user.getAvatarUrl())));
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onMessage = client.on(MessageInteractionEvent.class, event -> {
                        if (MESSAGE_COMMAND_NAME.equals(event.getCommandName())) {
                            return event.deferReply()
                                    .then(event.getTargetMessage())
                                    .flatMap(it -> it.addReaction(ReactionEmoji.unicode("✅")))
                                    .then(event.reply("Done!"));
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onSelect = client.on(SelectMenuInteractionEvent.class, event -> {
                        if (REPLY_MODE_SELECT.equals(event.getCustomId())) {
                            Snowflake user = event.getInteraction().getUser().getId();
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
                            Snowflake user = event.getInteraction().getUser().getId();
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
                            }
                        }
                        return Mono.empty();
                    });

                    // register the command and then subscribe to multiple listeners, using Mono.when
                    return GuildCommandRegistrar.create(client.getRestClient(), guildId, commands)
                            .registerCommands()
                            .thenMany(Mono.when(onChatInput, onUser, onMessage, onSelect, onButton));
                })
                .block();
    }

    private static Iterable<LayoutComponent> getComponents(boolean disabled) {
        return Arrays.asList(ActionRow.of(SelectMenu.of(REPLY_MODE_SELECT,
                                SelectMenu.Option.ofDefault("Deferred reply", DEFER_REPLY),
                                SelectMenu.Option.of("Reply", REPLY),
                                SelectMenu.Option.of("Deferred edit", DEFER_EDIT),
                                SelectMenu.Option.of("Edit", EDIT))
                        .withMaxValues(1)
                        .disabled(disabled)),
                ActionRow.of(Button.primary(ACTION_BUTTON, "Click me!").disabled(disabled)));
    }
}
