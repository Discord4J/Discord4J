package discord4j.core.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.rest.util.Image;
import org.slf4j.LoggerFactory;
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

public class ExtraBotSupport {

    private static final Logger log = Loggers.getLogger(ExtraBotSupport.class);

    private final GatewayDiscordClient client;

    public static ExtraBotSupport create(GatewayDiscordClient client) {
        return new ExtraBotSupport(client);
    }

    ExtraBotSupport(GatewayDiscordClient client) {
        this.client = client;
    }

    public Mono<Void> eventHandlers() {
        return commandHandler(client);
    }

    public static Mono<Void> commandHandler(GatewayDiscordClient client) {
        Mono<Long> ownerId = client.rest().getApplicationInfo()
                .map(ApplicationInfoData::owner)
                .map(user -> Snowflake.asLong(user.id()))
                .cache();

        List<EventHandler> eventHandlers = new ArrayList<>();
        eventHandlers.add(new AddRole());
        eventHandlers.add(new BurstMessages());
        eventHandlers.add(new ChangeAvatar());
        eventHandlers.add(new LogLevelChange());
        eventHandlers.add(new Reactor());
        eventHandlers.add(new UserInfo());
        eventHandlers.add(new ReactionRemove());

        return client.on(MessageCreateEvent.class,
                event -> ownerId.filter(
                        owner -> {
                            Long author = event.getMessage().getAuthor()
                                    .map(User::getId)
                                    .map(Snowflake::asLong)
                                    .orElse(null);
                            return owner.equals(author);
                        })
                        .flatMap(id -> Mono.when(eventHandlers.stream()
                                .map(handler -> handler.onMessageCreate(event))
                                .collect(Collectors.toList()))
                        ))
                .then();
    }

    public static class AddRole extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            return Mono.justOrEmpty(message.getContent())
                    .filter(content -> content.startsWith("!addrole "))
                    .filterWhen(content -> message.getGuild().hasElement())
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

    public static class UserInfo extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            if (message.getContent().startsWith("!user ")) {
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
            if (message.getContent().startsWith("!log ")) {
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
            if (message.getContent().startsWith("!react")) {
                String rawCount = message.getContent().substring("!react".length());
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
            }
            return Mono.empty();
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
            if (message.getContent().equals("!avatar")) {
                for (Attachment attachment : message.getAttachments()) {
                    // This code is very optimistic as it does not check for status codes or file types
                    return HttpClient.create()
                            .get()
                            .uri(attachment.getUrl())
                            .responseSingle((res, mono) -> mono.asByteArray())
                            .flatMap(input -> message.getClient()
                                    .edit().withAvatar(Image.ofRaw(input, Image.Format.PNG)))
                            .then();
                }
            }
            return Mono.empty();
        }
    }

    public static class BurstMessages extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            if (!event.getMessage().getContent().startsWith("!burstmessages")) {
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

    public static class ReactionRemove extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            if (message.getContent().startsWith("!rr ")) {
                String[] tokens = message.getContent().split(" ");
                return event.getClient().getMessageById(Snowflake.of(tokens[1]), Snowflake.of(tokens[2]))
                        .flatMap(m -> m.removeReactions(ReactionEmoji.unicode("✅")));
            }
            return Mono.empty();
        }
    }
}
