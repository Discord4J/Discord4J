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

import discord4j.common.json.MessageResponse;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.data.stored.ChannelBean;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.util.PaginationUtil;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/** An internal implementation of {@link MessageChannel} designed to streamline inheritance. */
class BaseMessageChannel extends BaseChannel implements MessageChannel {

    /**
     * Constructs an {@code BaseMessageChannel} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    BaseMessageChannel(final GatewayDiscordClient gateway, final ChannelBean data) {
        super(gateway, data);
    }

    @Override
    public final Optional<Snowflake> getLastMessageId() {
        return Optional.ofNullable(getData().getLastMessageId()).map(Snowflake::of);
    }

    @Override
    public final Mono<Message> getLastMessage() {
        return Mono.justOrEmpty(getLastMessageId()).flatMap(id -> getClient().getMessageById(getId(), id));
    }

    @Override
    public final Optional<Instant> getLastPinTimestamp() {
        return Optional.ofNullable(getData().getLastPinTimestamp())
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    @Override
    public final Mono<Message> createMessage(final Consumer<? super MessageCreateSpec> spec) {
        final MessageCreateSpec mutatedSpec = new MessageCreateSpec();
        spec.accept(mutatedSpec);

        return getRestChannel().createMessage(mutatedSpec.asRequest())
                .map(MessageBean::new)
                .map(bean -> new Message(getClient(), bean));
    }

    @Override
    public final Mono<Void> type() {
        return getClient().getRestClient().getChannelService()
                .triggerTypingIndicator(getId().asLong())
                .then();
    }

    @Override
    public final Flux<Long> typeUntil(final Publisher<?> until) {
        Flux<Long> repeatUntilOther = Flux.interval(Duration.ofSeconds(8L), Schedulers.elastic()) // 8 to avoid choppiness
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
        final Function<Map<String, Object>, Flux<MessageResponse>> doRequest = params ->
                getClient().getRestClient().getChannelService()
                        .getMessages(getId().asLong(), params);

        return PaginationUtil.paginateBefore(doRequest, MessageResponse::getId, messageId.asLong(), 100)
                .map(MessageBean::new)
                .map(bean -> new Message(getClient(), bean));
    }

    @Override
    public final Flux<Message> getMessagesAfter(final Snowflake messageId) {
        final Function<Map<String, Object>, Flux<MessageResponse>> doRequest = params ->
                getClient().getRestClient().getChannelService()
                        .getMessages(getId().asLong(), params);

        return PaginationUtil.paginateAfter(doRequest, MessageResponse::getId, messageId.asLong(), 100)
                .map(MessageBean::new)
                .map(bean -> new Message(getClient(), bean));
    }

    @Override
    public final Mono<Message> getMessageById(final Snowflake id) {
        return getClient().getMessageById(getId(), id);
    }

    @Override
    public final Flux<Message> getPinnedMessages() {
        return getClient().getRestClient().getChannelService()
                .getPinnedMessages(getId().asLong())
                .map(MessageBean::new)
                .map(bean -> new Message(getClient(), bean));
    }

    @Override
    public String toString() {
        return "BaseMessageChannel{} " + super.toString();
    }
}
