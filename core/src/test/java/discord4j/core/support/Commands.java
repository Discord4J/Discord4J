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

package discord4j.core.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import discord4j.common.ReactorResources;
import discord4j.common.util.Snowflake;
import discord4j.core.command.CommandContext;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.util.MentionUtil;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.util.Image;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Commands {

    private static final Logger log = Loggers.getLogger(Commands.class);

    public static Mono<Boolean> isAuthor(Mono<Long> authorId, CommandContext context) {
        return authorId.filter(id -> context.getAuthor()
                .map(User::getId)
                .map(Snowflake::asLong)
                .map(eventAuthor -> eventAuthor.equals(id))
                .orElse(false))
                .hasElement();
    }

    public static Mono<Void> echo(CommandContext context) {
        Message message = context.getMessage();
        return message.getRestChannel().createMessage(
                MessageCreateRequest.builder()
                        .content(MentionUtil.forUser(Snowflake.of(message.getUserData().id())) + " " + context.parameters())
                        .build())
                .flatMap(data -> context.getClient().rest().restMessage(data)
                        .createReaction("✅"))
                .then();
    }

    public static Mono<Void> status(CommandContext context) {
        String params = context.parameters();
        if (params.equalsIgnoreCase("online")) {
            return context.getClient().updatePresence(ClientPresence.online());
        } else if (params.equalsIgnoreCase("dnd")) {
            return context.getClient().updatePresence(ClientPresence.doNotDisturb());
        } else if (params.equalsIgnoreCase("idle")) {
            return context.getClient().updatePresence(ClientPresence.idle());
        } else if (params.equalsIgnoreCase("invisible")) {
            return context.getClient().updatePresence(ClientPresence.invisible());
        } else {
            // showing you can block too
            return context.withScheduler(Schedulers.boundedElastic())
                    .sendEmbed(EmbedCreateSpec.builder()
                            .thumbnail(context.getClient().getSelf()
                                    .blockOptional()
                                    .orElseThrow(RuntimeException::new)
                                    .getAvatarUrl())
                            .addField("Servers", context.getClient().getGuilds().count()
                                    .blockOptional()
                                    .orElse(-1L)
                                    .toString(), false)
                            .build());
        }
    }

    public static Mono<Void> requestMembers(CommandContext context) {
        Message message = context.getMessage();
        String guildId = context.parameters();
        return context.getClient().requestMembers(Snowflake.of(guildId))
                .doOnNext(member -> log.info("{}", member))
                .then(message.getRestChannel().createMessage(
                        MessageCreateRequest.builder()
                                .content(MentionUtil.forUser(Snowflake.of(message.getUserData().id())) + " Done!")
                                .build()))
                .then();
    }

    public static Mono<Void> getMembers(CommandContext context) {
        String guildId = context.parameters();
        return context.getClient().getGuildById(Snowflake.of(guildId))
                .flatMapMany(Guild::getMembers)
                .doOnNext(member -> log.info("{}", member.getTag()))
                .then();
    }

    public static Mono<Void> addRole(CommandContext context) {
        Message message = context.getMessage();
        String params = context.parameters();
        return message.getGuild()
                .flatMap(guild -> {
                    // params: "userId roleName"
                    String[] tokens = params.split(" ");
                    if (tokens.length > 2) {
                        String user = tokens[0];
                        String roleName = tokens[1];
                        Mono<Member> member = guild.getMemberById(Snowflake.of(user));
                        Mono<Role> role = guild.getRoles()
                                .filter(r -> r.getName().equalsIgnoreCase(roleName))
                                .next();
                        return member.zipWith(role)
                                .flatMap(t2 -> t2.getT1().addRole(t2.getT2().getId()));
                    }
                    return Mono.empty();
                });
    }

    public static Mono<Void> burstMessages(CommandContext context) {
        return context.getMessage().getGuild()
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

    public static Mono<Void> changeAvatar(CommandContext context) {
        for (Attachment attachment : context.getMessage().getAttachments()) {
            // This code is very optimistic as it does not check for status codes or file types
            return ReactorResources.DEFAULT_HTTP_CLIENT.get()
                    .get()
                    .uri(attachment.getUrl())
                    .responseSingle((res, mono) -> mono.asByteArray())
                    .flatMap(input -> context.getClient().edit().withAvatar(Image.ofRaw(input, Image.Format.PNG)))
                    .then();
        }
        return Mono.empty();
    }

    private static final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    public static Mono<Void> logLevelChange(CommandContext context) {
        return Mono.fromRunnable(() -> {
            String params = context.parameters();
            String[] tokens = params.split(" ");
            String level = tokens[0];
            String name = tokens[1];
            Level logLevel = Level.valueOf(level);
            loggerContext.getLoggerList().stream()
                    .filter(logger -> logger.getName().startsWith(name))
                    .forEach(logger -> {
                        log.info("Changing {} to {}", logger, logLevel);
                        logger.setLevel(logLevel);
                    });
        });
    }

    public static Mono<Void> userInfo(CommandContext context) {
        Message message = context.getMessage();
        String params = context.parameters();
        return message.getClient().getUserById(Snowflake.of(params))
                .flatMap(user -> message.getChannel()
                        .flatMap(channel -> channel.createEmbed(EmbedCreateSpec.builder()
                                .addField("Name", user.getUsername(), false)
                                .addField("Avatar URL", user.getAvatarUrl(), false)
                                .image(user.getAvatarUrl())
                                .build())))
                .switchIfEmpty(Mono.just("Not found")
                        .flatMap(reason -> message.getChannel()
                                .flatMap(channel -> channel.createMessage(reason))))
                .then();
    }

    public static Mono<Void> reactionRemove(CommandContext context) {
        String[] tokens = context.parameters().split(" ");
        return context.getClient().getMessageById(Snowflake.of(tokens[0]), Snowflake.of(tokens[1]))
                .flatMap(m -> m.removeReactions(ReactionEmoji.unicode("✅")));
    }

    public static Mono<Void> leaveGuild(CommandContext context) {
        String params = context.parameters();
        return context.getClient().getGuildById(Snowflake.of(params)).flatMap(Guild::leave);
    }
}
