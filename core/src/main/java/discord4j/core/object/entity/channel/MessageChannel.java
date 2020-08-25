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

import discord4j.core.object.entity.Message;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateMono;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** A Discord channel that can utilize messages. */
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
     * Requests to retrieve the last message sent in this channel, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the last message
     * @return A {@link Mono} where, upon successful completion, emits the last {@link Message message} sent in this
     * channel, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Message> getLastMessage(EntityRetrievalStrategy retrievalStrategy);

    /**
     * Gets when the last pinned message was pinned, if present.
     *
     * @return When the last pinned message was pinned, if present.
     */
    Optional<Instant> getLastPinTimestamp();

    /**
     * Requests to create a message.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link MessageCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    Mono<Message> createMessage(Consumer<? super MessageCreateSpec> spec);

    /**
     * Requests to create a message with only {@link MessageCreateSpec#setContent(String) content}.
     *
     * @param message A string message to populate the message with.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     *
     * @see MessageCreateSpec#setContent(String)
     */
    default Mono<Message> createMessage(final String message) {
        return createMessage(spec -> spec.setContent(message));
    }

    /**
     * Requests to create a message with only an {@link MessageCreateSpec#setEmbed(Consumer)}.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link EmbedCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    default Mono<Message> createEmbed(final Consumer<? super EmbedCreateSpec> spec) {
        return createMessage(messageSpec -> messageSpec.setEmbed(spec));
    }

    default EmbedCreateMono createEmbed() {
        return new EmbedCreateMono(getClient(), getRestChannel());
    }

    /**
     * Requests to trigger the typing indicator in this channel. A single invocation of this method will trigger the
     * indicator for 10 seconds or until the bot sends a message in this channel.
     *
     * @return A {@link Mono} which completes upon successful triggering of the typing indicator in this channel. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Void> type();

    /**
     * Requests to trigger the typing indicator in this channel. It will be continuously triggered every 10 seconds
     * until the given publisher emits.
     * <p>
     * This method <b>cannot</b> stop the typing indicator during the 10 second duration. It simply checks every 10
     * seconds whether to trigger the indicator again depending on if the publisher emitted. For example, the following
     * code will show the typing indicator for <b>10</b> seconds, not 5:
     * <pre>
     * {@code
     * channel.typeUntil(Mono.delay(Duration.ofSeconds(5))
     * }
     * </pre>
     * <p>
     * The only way to stop the typing indicator during the 10 second duration is to send a message in the channel. For
     * example, the following code will show the typing indicator until the message is sent:
     * <pre>
     * {@code
     * channel.typeUntil(channel.createMessage("Hello"))
     * }
     * </pre>
     *
     * @param until The companion {@link Publisher} that signals when to stop triggering the typing indicator.
     * @return A {@link Flux} which continually emits each time the typing indicator is triggered and completes when it
     * will no longer be triggered. If an error is received, it is emitted through the {@code Flux}.
     *
     * @implNote The default implementation actually sends a typing request every 8 seconds so it appears continuous.
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

    /**
     * Requests to retrieve the message as represented by the supplied ID.
     *
     * @param id The ID of the message.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Message> getMessageById(Snowflake id);

    /**
     * Requests to retrieve the message as represented by the supplied ID, using the given retrieval strategy.
     *
     * @param id The ID of the message.
     * @param retrievalStrategy the strategy to use to get the message
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Message> getMessageById(Snowflake id, EntityRetrievalStrategy retrievalStrategy);

    /**
     * Requests to retrieve all the pinned messages for this channel.
     *
     * @return A {@link Flux} that continually emits all the pinned messages for this channel. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    Flux<Message> getPinnedMessages();
}
