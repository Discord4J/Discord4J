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
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
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
                                    gw.on(SelectMenuInteractionEvent.class, event ->
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
