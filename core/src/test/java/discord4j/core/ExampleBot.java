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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ResumeEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.request.RouterOptions;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ExampleBot {

    private static final Logger log = Loggers.getLogger(ExampleBot.class);

    private static String token;
    private static String testRole;

    @BeforeClass
    public static void initialize() {
        token = System.getenv("token");
        testRole = System.getenv("testRole");
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testCommandBot() {
        DiscordClient client = new DiscordClientBuilder(token)
                .setRouterOptions(RouterOptions.builder()
                        .onClientResponse(ResponseFunction.emptyWhenNotFound()) // globally turn any 404 into {}
                        .onClientResponse(ResponseFunction.retryOnceOnErrorStatus(500)) // wait 1 sec and retry any 500
                        .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 400))
                        .onClientResponse(ResponseFunction.retryWhen(RouteMatcher.route(Routes.MESSAGE_CREATE),
                                Retry.onlyIf(ClientException.isRetryContextStatusCode(500))
                                        .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofSeconds(10))))
                        .build())
                .build();

        // Get the bot owner ID to filter commands
        AtomicLong ownerId = new AtomicLong();
        Flux.first(client.getEventDispatcher().on(ReadyEvent.class),
                client.getEventDispatcher().on(ResumeEvent.class))
                .next()
                .flatMap(evt -> client.getApplicationInfo())
                .map(ApplicationInfo::getOwnerId)
                .map(Snowflake::asLong)
                .subscribe(ownerId::set);

        // Create our event handlers
        List<EventHandler> eventHandlers = new ArrayList<>();
        eventHandlers.add(new AddRole());
        eventHandlers.add(new Echo());
        eventHandlers.add(new UserInfo());
        eventHandlers.add(new LogLevelChange());
        eventHandlers.add(new BlockingEcho());
        eventHandlers.add(new Reactor());
        eventHandlers.add(new ChangeAvatar());

        // Build a safe event-processing pipeline
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getAuthor()
                        .map(User::getId)
                        .map(Snowflake::asLong)
                        .filter(id -> ownerId.get() == id)
                        .isPresent())
                .flatMap(event -> Mono.whenDelayError(eventHandlers.stream()
                        .map(handler -> handler.onMessageCreate(event))
                        .collect(Collectors.toList())))
                .onErrorContinue((t, o) -> log.error("Error while processing event", t))
                .subscribe();

        client.login().block();
    }

    public static class AddRole extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            return message.getContent()
                    .filter(content -> content.startsWith("!addrole"))
                    .map(Mono::just)
                    .orElseGet(Mono::empty)
                    .flatMap(content -> message.getAuthorAsMember())
                    .flatMap(member -> member.addRole(Snowflake.of(testRole), null));
            // if "testRole" is null, the bot will keep processing events despite throwing an error
        }
    }

    public static class Echo extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            return message.getContent()
                    .filter(content -> content.startsWith("!echo "))
                    .map(Mono::just)
                    .orElseGet(Mono::empty)
                    .map(content -> content.substring("!echo ".length()))
                    .flatMap(source -> message.getChannel()
                            .flatMap(channel -> channel.createMessage(source)))
                    .then();
        }
    }

    public static class UserInfo extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            if (message.getContent()
                    .filter(content -> content.startsWith("!user "))
                    .isPresent()) {
                return Mono.justOrEmpty(message.getContent())
                        .map(content -> content.split(" ", 2))
                        .flatMap(tokens -> message.getClient().getUserById(Snowflake.of(tokens[1])))
                        .flatMap(user -> message.getChannel()
                                .flatMap(channel -> channel.createMessage(msg ->
                                        msg.setEmbed(embed -> embed
                                                .addField("Name", user.getUsername(), false)
                                                .addField("Avatar URL", user.getAvatarUrl(), false)
                                                .setImage(user.getAvatarUrl()))
                                )))
                        .switchIfEmpty(Mono.just("Not found")
                                .flatMap(reason -> message.getChannel()
                                        .flatMap(channel -> channel.createMessage(reason)))
                        )
                        .then();
            }
            return Mono.empty();
        }
    }

    public static class LogLevelChange extends EventHandler {

        private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            if (message.getContent()
                    .filter(content -> content.startsWith("!log "))
                    .isPresent()) {
                return Mono.justOrEmpty(message.getContent())
                        .map(content -> content.split(" ", 3))
                        .doOnNext(tokens -> {
                            String level = tokens[1];
                            String name = tokens[2];
                            Level logLevel = Level.valueOf(level);
                            context.getLoggerList().stream()
                                    .filter(logger -> logger.getName().startsWith(name))
                                    .forEach(logger -> {
                                        log.info("Changing {} to {}", logger, logLevel);
                                        logger.setLevel(logLevel);
                                    });
                        })
                        .then();
            }
            return Mono.empty();
        }
    }

    public static class BlockingEcho extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            message.getContent()
                    .filter(content -> content.startsWith("!echos "))
                    .ifPresent(content -> {
                        String source = content.substring("!echos ".length());
                        MessageChannel channel = message.getChannel().block();
                        channel.createMessage(source).block();
                    });
            return Mono.empty();
        }
    }

    public static class Reactor extends EventHandler {

        private final Random random = new Random();
        private final List<String> emoji = new ArrayList<>(Arrays.asList(
                "😀", "😬", "😂", "😄", "😅", "😇", "☺", "😋", "😘", "😚", "😜", "🤑", "😎", "🤗", "😳", "🙄", "😤",
                "😱", "😨", "😰", "😥", "🤒", "😭", "💩", "👹", "💀", "👻", "👽", "🤖", "😺", "😹", "😻", "😼", "😽",
                "🙀", "😿", "😾", "🙌", "👏", "👋", "👍", "👊", "✊", "✌", "👌", "✋", "👐", "💪", "☝", "🙏", "👆",
                "🖐", "🤘", "🖖", "✍", "💅", "👄", "👅", "👂", "👁", "👀", "👶", "👦", "👧", "👨", "👩", "👱", "👴",
                "👵", "👲", "👳", "👷", "💂", "🕵", "🎅", "👼", "👸", "👰", "🚶", "🏃", "💃", "👯", "👫", "👬", "👭",
                "🙇", "💁", "🙅", "🙆", "🙋", "🙎", "🙍", "💇", "💆", "💑", "👨‍❤️‍👨", "💏", "👩‍❤️‍💋‍👩",
                "👨‍❤️‍💋‍👨", "👩‍👩‍👦", "👨‍👨‍👦", "👮", "👚", "👕", "👖", "👔", "👗", "👙", "👘", "💄", "💋",
                "🎩", "👟", "👞", "👢", "👡", "👠", "👣", "⛑", "🎓", "👑", "🎒", "👝", "👛", "👜", "💼", "🌂", "💍",
                "🕶", "👓", "🐯", "🦁", "🐮", "🐷", "🐽", "🐸", "🐙", "🐵", "🐦", "🐧", "🐔", "🐒", "🙉", "🙈", "🐣",
                "🐥", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🐢", "🦀", "🦂", "🕷", "🐜", "🐞", "🐌", "🐠", "🐟", "🐡",
                "🐬", "🐋", "🐊", "🐆", "🐘", "🐫", "🐪", "🐄", "🐂", "🐃", "🐏", "🐑", "🐀", "🐁", "🐓", "🦃", "🐉",
                "🐾", "🐿", "🐇", "🐈", "🐩", "🐕", "🐲", "🌵", "🎄", "🌲", "🌴", "🌱", "🌿", "🌾", "🍁", "🍂", "🍃",
                "🎋", "🎍", "🍀", "🌺", "🌻", "🌹", "🌷", "🌼", "🌸", "💐", "🍄", "🎃", "🐚", "🌎", "🌍", "🌏", "🌕",
                "🌖", "🌗", "🌘", "🌑", "🌒", "🌓", "🌔", "🌚", "🌝", "🌛", "🌜", "🌞", "⭐", "🌟", "💫", "✨", "🌥",
                "🌦", "🌧", "⛈", "⚡", "🔥", "❄", "🌨", "☔", "☂", "🌪", "💨", "☃", "⛄", "💧", "💦", "🌊", "🍏", "🍎",
                "🍐", "🍋", "🍌", "🍉", "🍇", "🌶", "🍅", "🍍", "🍑", "🍈", "🍓", "🌽", "🍠", "🍯", "🍞", "🍗", "🧀",
                "🍖", "🍤", "🌯", "🌮", "🍝", "🍕", "🌭", "🍟", "🍔", "🍳", "🍜", "🍲", "🍥", "🍣", "🍱", "🍛", "🍙",
                "🍚", "🎂", "🍰", "🍦", "🍨", "🍧", "🍡", "🍢", "🍘", "🍮", "🍬", "🍭", "🍫", "🍿", "🍩", "🍪", "🍺",
                "☕", "🍵", "🍶", "🍹", "🍻", "🍼", "🍴", "🍷", "🍽", "⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🏉", "🎱",
                "🎿", "🏏", "🏑", "🏓", "🏌", "⛳", "⛷", "🏂", "⛸", "🏹", "🎣", "🚣", "🏊🏼", "🏄", "🏆", "🕴", "🏇",
                "🚵", "🚴", "🏋", "⛹", "🛀", "🎽", "🏅", "🎖", "🎗", "🏵", "🎫", "🎟", "🎭", "🎺", "🎷", "🎹", "🎤",
                "🎪", "🎨", "🎸", "🎻", "🎬", "🎮", "👾", "🎯", "🎲", "🎰", "🎳", "🚗", "🚕", "🚙", "🚌", "🚎", "🏎",
                "🚓", "🚒", "🚐", "🚛", "🚜", "🏍", "🚲", "🚨", "🚃", "🚟", "🚠", "🚡", "🚖", "🚘", "🚍", "🚔", "🚋",
                "🚝", "🚄", "🚅", "🚈", "🚞", "🚂", "🚆", "🛬", "🛫", "✈", "🛩", "🚁", "🚉", "🚊", "🚇", "⛵", "🛥",
                "🚤", "⛴", "🚀", "🛳", "🛰", "💺", "🏁", "🚥", "🚦", "🚏", "⛽", "🚧", "⚓", "🎡", "🎢", "🎠", "🏗",
                "🌁", "🗼", "🏭", "⛲", "⛺", "🏕", "🗾", "🌋", "🗻", "🏔", "⛰", "🎑", "🏞", "🛣", "🛤", "🌅", "🌄",
                "🏜", "🏖", "🏝", "🎇", "🌠", "🌌", "🌉", "🌃", "🏙", "🌆", "🌇", "🎆", "🌈", "🏘", "🏰", "🏯", "🏠",
                "🗽", "🏟", "🏡", "🏚", "🏢", "🏬", "🏣", "🏤", "🏥", "🏦", "🕌", "🏛", "💒", "🏩", "🏫", "🏪", "🏨",
                "🕍", "🕋", "⛩", "🕹", "💽", "💾", "💿", "📼", "📷", "📹", "🎥", "☎", "⏱", "🎙", "📻", "📺", "📠",
                "📟", "⏲", "⏰", "🕰", "⏳", "📡", "🔋", "💴", "💵", "💸", "🛢", "🔦", "💡", "💶", "💷", "💰", "💳",
                "💎", "🔨", "💣", "🔫", "🔪", "☠", "🔮", "💈", "💊", "💉", "🔖", "🚿", "🔑", "🛋", "🚪", "🛎", "🖼",
                "🎁", "🎀", "🎏", "🎈", "🛍", "⛱", "🗺", "🎊", "🎉", "🎎", "🎐", "🎌", "🏮", "📮", "📫", "📯", "📊",
                "🗃", "📇", "📅", "📉", "📈", "📰", "📕", "📙", "📒", "✂", "🖇", "📖", "📚", "📌", "📍", "🚩", "❤",
                "💔", "❣", "💕", "💓", "💗", "💖", "💘", "💝", "💠", "🔔"));

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            return message.getContent()
                    .filter(content -> content.startsWith("!react"))
                    .map(content -> {
                        String rawCount = content.substring("!react".length());
                        int count = 1;
                        if (!rawCount.isEmpty()) {
                            try {
                                count = Math.max(1, Math.min(20, Integer.parseInt(rawCount.trim())));
                            } catch (NumberFormatException e) {
                                throw Exceptions.propagate(e);
                            }
                        }
                        return Flux.fromIterable(fetch(count))
                                .flatMap(emoji -> message.addReaction(ReactionEmoji.unicode(emoji))
                                        .onErrorContinue(ClientException.isStatusCode(400),
                                                (t, o) -> log.info("Dropping {} due to {}", t.toString()))
                                )
                                .then();
                    })
                    .orElseGet(Mono::empty);
        }

        private List<String> fetch(int count) {
            List<String> reactions = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int index = random.nextInt(emoji.size());
                reactions.add(emoji.get(index));
                emoji.remove(index);
            }
            return reactions;
        }
    }

    public static class ChangeAvatar extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            if (message.getContent()
                    .filter(content -> content.equals("!avatar"))
                    .isPresent()) {
                for (Attachment attachment : message.getAttachments()) {
                    // This code is very optimistic as it does not check for status codes or file types
                    return HttpClient.create()
                            .get()
                            .uri(attachment.getUrl())
                            .responseSingle((res, mono) -> mono.asInputStream())
                            .flatMap(input -> message.getClient()
                                    .edit(spec -> {
                                        try {
                                            spec.setAvatar(Image.ofRaw(IOUtils.toByteArray(input), Image.Format.PNG));
                                        } catch (IOException e) {
                                            throw Exceptions.propagate(e);
                                        }
                                    }))
                            .then();
                }
            }
            return Mono.empty();
        }
    }

    public static abstract class EventHandler {

        public abstract Mono<Void> onMessageCreate(MessageCreateEvent event);
    }
}
