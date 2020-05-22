package discord4j.core.support;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BotSupport {

    private static final Logger log = Loggers.getLogger(BotSupport.class);

    private final GatewayDiscordClient client;

    public static BotSupport create(GatewayDiscordClient client) {
        return new BotSupport(client);
    }

    BotSupport(GatewayDiscordClient client) {
        this.client = client;
    }

    public Mono<Void> eventHandlers() {
        return Mono.when(readyHandler(client), commandHandler(client));
    }

    public static Mono<Void> readyHandler(GatewayDiscordClient client) {
        return client.on(ReadyEvent.class)
                .doOnNext(ready -> log.info("Logged in as {}", ready.getSelf().getUsername()))
                .then();
    }

    public static Mono<Void> commandHandler(GatewayDiscordClient client) {
        Mono<Long> ownerId = client.rest().getApplicationInfo()
                .map(ApplicationInfoData::owner)
                .map(user -> Snowflake.asLong(user.id()))
                .cache();

        List<EventHandler> eventHandlers = new ArrayList<>();
        eventHandlers.add(new Echo());
        eventHandlers.add(new Status());
        eventHandlers.add(new StatusEmbed());
        eventHandlers.add(new Exit());
        eventHandlers.add(new RequestMembers());
        eventHandlers.add(new GetMembers());

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

    public static class Echo extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            String content = message.getContent();
            if (content.startsWith("!echo ")) {
                return message.getRestChannel().createMessage(
                        MessageCreateRequest.builder()
                                .content("<@" + message.getUserData().id() + "> " + content.substring("!echo ".length()))
                                .build())
                        .flatMap(data -> event.getClient().rest().restMessage(data)
                                .createReaction("\uD83C\uDF89"))
                        .then();
            }
            return Mono.empty();
        }
    }

    public static class StatusEmbed extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            return Mono.justOrEmpty(message.getContent())
                    .filter(content -> content.equals("!status"))
                    .flatMap(source -> message.getChannel()
                            .publishOn(Schedulers.boundedElastic())
                            .flatMap(channel -> channel.createEmbed(spec -> {
                                spec.setThumbnail(event.getClient().getSelf()
                                        .blockOptional()
                                        .orElseThrow(RuntimeException::new)
                                        .getAvatarUrl());
                                spec.addField("Servers", event.getClient().getGuilds().count()
                                        .blockOptional()
                                        .orElse(-1L)
                                        .toString(), false);
                            })))
                    .then();
        }
    }

    public static class Status extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            return Mono.justOrEmpty(message.getContent())
                    .filter(content -> content.startsWith("!status "))
                    .map(content -> {
                        String status = content.substring("!status ".length());
                        if (status.equalsIgnoreCase("online")) {
                            return Presence.online();
                        } else if (status.equalsIgnoreCase("dnd")) {
                            return Presence.doNotDisturb();
                        } else if (status.equalsIgnoreCase("idle")) {
                            return Presence.idle();
                        } else if (status.equalsIgnoreCase("invisible")) {
                            return Presence.invisible();
                        } else {
                            throw new IllegalArgumentException("Invalid argument");
                        }
                    })
                    .flatMap(presence -> event.getClient().updatePresence(presence))
                    .then();
        }
    }

    public static class Exit extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            return Mono.justOrEmpty(message.getContent())
                    .filter(content -> content.equals("!exit"))
                    .flatMap(presence -> event.getClient().logout())
                    .then();
        }
    }

    public static class RequestMembers extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            String content = message.getContent();
            if (content.startsWith("!members ")) {
                String guildId = content.substring("!members ".length());
                return event.getClient().requestMembers(Snowflake.of(guildId))
                        .doOnNext(member -> log.info("{}", member))
                        .then(message.getRestChannel().createMessage(
                                MessageCreateRequest.builder()
                                        .content("<@" + message.getUserData().id() + "> Done!")
                                        .build()))
                        .then();
            }
            return Mono.empty();
        }
    }

    public static class GetMembers extends EventHandler {

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();
            String content = message.getContent();
            if (content.startsWith("!getMembers ")) {
                String guildId = content.substring("!getMembers ".length());
                return event.getClient().getGuildById(Snowflake.of(guildId))
		                .flatMapMany(Guild::getMembers)
                        .doOnNext(member -> log.info("{}", member.getTag()))
                        .then();
            }
            return Mono.empty();
        }
    }
}
