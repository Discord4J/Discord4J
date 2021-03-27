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
import discord4j.core.command.CommandRequest;
import discord4j.core.command.CommandResponse;
import discord4j.core.object.clientpresence.ClientPresence;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
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

    public static Mono<Boolean> isAuthor(Mono<Long> authorId, CommandRequest request) {
        return authorId.filter(id -> request.getAuthor()
                .map(User::getId)
                .map(Snowflake::asLong)
                .map(eventAuthor -> eventAuthor.equals(id))
                .orElse(false))
                .hasElement();
    }

    public static Mono<Void> echo(CommandRequest request, CommandResponse response) {
        Message message = request.getMessage();
        return message.getRestChannel().createMessage(
                MessageCreateRequest.builder()
                        .content("<@" + message.getUserData().id() + "> " + request.parameters())
                        .build())
                .flatMap(data -> request.getClient().rest().restMessage(data)
                        .createReaction("✅"))
                .then();
    }

    public static Mono<Void> status(CommandRequest request, CommandResponse response) {
        String params = request.parameters();
        if (params.equalsIgnoreCase("online")) {
            return request.getClient().updatePresence(ClientPresence.online());
        } else if (params.equalsIgnoreCase("dnd")) {
            return request.getClient().updatePresence(ClientPresence.doNotDisturb());
        } else if (params.equalsIgnoreCase("idle")) {
            return request.getClient().updatePresence(ClientPresence.idle());
        } else if (params.equalsIgnoreCase("invisible")) {
            return request.getClient().updatePresence(ClientPresence.invisible());
        } else {
            // showing you can block too
            return response.withScheduler(Schedulers.boundedElastic())
                    .sendEmbed(spec -> {
                        spec.setThumbnail(request.getClient().getSelf()
                                .blockOptional()
                                .orElseThrow(RuntimeException::new)
                                .getAvatarUrl());
                        spec.addField("Servers", request.getClient().getGuilds().count()
                                .blockOptional()
                                .orElse(-1L)
                                .toString(), false);
                    });
        }
    }

    public static Mono<Void> requestMembers(CommandRequest request, CommandResponse response) {
        Message message = request.getMessage();
        String guildId = request.parameters();
        return request.getClient().requestMembers(Snowflake.of(guildId))
                .doOnNext(member -> log.info("{}", member))
                .then(message.getRestChannel().createMessage(
                        MessageCreateRequest.builder()
                                .content("<@" + message.getUserData().id() + "> Done!")
                                .build()))
                .then();
    }

    public static Mono<Void> getMembers(CommandRequest request, CommandResponse response) {
        String guildId = request.parameters();
        return request.getClient().getGuildById(Snowflake.of(guildId))
                .flatMapMany(Guild::getMembers)
                .doOnNext(member -> log.info("{}", member.getTag()))
                .then();
    }

    public static Mono<Void> addRole(CommandRequest request, CommandResponse response) {
        Message message = request.getMessage();
        String params = request.parameters();
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

    public static Mono<Void> burstMessages(CommandRequest request, CommandResponse response) {
        return request.getMessage().getGuild()
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

    public static Mono<Void> changeAvatar(CommandRequest request, CommandResponse response) {
        for (Attachment attachment : request.getMessage().getAttachments()) {
            // This code is very optimistic as it does not check for status codes or file types
            return ReactorResources.DEFAULT_HTTP_CLIENT.get()
                    .get()
                    .uri(attachment.getUrl())
                    .responseSingle((res, mono) -> mono.asByteArray())
                    .flatMap(input -> request.getClient()
                            .edit(spec -> spec.setAvatar(Image.ofRaw(input, Image.Format.PNG))))
                    .then();
        }
        return Mono.empty();
    }

    private static final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    public static Mono<Void> logLevelChange(CommandRequest request, CommandResponse response) {
        return Mono.fromRunnable(() -> {
            String params = request.parameters();
            String[] tokens = params.split(" ");
            String level = tokens[0];
            String name = tokens[1];
            Level logLevel = Level.valueOf(level);
            context.getLoggerList().stream()
                    .filter(logger -> logger.getName().startsWith(name))
                    .forEach(logger -> {
                        log.info("Changing {} to {}", logger, logLevel);
                        logger.setLevel(logLevel);
                    });
        });
    }

    public static Mono<Void> userInfo(CommandRequest request, CommandResponse response) {
        Message message = request.getMessage();
        String params = request.parameters();
        return message.getClient().getUserById(Snowflake.of(params))
                .flatMap(user -> message.getChannel()
                        .flatMap(channel -> channel.createEmbed(embed -> embed
                                .addField("Name", user.getUsername(), false)
                                .addField("Avatar URL", user.getAvatarUrl(), false)
                                .setImage(user.getAvatarUrl()))))
                .switchIfEmpty(Mono.just("Not found")
                        .flatMap(reason -> message.getChannel()
                                .flatMap(channel -> channel.createMessage(reason))))
                .then();
    }

    public static Mono<Void> reactionRemove(CommandRequest request, CommandResponse response) {
        String[] tokens = request.parameters().split(" ");
        return request.getClient().getMessageById(Snowflake.of(tokens[0]), Snowflake.of(tokens[1]))
                .flatMap(m -> m.removeReactions(ReactionEmoji.unicode("✅")));
    }

    public static Mono<Void> leaveGuild(CommandRequest request, CommandResponse response) {
        String params = request.parameters();
        return request.getClient().getGuildById(Snowflake.of(params)).flatMap(Guild::leave);
    }
}
