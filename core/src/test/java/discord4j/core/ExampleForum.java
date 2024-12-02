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
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.ForumTag;
import discord4j.core.object.entity.channel.ForumChannel;
import discord4j.core.spec.*;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;

import java.util.Optional;

/**
 * Showcase some forum channels operations under a given test guild. Requires TOKEN and GUILD_ID environment variables.
 * <p>
 * You can optionally include a PARENT_ID category to create the forum channel inside a category.
 */
public class ExampleForum {

    private static final String TOKEN = System.getenv("TOKEN");
    private static final long GUILD_ID = Long.parseLong(System.getenv("GUILD_ID"));

    public static void main(String[] args) {
        // Those are tags that will be created in the forum channel
        final ForumTagCreateSpec testTag1 = ForumTagCreateSpec.builder()
                .name("Tag 1")
                .emojiNameOrNull("✅")
                .build(),
                testTag2 = ForumTagCreateSpec.builder()
                        .name("Tag 2")
                        .emojiNameOrNull("❤️")
                        .build();

        Possible<Snowflake> parentId = Optional.ofNullable(System.getenv("PARENT_ID"))
                .map(Long::parseLong)
                .map(it -> Possible.of(Snowflake.of(it)))
                .orElse(Possible.absent());

        DiscordClient.create(TOKEN)
                .withGateway(client -> client.on(ReadyEvent.class, event -> client.getGuildById(Snowflake.of(GUILD_ID))
                        //Create forum channel
                        .flatMap(guild -> guild.createForumChannel(ForumChannelCreateSpec.builder()
                                .name("test-forum")
                                .parentId(parentId)
                                .addAvailableTags(testTag1, testTag2)
                                .defaultSortOrderOrNull(ForumChannel.SortOrder.LATEST_ACTIVITY.getValue())
                                .defaultForumLayoutOrNull(ForumChannel.LayoutType.GALLERY_VIEW.getValue())
                                .reason("Creating the test forum channel")
                                .build()))
                        //Starting a thread
                        .flatMap(channel -> {
                            Snowflake tag2Id = channel.getAvailableTags().stream()
                                    .filter(tag -> tag.getName().equals("Tag 2"))
                                    .findFirst()
                                    .map(ForumTag::getId)
                                    .orElseThrow(IllegalArgumentException::new);

                            return channel.startThread(StartThreadInForumChannelSpec.builder()
                                    .name("Test Thread")
                                    .addAppliedTag(tag2Id)
                                    .reason("Creating a test thread")
                                    .message(ForumThreadMessageCreateSpec.builder()
                                            .content("Message content")
                                            .addComponent(ActionRow.of(Button.primary("test",
                                                    "Test button (does nothing)")))
                                            .addEmbed(EmbedCreateSpec.builder()
                                                    .title("Test embed")
                                                    .color(Color.ORANGE)
                                                    .build())
                                            .build())
                                    .build());
                        })
                ))
                .block();
    }
}
