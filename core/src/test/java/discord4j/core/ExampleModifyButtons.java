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
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageCreateSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.ArrayList;
import java.util.List;

public class ExampleModifyButtons {

    private static final Logger log = Loggers.getLogger(ExampleModifyButtons.class);

    private static final String TOKEN = System.getenv("token");
    private static final String GUILD_ID = System.getenv("guildId");
    private static final String CHANNEL_ID = System.getenv("channelId");

    public static void main(String[] args) {
        DiscordClient.create(TOKEN)
                .withGateway(gateway -> {

                    long id = System.currentTimeMillis();
                    String add = id + "-add";
                    String rem = id + "-rem";

                    Publisher<?> initialMessage = gateway.getChannelById(Snowflake.of(CHANNEL_ID))
                            .ofType(TextChannel.class)
                            .flatMap(it -> it.createMessage(MessageCreateSpec.builder()
                                    .content("Press a button")
                                    .addComponent(ActionRow.of(
                                            Button.primary(add, "Add"),
                                            Button.danger(rem, "Remove")))
                                    .build()));

                    Publisher<?> listener = gateway.on(ButtonInteractionEvent.class, event -> {
                        log.info("Event: {}", event.getCustomId());

                        Message message = event.getMessage().orElseThrow(RuntimeException::new);

                        if (add.equals(event.getCustomId())) {
                            List<LayoutComponent> edited = new ArrayList<>(message.getComponents());
                            int lastIndex = message.getComponents().size() - 1;
                            LayoutComponent last = message.getComponents().get(lastIndex);
                            int count = message.getComponents()
                                    .stream()
                                    .mapToInt(it -> it.getChildren().size())
                                    .sum();

                            if (last instanceof ActionRow) {
                                ActionRow row = (ActionRow) last;
                                if (count == 25) {
                                    return event.edit(InteractionApplicationCommandCallbackSpec.builder()
                                                    .content("Cannot add more than 25 buttons")
                                                    .build());
                                } else {
                                    String current = String.valueOf(count + 1);

                                    if (row.getChildren().size() < 5) {
                                        ActionRow editedLastRow = row.withAddedComponent(
                                                Button.secondary(id + "-" + current, current));
                                        edited.set(lastIndex, editedLastRow);
                                    } else {
                                        ActionRow editedNewRow = ActionRow.of(
                                                Button.secondary(id + "-" + current, current));
                                        edited.add(editedNewRow);
                                    }

                                    return event.edit(InteractionApplicationCommandCallbackSpec.builder()
                                                    .content("Button added!")
                                                    .components(edited)
                                                    .build());
                                }
                            }
                        } else if (rem.equals(event.getCustomId())) {
                            List<LayoutComponent> edited = new ArrayList<>(message.getComponents());
                            int lastIndex = message.getComponents().size() - 1;
                            LayoutComponent last = message.getComponents().get(lastIndex);
                            int count = message.getComponents()
                                    .stream()
                                    .mapToInt(it -> it.getChildren().size())
                                    .sum();

                            if (last instanceof ActionRow) {
                                ActionRow row = (ActionRow) last;
                                if (count == 2) {
                                    return event.edit(InteractionApplicationCommandCallbackSpec.builder()
                                                    .content("No buttons to remove")
                                                    .build());
                                } else {
                                    String current = String.valueOf(count);

                                    if (row.getChildren().size() == 1) {
                                        edited.remove(lastIndex);
                                    } else {
                                        ActionRow editedNewRow = row.withRemovedComponent(id + "-" + current);
                                        edited.set(lastIndex, editedNewRow);
                                    }

                                    return event.edit(InteractionApplicationCommandCallbackSpec.builder()
                                                    .content("Button removed!")
                                                    .components(edited)
                                                    .build());
                                }
                            }
                        }

                        return event.reply("Done after pressing " + event.getCustomId())
                                .withEphemeral(true);
                    });

                    return Mono.when(initialMessage, listener);
                })
                .block();
    }
}
