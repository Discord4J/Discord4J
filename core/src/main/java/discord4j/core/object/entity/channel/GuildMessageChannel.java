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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity.channel;

import discord4j.common.util.Snowflake;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.BulkDeleteRequest;
import discord4j.rest.util.Permission;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/** A Discord channel in a guild that can have messages sent to it. */
public interface GuildMessageChannel extends GuildChannel, MessageChannel {

    /**
     * Requests to bulk delete the supplied message IDs.
     * <p>
     * Typically this method is paired with a call from {@link #getMessagesBefore(Snowflake)} or
     * {@link #getMessagesAfter(Snowflake)} to delete some or (potentially) all messages from a channel.
     *
     * <pre>
     * {@code
     * channel.getMessagesBefore(Snowflake.of(Instant.now()))
     *     .take(420)
     *     .map(Message::getId)
     *     .transform(channel::bulkDelete)
     * }
     * </pre>
     *
     * If you have a {@code Publisher<Message>}, consider {@link #bulkDeleteMessages(Publisher)}.
     *
     * @param messageIds A {@link Publisher} to supply the message IDs to bulk delete.
     * @return A {@link Flux} that continually emits {@link Snowflake message IDs} that were <b>not</b> bulk deleted
     * (typically if the ID was older than 2 weeks). If an error is received, it is emitted through the {@code Flux}.
     */
    default Flux<Snowflake> bulkDelete(Publisher<Snowflake> messageIds) {
        return getRestChannel().bulkDelete(messageIds);
    }

    /**
     * Requests to bulk delete the supplied messages.
     * <p>
     * Typically this method is paired with a call from {@link #getMessagesBefore(Snowflake)} or
     * {@link #getMessagesAfter(Snowflake)} to delete some or (potentially) all messages from a channel.
     *
     * <pre>
     * {@code
     * channel.getMessagesBefore(Snowflake.of(Instant.now()))
     *     .take(420)
     *     .transform(channel::bulkDeleteMessages)
     * }
     * </pre>
     *
     * If you have a {@code Publisher<Snowflake>}, consider {@link #bulkDelete(Publisher)}.
     *
     * @param messages A {@link Publisher} to supply the messages to bulk delete.
     * @return A {@link Flux} that continually emits {@link Message messages} that were <b>not</b> bulk deleted
     * (typically if the message was older than 2 weeks). If an error is received, it is emitted through the
     * {@code Flux}.
     */
    default Flux<Message> bulkDeleteMessages(Publisher<Message> messages) {
        // FIXME This is essentially a copy of the RestChannel implementation which incurs a potentially
        //  problematic amount of duplication. Optimally, this method should be able to delegate to
        //  bulkDelete, but no implementation has been found that can do so in a performant manner.
        final Instant timeLimit = Instant.now().minus(Duration.ofDays(14L));

        return Flux.from(messages)
                .distinct(Message::getId)
                .buffer(100)
                .flatMap(allMessages -> {
                    final List<Message> eligibleMessages = new ArrayList<>(0);
                    final Collection<Message> ineligibleMessages = new ArrayList<>(0);

                    for (final Message message : allMessages) {
                        if (message.getId().getTimestamp().isBefore(timeLimit)) {
                            ineligibleMessages.add(message);

                        } else {
                            eligibleMessages.add(message);
                        }
                    }

                    if (eligibleMessages.size() == 1) {
                        ineligibleMessages.add(eligibleMessages.get(0));
                        eligibleMessages.clear();
                    }

                    final Collection<String> eligibleIds = eligibleMessages.stream()
                            .map(Message::getId)
                            .map(Snowflake::asString)
                            .collect(Collectors.toList());

                    return Mono.just(eligibleIds)
                            .filter(chunk -> !chunk.isEmpty())
                            .flatMap(chunk -> getClient().getRestClient()
                                    .getChannelService()
                                    .bulkDeleteMessages(getId().asLong(), BulkDeleteRequest.builder().messages(chunk).build()))
                            .thenMany(Flux.fromIterable(ineligibleMessages));
                });
    }

    /**
     * Returns all members in the guild which have access to <b>view</b> this channel.
     *
     * @return A {@link Flux} that continually emits all members from {@link Guild#getMembers()} which have access to
     * view this channel {@link discord4j.rest.util.Permission#VIEW_CHANNEL}
     */
    default Flux<Member> getMembers() {
        return getGuild()
                .flatMapMany(Guild::getMembers)
                .filterWhen(member -> getEffectivePermissions(member.getId())
                        .map(permissions -> permissions.contains(Permission.VIEW_CHANNEL)));
    }
}
