package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.ButtonInteractEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Buttons;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

public class ExampleButtons {

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
                                msg.setContent("Click some buttons!");
                                msg.setActionRows(
                                        ActionRow.of(
                                                //              ID,  label
                                                Buttons.primary("1", "1"),
                                                Buttons.primary("2", "2"),
                                                Buttons.primary("3", "3"),
                                                Buttons.primary("4", "4"),
                                                Buttons.primary("5", "5")
                                        ),
                                        ActionRow.of(
                                                Buttons.primary("6", "6"),
                                                Buttons.primary("7", "7"),
                                                Buttons.primary("8", "8"),
                                                Buttons.primary("9", "9"),
                                                Buttons.primary("10", "10")
                                        ),
                                        ActionRow.of(
                                                Buttons.primary("11", "11"),
                                                Buttons.primary("12", "12"),
                                                Buttons.primary("13", "13"),
                                                Buttons.primary("14", "14"),
                                                Buttons.primary("15", "15")
                                        ),
                                        ActionRow.of(
                                                Buttons.primary("16", "16"),
                                                Buttons.primary("17", "17"),
                                                Buttons.primary("18", "18"),
                                                Buttons.primary("19", "19"),
                                                Buttons.primary("20", "20")
                                        ),
                                        ActionRow.of(
                                                Buttons.primary("21", "21"),
                                                Buttons.primary("22", "22"),
                                                Buttons.primary("23", "23"),
                                                Buttons.primary("24", "24"),
                                                Buttons.primary("25", "25")
                                        )
                                );
                            }));

                    return sendMessage
                            .map(Message::getId)
                            .flatMapMany(buttonMessageId ->
                                    gw.on(ButtonInteractEvent.class, event ->
                                            Mono.justOrEmpty(event.getInteraction().getMessage())
                                                .map(Message::getId)
                                                .filter(buttonMessageId::equals)
                                                .then(event.reply(event.getCustomId()))
                                        )
                            )
                            .then();
                })
                .block();
    }
}
