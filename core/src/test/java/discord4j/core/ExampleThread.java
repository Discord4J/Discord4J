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
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.event.domain.thread.ThreadEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.object.entity.channel.TopLevelGuildMessageWithThreadsChannel;
import discord4j.core.spec.StartThreadFromMessageSpec;
import discord4j.core.spec.ThreadChannelEditSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.interaction.GuildCommandRegistrar;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static reactor.function.TupleUtils.function;

/**
 * Showcase some thread operations under a given test guild. Requires TOKEN and GUILD_ID environment variables.
 */
public class ExampleThread {

    private static final Logger log = Loggers.getLogger(ExampleThread.class);

    private static final String TOKEN = System.getenv("TOKEN");
    private static final long GUILD_ID = Long.parseLong(System.getenv("GUILD_ID"));

    private static final String START = "start";
    private static final String START_MESSAGE = "start-from-message";
    private static final String JOIN = "join";
    private static final String LEAVE = "leave";
    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String EDIT = "edit";

    private static final String TITLE_OPTION = "title";
    private static final String THREAD_OPTION = "thread";
    private static final String MEMBER_OPTION = "member";

    public static void main(String[] args) {
        DiscordClient.create(TOKEN)
                .withGateway(client -> {
                    List<ApplicationCommandRequest> commands = Arrays.asList(
                            ApplicationCommandRequest.builder()
                                    .name(START)
                                    .description("Start a thread in this channel")
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .type(ApplicationCommandOption.Type.STRING.getValue())
                                            .name(TITLE_OPTION)
                                            .description("the thread title")
                                            .build())
                                    .build(),
                            ApplicationCommandRequest.builder()
                                    .name(START_MESSAGE)
                                    .type(ApplicationCommand.Type.MESSAGE.getValue())
                                    .build(),
                            ApplicationCommandRequest.builder()
                                    .name(JOIN)
                                    .description("Join a thread")
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .type(ApplicationCommandOption.Type.CHANNEL.getValue())
                                            .name(THREAD_OPTION)
                                            .description("the thread to join")
                                            .required(true)
                                            .build())
                                    .build(),
                            ApplicationCommandRequest.builder()
                                    .name(LEAVE)
                                    .description("Leave a thread")
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .type(ApplicationCommandOption.Type.CHANNEL.getValue())
                                            .name(THREAD_OPTION)
                                            .description("the thread to leave")
                                            .required(true)
                                            .build())
                                    .build(),
                            ApplicationCommandRequest.builder()
                                    .name(ADD)
                                    .description("Add a member to a thread")
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .type(ApplicationCommandOption.Type.USER.getValue())
                                            .name(MEMBER_OPTION)
                                            .description("the member to add")
                                            .required(true)
                                            .build())
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .type(ApplicationCommandOption.Type.CHANNEL.getValue())
                                            .name(THREAD_OPTION)
                                            .description("the target thread")
                                            .required(true)
                                            .build())
                                    .build(),
                            ApplicationCommandRequest.builder()
                                    .name(REMOVE)
                                    .description("Remove a member from a thread")
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .type(ApplicationCommandOption.Type.USER.getValue())
                                            .name(MEMBER_OPTION)
                                            .description("the member to remove")
                                            .required(true)
                                            .build())
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .type(ApplicationCommandOption.Type.CHANNEL.getValue())
                                            .name(THREAD_OPTION)
                                            .description("the target thread")
                                            .required(true)
                                            .build())
                                    .build(),
                            ApplicationCommandRequest.builder()
                                    .name(EDIT)
                                    .description("Edit a thread")
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .type(ApplicationCommandOption.Type.STRING.getValue())
                                            .name(TITLE_OPTION)
                                            .description("the thread title")
                                            .required(true)
                                            .build())
                                    .addOption(ApplicationCommandOptionData.builder()
                                            .type(ApplicationCommandOption.Type.CHANNEL.getValue())
                                            .name(THREAD_OPTION)
                                            .description("the target thread")
                                            .required(true)
                                            .build())
                                    .build()
                    );
                    Mono<Void> createCommands = GuildCommandRegistrar.create(client.getRestClient(), commands)
                        .registerCommands(Snowflake.of(GUILD_ID))
                        .then();

                    Publisher<?> onChatInput = client.on(ChatInputInteractionEvent.class, event -> {
                        if (START.equals(event.getCommandName())) {
                            String title = event.getOption(TITLE_OPTION)
                                    .flatMap(ApplicationCommandInteractionOption::getValue)
                                    .map(ApplicationCommandInteractionOptionValue::asString)
                                    .orElse("Thread at " + LocalDateTime.now());

                            return event.deferReply()
                                    .withEphemeral(true)
                                    .then(event.getInteraction().getChannel())
                                    .ofType(TopLevelGuildMessageWithThreadsChannel.class) // TextChannel or NewsChannel
                                    .flatMap(ch -> ch.startPublicThreadWithoutMessage(title))
                                    .then(event.editReply("Done!"))
                                    .onErrorResume(ex -> event.editReply("Error: " + ex).then(Mono.error(ex)));

                        } else if (JOIN.equals(event.getCommandName())) {
                            Snowflake id = event.getOption(THREAD_OPTION)
                                    .flatMap(ApplicationCommandInteractionOption::getValue)
                                    .map(ApplicationCommandInteractionOptionValue::asSnowflake)
                                    .orElseThrow(RuntimeException::new);
                            Mono<ThreadChannel> thread = event.getClient().getChannelById(id).cast(ThreadChannel.class);

                            return event.deferReply()
                                    .withEphemeral(true)
                                    .then(thread)
                                    .flatMap(ThreadChannel::join)
                                    .then(event.editReply("Done!"))
                                    .onErrorResume(ex -> event.editReply("Error: " + ex).then(Mono.error(ex)));

                        } else if (LEAVE.equals(event.getCommandName())) {
                            Snowflake id = event.getOption(THREAD_OPTION)
                                    .flatMap(ApplicationCommandInteractionOption::getValue)
                                    .map(ApplicationCommandInteractionOptionValue::asSnowflake)
                                    .orElseThrow(RuntimeException::new);
                            Mono<ThreadChannel> thread = event.getClient().getChannelById(id).cast(ThreadChannel.class);

                            return event.deferReply()
                                    .withEphemeral(true)
                                    .then(thread)
                                    .flatMap(ThreadChannel::leave)
                                    .then(event.editReply("Done!"))
                                    .onErrorResume(ex -> event.editReply("Error: " + ex).then(Mono.error(ex)));

                        } else if (ADD.equals(event.getCommandName())) {
                            Mono<User> user = event.getOption(MEMBER_OPTION)
                                    .flatMap(ApplicationCommandInteractionOption::getValue)
                                    .map(ApplicationCommandInteractionOptionValue::asUser)
                                    .orElseThrow(RuntimeException::new);

                            Snowflake id = event.getOption(THREAD_OPTION)
                                    .flatMap(ApplicationCommandInteractionOption::getValue)
                                    .map(ApplicationCommandInteractionOptionValue::asSnowflake)
                                    .orElseThrow(RuntimeException::new);
                            Mono<ThreadChannel> thread = event.getClient().getChannelById(id).cast(ThreadChannel.class);

                            // we use zipWith to act on the two above mono instances in a reactive way
                            return event.deferReply()
                                    .withEphemeral(true)
                                    .then(thread)
                                    .zipWith(user)
                                    .flatMap(function(ThreadChannel::addMember))
                                    .then(event.editReply("Done!"))
                                    .onErrorResume(ex -> event.editReply("Error: " + ex).then(Mono.error(ex)));

                        } else if (REMOVE.equals(event.getCommandName())) {
                            Mono<User> user = event.getOption(MEMBER_OPTION)
                                    .flatMap(ApplicationCommandInteractionOption::getValue)
                                    .map(ApplicationCommandInteractionOptionValue::asUser)
                                    .orElseThrow(RuntimeException::new);

                            Snowflake id = event.getOption(THREAD_OPTION)
                                    .flatMap(ApplicationCommandInteractionOption::getValue)
                                    .map(ApplicationCommandInteractionOptionValue::asSnowflake)
                                    .orElseThrow(RuntimeException::new);
                            Mono<ThreadChannel> thread = event.getClient().getChannelById(id).cast(ThreadChannel.class);

                            // we use zipWith to act on the two above mono instances in a reactive way
                            return event.deferReply()
                                    .withEphemeral(true)
                                    .then(thread)
                                    .zipWith(user)
                                    .flatMap(function(ThreadChannel::removeMember))
                                    .then(event.editReply("Done!"))
                                    .onErrorResume(ex -> event.editReply("Error: " + ex).then(Mono.error(ex)));

                        } else if (EDIT.equals(event.getCommandName())) {
                            String title = event.getOption(TITLE_OPTION)
                                    .flatMap(ApplicationCommandInteractionOption::getValue)
                                    .map(ApplicationCommandInteractionOptionValue::asString)
                                    .orElse("Thread at " + LocalDateTime.now());

                            Snowflake id = event.getOption(THREAD_OPTION)
                                    .flatMap(ApplicationCommandInteractionOption::getValue)
                                    .map(ApplicationCommandInteractionOptionValue::asSnowflake)
                                    .orElseThrow(RuntimeException::new);
                            Mono<ThreadChannel> thread = event.getClient().getChannelById(id).cast(ThreadChannel.class);

                            return event.deferReply()
                                    .withEphemeral(true)
                                    .then(thread)
                                    .flatMap(ch -> ch.edit(ThreadChannelEditSpec.builder()
                                            .name(title)
                                            .build()))
                                    .then(event.editReply("Done!"))
                                    .onErrorResume(ex -> event.editReply("Error: " + ex).then(Mono.error(ex)));
                        }
                        return Mono.empty();
                    });

                    Publisher<?> onMessage = client.on(MessageInteractionEvent.class, event -> {
                        if (START_MESSAGE.equals(event.getCommandName())) {
                            return event.deferReply()
                                    .withEphemeral(true)
                                    .then(event.getTargetMessage())
                                    .flatMap(msg -> msg.startThread(StartThreadFromMessageSpec.builder()
                                            .name("Thread from " + msg.getId())
                                            .build()))
                                    .then(event.editReply("Done!"))
                                    .onErrorResume(ex -> event.editReply("Error: " + ex).then(Mono.error(ex)));
                        }
                        return Mono.empty();
                    });

                    // Listen to new thread events
                    Publisher<?> listenThreadEvents = client.on(ThreadEvent.class, event -> {
                        log.info("Thread event: {}", event);
                        return Mono.empty();
                    });

                    return createCommands.then(Mono.when(onChatInput, onMessage, listenThreadEvents));
                })
                .block();
    }
}
