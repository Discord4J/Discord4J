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
import discord4j.core.object.entity.Message;
import discord4j.core.object.guildmessagesearch.AuthorType;
import discord4j.core.object.guildmessagesearch.SearchHasType;
import discord4j.core.object.guildmessagesearch.SortBy;
import discord4j.core.object.guildmessagesearch.SortOrder;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class ExampleGuildMessageSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleGuildMessageSearch.class);

    private static final String token = System.getenv("token");
    private static final String guildId = System.getenv("guildId");

    public static void main(String[] args) {
        ExampleGuildMessageSearch.LOGGER.info("Starting example for guild message search [{}]", guildId);
        DiscordClient.create(token)
                .gateway()
                .setEnabledIntents(IntentSet.of(Intent.MESSAGE_CONTENT))
                .withGateway(gw -> {
                    Publisher<?> messagesSearch = gw.on(ReadyEvent.class)
                            .flatMap(ready -> gw.getGuildById(Snowflake.of(guildId)))
                            .flatMap(guild -> {
                                // Search for the last 12 messages that contain a file but not an image,
                                // sent by anyone except bots, and exclude nsfw
                                ExampleGuildMessageSearch.LOGGER.info("Try to search messages in {}", guild.getName());
                                return guild.searchMessages()
                                        .withLimit(12)
                                        .withSortBy(SortBy.TIMESTAMP)
                                        .withSortOrder(SortOrder.DESC)
                                        .withHasType(SearchHasType.FILE)
                                        .withHasNotType(SearchHasType.IMAGE)
                                        .withAuthorNotTypes(AuthorType.BOT)
                                        .withIncludeNsfw(false)
                                        .flatMap(guildSearchResult -> Mono.fromRunnable(() -> {
                                            ExampleGuildMessageSearch.LOGGER.info("Found {} messages in total", guildSearchResult.getTotalResults());

                                            for (Message message : guildSearchResult.getMessages()) {
                                                ExampleGuildMessageSearch.LOGGER.info("- {}", message.toString());
                                            }
                                        }));
                            })
                            .switchIfEmpty(Mono.fromRunnable(() -> ExampleGuildMessageSearch.LOGGER.info("No messages found")));
                    return Mono.when(messagesSearch);
                })
                .block();
    }
}
