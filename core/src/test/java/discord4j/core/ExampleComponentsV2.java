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
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.component.Container;
import discord4j.core.object.component.ICanBeUsedInContainerComponent;
import discord4j.core.object.component.Section;
import discord4j.core.object.component.TextDisplay;
import discord4j.core.object.component.Thumbnail;
import discord4j.core.object.component.UnfurledMediaItem;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.ArrayList;
import java.util.List;

public class ExampleComponentsV2 {

    private static final Logger log = Loggers.getLogger(ExampleComponentsV2.class);

    public static void main(String[] args) {
        GatewayDiscordClient client = DiscordClient.create(System.getenv("token"))
            .gateway()
            .withEventDispatcher(d -> d.on(ReadyEvent.class)
                .doOnNext(readyEvent -> log.info("Ready: {}", readyEvent.getShardInfo())))
            .login()
            .block();

        assert client != null;
        client.getChannelById(Snowflake.of(System.getenv("channelId"))).ofType(MessageChannel.class).flatMap(channel -> {
            List<ICanBeUsedInContainerComponent> firstComponents = new ArrayList<>();
            firstComponents.add(TextDisplay.of("D4J"));
            firstComponents.add(TextDisplay.of("is awesome!"));
            firstComponents.add(Section.of(Thumbnail.of(UnfurledMediaItem.of("https://docs.discord4j.com/img/placeholder.png")), TextDisplay.of("A Placeholder Image")));

            Container container = Container.of(firstComponents);

            Section section = Section.of(Thumbnail.of(UnfurledMediaItem.of("https://docs.discord4j.com/img/embed-preview.png")), TextDisplay.of("Old days..."));

            MessageCreateSpec.Builder builder = MessageCreateSpec.builder();
            builder.addFlag(Message.Flag.IS_COMPONENTS_V2);
            builder.addComponent(container);
            builder.addComponent(section);


            return channel.createMessage(builder.build());
        }).subscribe();

        client.onDisconnect().block();
    }

}
