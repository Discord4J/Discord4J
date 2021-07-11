package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.rest.util.AllowedMentions;
import reactor.core.publisher.Mono;

public class ExampleAllowedMentions {

    public static void main(String[] args) {
        DiscordClient.builder(System.getenv("token"))
                .build()
                .gateway()
                .login()
                .flatMapMany(client -> client.on(MessageCreateEvent.class))
                .filter(event -> event.getGuildId().isPresent())
                .filter(event -> event.getMessage().getContent().startsWith("pingme"))
                .flatMap(ExampleAllowedMentions::sendAllowedMentionsMessage)
                .onErrorContinue((throwable, o) -> throwable.printStackTrace())
                .then()
                .block();
    }

    private static Mono<Message> sendAllowedMentionsMessage(MessageCreateEvent event) {
        final User author = event.getMessage().getAuthor().get();
        return event.getMessage().getChannel()
                .flatMap(channel ->
                        channel.createMessage("Hello " + author.getMention() + ", get pinged!")
                                .withAllowedMentions(AllowedMentions.builder()
                                        .allowUser(author.getId())
                                        .build())
                                .thenReturn(channel))
                .flatMap(channel ->
                        channel.createMessage("Hello " + author.getMention() + ", get (not) pinged!")
                                .withAllowedMentions(AllowedMentions.builder().build())
                                .thenReturn(channel))
                .flatMap(channel ->
                        channel.createMessage("This is invalid")
                                .withAllowedMentions(AllowedMentions.builder()
                                        .parseType(AllowedMentions.Type.USER)
                                        .allowUser(author.getId())
                                        .build()));
    }

}
