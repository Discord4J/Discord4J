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
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ChannelData;
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
import java.util.function.Function;

/** An internal implementation of {@link MessageChannel} designed to streamline inheritance. */
class BaseMessageChannel extends BaseChannel implements MessageChannel {

    /**
     * Constructs an {@code BaseMessageChannel} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    BaseMessageChannel(final GatewayDiscordClient gateway, final ChannelData data) {
        super(gateway, data);
    }

    @Override
    public final Optional<Snowflake> getLastMessageId() {
        return Possible.flatOpt(getData().lastMessageId()).map(Snowflake::of);
    }

    @Override
    public final Mono<Message> getLastMessage() {
        return Mono.justOrEmpty(getLastMessageId()).flatMap(id -> getClient().getMessageById(getId(), id));
    }

    @Override
    public Mono<Message> getLastMessage(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getLastMessageId())
                .flatMap(id -> getClient().withRetrievalStrategy(retrievalStrategy).getMessageById(getId(), id));
    }

    @Override
    public final Optional<Instant> getLastPinTimestamp() {
        return Possible.flatOpt(getData().lastPinTimestamp())
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    @Override
    public Mono<Message> createMessage(MessageCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> {
                    MessageCreateSpec actualSpec = getClient().getRestClient()
                            .getRestResources()
                            .getAllowedMentions()
                            .map(spec::withAllowedMentions)
                            .orElse(spec);
                    return getRestChannel().createMessage(actualSpec.asRequest());
                })
                .map(data -> new Message(getClient(), data));
    }

    @Override
    public final Mono<Void> type() {
        return getClient().getRestClient().getChannelService()
                .triggerTypingIndicator(getId().asLong())
                .then();
    }

    @Override
    public final Flux<Long> typeUntil(final Publisher<?> until) {
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

    @Override
    public final Flux<Message> getMessagesBefore(final Snowflake messageId) {
        final Function<Map<String, Object>, Flux<MessageData>> doRequest = params ->
                getClient().getRestClient().getChannelService()
                        .getMessages(getId().asLong(), params);

        return PaginationUtil.paginateBefore(doRequest, data -> Snowflake.asLong(data.id()), messageId.asLong(), 100)
                .map(data -> new Message(getClient(), data));
    }

    @Override
    public final Flux<Message> getMessagesAfter(final Snowflake messageId) {
        final Function<Map<String, Object>, Flux<MessageData>> doRequest = params ->
                getClient().getRestClient().getChannelService()
                        .getMessages(getId().asLong(), params);

        return PaginationUtil.paginateAfter(doRequest, data -> Snowflake.asLong(data.id()), messageId.asLong(), 100)
                .map(data -> new Message(getClient(), data));
    }

    @Override
    public final Mono<Message> getMessageById(final Snowflake id) {
        return getClient().getMessageById(getId(), id);
    }

    @Override
    public Mono<Message> getMessageById(Snowflake id, EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getMessageById(getId(), id);
    }

    @Override
    public final Flux<Message> getPinnedMessages() {
        return getClient().getRestClient().getChannelService()
                .getPinnedMessages(getId().asLong())
                .map(data -> new Message(getClient(), data));
    }

    @Override
    public String toString() {
        return "BaseMessageChannel{} " + super.toString();
    }
}
