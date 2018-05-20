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
package discord4j.core.object.entity;

import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** A Discord channel that can utilizes messages. */
public interface MessageChannel extends Channel {

    /**
     * Gets the ID of the last message sent in this channel, if present.
     *
     * @return The ID of the last message sent in this channel, if present.
     */
    Optional<Snowflake> getLastMessageId();

    /**
     * Requests to retrieve the last message sent in this channel, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the last {@link Message message} sent in this
     * channel, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Message> getLastMessage();

    /**
     * Gets when the last pinned message was pinned, if present.
     *
     * @return When the last pinned message was pinned, if present.
     */
    Optional<Instant> getLastPinTimestamp();

    /**
     * Requests to create a message.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link MessageCreateSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #createMessage(MessageCreateSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    Mono<Message> createMessage(Consumer<MessageCreateSpec> spec);

    /**
     * Requests to create a message.
     *
     * @param spec A configured {@link MessageCreateSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    Mono<Message> createMessage(MessageCreateSpec spec);

    /**
     * Requests to trigger the typing indicator in this channel. A single invocation of this method will trigger the
     * indicator for 10 seconds or until the bot sends a message in this channel.
     *
     * @return A {@link Mono} which completes upon successful triggering of the typing indicator in this channel. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Void> type();

    /**
     * Requests to trigger the typing indicator in this channel. It will be continuously triggered until the given
     * publisher emits.
     *
     * @param until The companion {@link Publisher} that signals when to stop triggering the typing indicator.
     * @return A {@link Flux} which continually emits each time the typing indicator is triggered and completes when it
     * will no longer be triggered. If an error is received, it is emitted through the {@code Flux}.
     */
    Flux<Long> typeUntil(Publisher<?> until);

    /**
     * Requests to retrieve <i>all</i> messages <i>before</i> the specified ID.
     * <p>
     * The returned {@code Flux} will emit items in <i>reverse-</i>chronological order (newest to oldest). It is
     * recommended to limit the emitted items by invoking either {@link Flux#takeWhile(Predicate)} (to retrieve IDs
     * within a specified range) or {@link Flux#take(long)} (to retrieve a specific amount of IDs).
     * <p>
     * The following example will get <i>all</i> messages from {@code messageId} to {@code myOtherMessageId}:
     * {@code getMessagesBefore(messageId).takeWhile(message -> message.getId().compareTo(myOtherMessageId) >= 0)}
     *
     * @param messageId The ID of the <i>newest</i> message to retrieve. Use {@link Snowflake#of(Instant)} to retrieve a
     * time-based ID.
     * @return A {@link Flux} that continually emits <i>all</i> {@link Message messages} <i>before</i> the specified ID.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    Flux<Message> getMessagesBefore(Snowflake messageId);

    /**
     * Requests to retrieve <i>all</i> messages <i>after</i> the specified ID.
     * <p>
     * The returned {@code Flux} will emit items in chronological order (oldest to newest). It is recommended to limit
     * the emitted items by invoking either {@link Flux#takeWhile(Predicate)} (to retrieve IDs within a specified range)
     * or {@link Flux#take(long)} (to retrieve a specific amount of IDs).
     * <p>
     * The following example will get <i>all</i> messages from {@code messageId} to {@code myOtherMessageId}:
     * {@code getMessagesAfter(messageId).takeWhile(message -> message.getId().compareTo(myOtherMessageId) <= 0)}
     *
     * @param messageId The ID of the <i>oldest</i> message to retrieve. Use {@link Snowflake#of(Instant)} to retrieve a
     * time-based ID.
     * @return A {@link Flux} that continually emits <i>all</i> {@link Message messages} <i>after</i> the specified ID.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    Flux<Message> getMessagesAfter(Snowflake messageId);
}
