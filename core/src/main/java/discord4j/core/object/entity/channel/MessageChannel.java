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
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.poll.Poll;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateMono;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.PollCreateMono;
import discord4j.core.spec.PollCreateSpec;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import discord4j.core.spec.legacy.LegacyMessageCreateSpec;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.PaginationUtil;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A Discord channel that can utilize messages.
 */
public interface MessageChannel extends Channel {

    /**
     * Gets the ID of the last message sent in this channel, if present.
     *
     * @return The ID of the last message sent in this channel, if present.
     */
    default Optional<Snowflake> getLastMessageId() {
        return Possible.flatOpt(getData().lastMessageId())
                .map(Snowflake::of);
    }

    /**
     * Requests to retrieve the last message sent in this channel, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the last {@link Message message} sent in this
     * channel, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Message> getLastMessage() {
        return Mono.justOrEmpty(getLastMessageId())
                .flatMap(id -> getClient().getMessageById(getId(), id));
    }

    /**
     * Requests to retrieve the last message sent in this channel, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the last message
     * @return A {@link Mono} where, upon successful completion, emits the last {@link Message message} sent in this
     * channel, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Message> getLastMessage(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getLastMessageId())
                .flatMap(id -> getClient().withRetrievalStrategy(retrievalStrategy).getMessageById(getId(), id));
    }

    /**
     * Gets when the last pinned message was pinned, if present.
     *
     * @return When the last pinned message was pinned, if present.
     */
    default Optional<Instant> getLastPinTimestamp() {
        return Possible.flatOpt(getData().lastPinTimestamp())
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Requests to create a message.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyMessageCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createMessage(MessageCreateSpec)} or {@link #createMessage(String)} which offer an
     * immutable approach to build specs
     */
    @Deprecated
    default Mono<Message> createMessage(Consumer<? super LegacyMessageCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyMessageCreateSpec mutatedSpec = new LegacyMessageCreateSpec();
                    getClient().getRestClient().getRestResources()
                            .getAllowedMentions()
                            .ifPresent(mutatedSpec::setAllowedMentions);
                    spec.accept(mutatedSpec);
                    return getRestChannel().createMessage(mutatedSpec.asRequest());
                })
                .map(data -> new Message(getClient(), data));
    }

    /**
     * Requests to create a message.
     *
     * @param spec an immutable object that specifies how to create the message
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @see MessageCreateSpec#builder()
     */
    default Mono<Message> createMessage(MessageCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> {
                    MessageCreateSpec actualSpec = getClient().getRestClient()
                            .getRestResources()
                            .getAllowedMentions()
                            .filter(allowedMentions -> !spec.isAllowedMentionsPresent())
                            .map(spec::withAllowedMentions)
                            .orElse(spec);
                    return getRestChannel().createMessage(actualSpec.asRequest());
                })
                .map(data -> new Message(getClient(), data));
    }

    /**
     * Requests to create a message with a content. Other properties specifying how to create the message can be set via
     * the {@code withXxx} methods of the returned {@link MessageCreateMono}.
     *
     * @param message A string message to populate the message with.
     * @return A {@link MessageCreateMono} where, upon successful completion, emits the created {@link Message}. If an
     * error is received, it is emitted through the {@code MessageCreateMono}.
     * @see #createMessage(MessageCreateSpec)
     */
    default MessageCreateMono createMessage(String message) {
        return MessageCreateMono.of(this).withContent(message);
    }

    /**
     * Requests to create a message with embeds. Other properties specifying how to create the message can be set via
     * the {@code withXxx} methods of the returned {@link MessageCreateMono}.
     *
     * @param embeds immutable objects that specify how to create the embeds
     * @return A {@link MessageCreateMono} where, upon successful completion, emits the created {@link Message}. If an
     * error is received, it is emitted through the {@code MessageCreateMono}.
     * @see #createMessage(MessageCreateSpec)
     */
    default MessageCreateMono createMessage(EmbedCreateSpec... embeds) {
        return MessageCreateMono.of(this).withEmbeds(embeds);
    }

    /**
     * Requests to create a message with an embed.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyEmbedCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createEmbed(EmbedCreateSpec)} which offers an immutable approach to build specs
     */
    @Deprecated
    default Mono<Message> createEmbed(final Consumer<? super LegacyEmbedCreateSpec> spec) {
        return createMessage(messageSpec -> messageSpec.setEmbed(spec));
    }

    /**
     * Requests to create a message with an embed. Other properties specifying how to create the message can be set via
     * the {@code withXxx} methods of the returned {@link MessageCreateMono}.
     *
     * @param embed an immutable object that specifies how to create the embed
     * @return A {@link MessageCreateMono} where, upon successful completion, emits the created {@link Message}. If an
     * error is received, it is emitted through the {@code MessageCreateMono}.
     * @see #createMessage(MessageCreateSpec)
     * @deprecated Use {@link #createMessage(EmbedCreateSpec...)}.
     */
    @Deprecated
    default MessageCreateMono createEmbed(EmbedCreateSpec embed) {
        return MessageCreateMono.of(this).withEmbeds(embed);
    }

    /**
     * Requests to create a message.
     *
     * @param spec an immutable object that specifies how to create the poll
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @see PollCreateSpec#builder()
     */
    default Mono<Poll> createPoll(PollCreateSpec spec) {
        Objects.requireNonNull(spec);
        return createMessage()
            .withPoll(spec.asRequest())
            .map(Message::getPoll)
            .flatMap(Mono::justOrEmpty);
    }

    /**
     * Requests to create a poll.
     *
     * @return A {@link PollCreateMono} where, upon successful completion, emits the created {@link Message}. If an
     * error is received, it is emitted through the {@code PollCreateMono}.
     */
    default PollCreateMono createPoll() {
        return PollCreateMono.of(this);
    }

    /**
     * Requests to trigger the typing indicator in this channel. A single invocation of this method will trigger the
     * indicator for 10 seconds or until the bot sends a message in this channel.
     *
     * @return A {@link Mono} which completes upon successful triggering of the typing indicator in this channel. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Void> type() {
        return getClient().getRestClient().getChannelService()
                .triggerTypingIndicator(getId().asLong())
                .then();
    }

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
     * @implNote The default implementation actually sends a typing request every 8 seconds so it appears continuous.
     */
    default Flux<Long> typeUntil(Publisher<?> until) {
        Scheduler delayScheduler = getClient().getCoreResources().getReactorResources().getTimerTaskScheduler();
        Flux<Long> repeatUntilOther = Flux.interval(Duration.ofSeconds(8L), delayScheduler) // 8 to avoid
                // choppiness
                .flatMap(tick -> type().thenReturn(tick + 1)) // add 1 to offset the separate type() request
                .takeUntilOther(until);

        // send the first typing indicator before subscribing to the other publisher to ensure that a message send will
        // cancel the indicator properly. #509
        return type()
                .thenReturn(0L) // start with tick 0
                .concatWith(repeatUntilOther);
    }

    /**
     * Requests to retrieve <i>all</i> messages <i>before</i> the specified ID.
     * <p>
     * The returned {@code Flux} will emit items in <i>reverse-</i>chronological order (newest to oldest). It is
     * recommended to limit the emitted items by invoking either {@link Flux#takeWhile(Predicate)} (to retrieve IDs
     * within a specified range) or {@link Flux#take(long)} (to retrieve a specific amount of IDs).
     * <p>
     * The following example will get <i>all</i> messages from {@code messageId} to {@code myOtherMessageId}: {@code
     * getMessagesBefore(messageId).takeWhile(message -> message.getId().compareTo(myOtherMessageId) >= 0)}
     *
     * @param messageId The ID of the <i>newest</i> message to retrieve. Use {@link Snowflake#of(Instant)} to retrieve a
     *                  time-based ID.
     * @return A {@link Flux} that continually emits <i>all</i> {@link Message messages} <i>before</i> the specified ID.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    default Flux<Message> getMessagesBefore(Snowflake messageId) {
        final Function<Map<String, Object>, Flux<MessageData>> doRequest = params ->
                getClient().getRestClient().getChannelService()
                        .getMessages(getId().asLong(), params);

        return PaginationUtil.paginateBefore(doRequest, data -> Snowflake.asLong(data.id()), messageId.asLong(), 100)
                .map(data -> new Message(getClient(), data));
    }

    /**
     * Requests to retrieve <i>all</i> messages <i>after</i> the specified ID.
     * <p>
     * The returned {@code Flux} will emit items in chronological order (oldest to newest). It is recommended to limit
     * the emitted items by invoking either {@link Flux#takeWhile(Predicate)} (to retrieve IDs within a specified range)
     * or {@link Flux#take(long)} (to retrieve a specific amount of IDs).
     * <p>
     * The following example will get <i>all</i> messages from {@code messageId} to {@code myOtherMessageId}: {@code
     * getMessagesAfter(messageId).takeWhile(message -> message.getId().compareTo(myOtherMessageId) <= 0)}
     *
     * @param messageId The ID of the <i>oldest</i> message to retrieve. Use {@link Snowflake#of(Instant)} to retrieve a
     *                  time-based ID.
     * @return A {@link Flux} that continually emits <i>all</i> {@link Message messages} <i>after</i> the specified ID.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    default Flux<Message> getMessagesAfter(Snowflake messageId) {
        final Function<Map<String, Object>, Flux<MessageData>> doRequest = params ->
                getClient().getRestClient().getChannelService()
                        .getMessages(getId().asLong(), params);

        return PaginationUtil.paginateAfter(doRequest, data -> Snowflake.asLong(data.id()), messageId.asLong(), 100)
                .map(data -> new Message(getClient(), data));
    }

    /**
     * Requests to retrieve the message as represented by the supplied ID.
     *
     * @param id The ID of the message.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Message> getMessageById(Snowflake id) {
        return getClient().getMessageById(getId(), id);
    }

    /**
     * Requests to retrieve the message as represented by the supplied ID, using the given retrieval strategy.
     *
     * @param id                The ID of the message.
     * @param retrievalStrategy the strategy to use to get the message
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Message> getMessageById(Snowflake id, EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getMessageById(getId(), id);
    }

    /**
     * Requests to retrieve all the pinned messages for this channel.
     *
     * @return A {@link Flux} that continually emits all the pinned messages for this channel. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    default Flux<Message> getPinnedMessages() {
        return getClient().getRestClient().getChannelService()
                .getPinnedMessages(getId().asLong())
                .map(data -> new Message(getClient(), data));
    }
}
