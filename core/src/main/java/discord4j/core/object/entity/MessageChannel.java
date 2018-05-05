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

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

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
     * @return A {@link Mono} which completes when the typing indicator in this channel will no longer be triggered. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Void> typeUntil(Publisher<Void> until);

    /**
     * Requests to retrieve messages within the specified ID range, sorted by {@link Snowflake#compareTo(Snowflake)}.
     *
     * @param startId The ID of the <i>earliest</i> message to retrieve (inclusive). To get messages <i>after</i> a
     * specified time (also inclusive) use {@link Snowflake#of(Instant)}. If {@code null}, the default value is
     * equivalent to {@code Snowflake.of(id.getTimestamp())} where {@code id} is {@link #getId()} for this channel.
     *
     * @param endId The ID of the <i>latest</i> message to retrieve (exclusive). To get messages <i>before</i> a
     * specified time (also exclusive) use {@link Snowflake#of(Instant)}. If {@code null}, the default value is
     * equivalent to {@code Snowflake.of(Instant.now())}.
     *
     * @return A {@link Flux} that continually emits {@link Message messages} within the specified ID range, sorted by
     * {@link Snowflake#compareTo(Snowflake)}. If an error is received, it is emitted through the {@code Flux}.
     */
    Flux<Message> getMessages(@Nullable Snowflake startId, @Nullable Snowflake endId);
}
