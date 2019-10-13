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
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import discord4j.rest.entity.data.ApplicationInfoData;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.request.RouterOptions;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import reactor.blockhound.BlockHound;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.netty.http.client.HttpClient;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ExampleBot {

    private static final Logger log = Loggers.getLogger(ExampleBot.class);

    private static String token;

    @BeforeClass
    public static void initialize() {
        token = System.getenv("token");
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testCommandBot() {
        BlockHound.builder()
                .allowBlockingCallsInside("java.io.FileInputStream", "readBytes")
                .install();

        DiscordClient client = DiscordClient.builder(token)
                .setRouterOptions(RouterOptions.builder()
                        // globally suppress any not found (404) error
                        .onClientResponse(ResponseFunction.emptyIfNotFound())
                        // bad requests (400) while adding reactions will be suppressed
                        .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 400))
                        .requestParallelism(12)
                        .build())
                .build();

        // Get the bot owner ID to filter commands
        Mono<Long> ownerId = client.getApplicationInfo()
                .map(ApplicationInfoData::getOwnerId)
                .cache();

        // Create our event handlers
        List<EventHandler> eventHandlers = new ArrayList<>();
        eventHandlers.add(new AddRole());
        eventHandlers.add(new Echo());
        eventHandlers.add(new UserInfo());
        eventHandlers.add(new LogLevelChange());
        eventHandlers.add(new Reactor());
        eventHandlers.add(new ChangeAvatar());
        eventHandlers.add(new BurstMessages());

        // Build a safe event-processing pipeline
        GatewayDiscordClient gateway = client.login().block();
        assert gateway != null;

        Mono<Void> events = gateway.on(MessageCreateEvent.class, event -> ownerId
                .filter(owner -> {
                    Long author = event.getMessage().getAuthor()
                            .map(u -> u.getId().asLong())
                            .orElse(null);
                    return owner.equals(author);
                })
                .flatMap(id -> Mono.when(eventHandlers.stream()
                        .map(handler -> handler.onMessageCreate(event))
                        .collect(Collectors.toList()))
                ))
                .then();

        Mono.when(events, gateway.onDisconnect()).block();
    }

    public static class AddRole extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            return Mono.justOrEmpty(message.getContent())
                    .filterWhen(content -> message.getGuild().hasElement())
                    .filter(content -> content.startsWith("!addrole "))
                    .flatMap(content -> {
                        // !addrole userId roleName
                        String[] tokens = content.split(" ", 3);
                        if (tokens.length > 2) {
                            String user = tokens[1];
                            String roleName = tokens[2];
                            Mono<Member> member = message.getGuild()
                                    .flatMap(g -> g.getMemberById(Snowflake.of(user)));
                            Mono<Role> role = message.getGuild()
                                    .flatMapMany(Guild::getRoles)
                                    .filter(r -> r.getName().equalsIgnoreCase(roleName))
                                    .next();
                            return member.zipWith(role)
                                    .flatMap(t2 -> t2.getT1().addRole(t2.getT2().getId()));
                        }
                        return Mono.empty();
                    })
                    .then();
        }
    }

    public static class Echo extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            return Mono.justOrEmpty(message.getContent())
                    .filter(content -> content.startsWith("!echo "))
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
                                .flatMap(emoji -> message.addReaction(ReactionEmoji.unicode(emoji)))
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
                            .responseSingle((res, mono) -> mono.asByteArray())
                            .flatMap(input -> message.getClient()
                                    .edit(spec -> spec.setAvatar(Image.ofRaw(input, Image.Format.PNG))))
                            .then();
                }
            }
            return Mono.empty();
        }
    }

    public static class BurstMessages extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            if (!event.getMessage().getContent().orElse("").startsWith("!burstmessages")) {
                return Mono.empty();
            }
            return event.getGuild()
                    .flatMapMany(Guild::getChannels)
                    .ofType(TextChannel.class)
                    .filter(channel -> channel.getName().startsWith("test"))
                    .collectList()
                    .doOnNext(channelList -> Flux.fromIterable(channelList)
                            .flatMap(channel -> Flux.range(1, 5)
                                    .map(String::valueOf)
                                    .flatMap(channel::createMessage))
                            .collectList()
                            .elapsed()
                            .doOnNext(TupleUtils.consumer(
                                    (time, list) -> log.info("Sent {} messages in {} milliseconds ({} messages/s)",
                                            list.size(), time, (list.size() / (double) time) * 1000)))
                            .subscribe()) // Separate subscribe in order to improve accuracy of elapsed time
                    .then();
        }
    }

    public static abstract class EventHandler {

        public abstract Mono<Void> onMessageCreate(MessageCreateEvent event);
    }
}
