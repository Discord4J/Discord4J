package discord4j.core.interaction;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

public class ExampleSelectMenu {

    private static final String token = System.getenv("token");
    private static final String guildId = System.getenv("guildId");
    private static final String channelId = System.getenv("channelId");

    public static void main(String[] args) {
        DiscordClient.create(token)
                .login()
                .flatMap(gw -> {
                    Mono<Message> sendMessage = gw.on(GuildCreateEvent.class)
                            .filter(e -> e.getGuild().getId().asString().equals(guildId))
                            .next()
                            .flatMap(e -> e.getGuild().getChannelById(Snowflake.of(channelId)))
                            .ofType(TextChannel.class)
                            .flatMap(channel -> channel.createMessage(msg -> {
                                msg.setContent("Select some options!");
                                msg.setComponents(
                                        ActionRow.of(
                                                SelectMenu.of("mySelectMenu",
                                                        SelectMenu.Option.of("option 1", "foo"),
                                                        SelectMenu.Option.of("option 2", "bar"),
                                                        SelectMenu.Option.of("option 3", "baz"))
                                                        .withMaxValues(2)
                                        )
                                );
                            }));

                    return sendMessage
                            .map(Message::getId)
                            .flatMapMany(selectMenuMessageId ->
                                    gw.on(SelectMenuInteractEvent.class, event ->
                                            Mono.justOrEmpty(event.getInteraction().getMessage())
                                                    .map(Message::getId)
                                                    .filter(selectMenuMessageId::equals)
                                                    .then(event.reply(event.getValues().toString()))
                                    )
                            )
                            .then();
                })
                .block();
    }

}
