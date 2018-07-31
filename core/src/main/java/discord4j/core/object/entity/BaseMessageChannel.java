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

import discord4j.common.json.MessageResponse;
import discord4j.core.ServiceMediator;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.data.stored.MessageChannelBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.util.PaginationUtil;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    BaseMessageChannel(final ServiceMediator serviceMediator, final MessageChannelBean data) {
        super(serviceMediator, data);
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
    public final Mono<Message> createMessage(final Consumer<MessageCreateSpec> spec) {
        final MessageCreateSpec mutatedSpec = new MessageCreateSpec();
        spec.accept(mutatedSpec);
        return createMessage(mutatedSpec);
    }

    @Override
    public final Mono<Message> createMessage(final MessageCreateSpec spec) {
        return getServiceMediator().getRestClient().getChannelService()
                .createMessage(getId().asLong(), spec.asRequest())
                .map(MessageBean::new)
                .map(bean -> new Message(getServiceMediator(), bean));
    }

    @Override
    public final Mono<Void> type() {
        return getServiceMediator().getRestClient().getChannelService()
                .triggerTypingIndicator(getId().asLong())
                .then();
    }

    @Override
    public final Flux<Long> typeUntil(final Publisher<?> until) {
        return Flux.interval(Duration.ZERO, Duration.ofSeconds(10L))
                .flatMap(l -> type().thenReturn(l))
                .takeUntilOther(until);
    }

    @Override
    public final Flux<Message> getMessagesBefore(final Snowflake messageId) {
        final Function<Map<String, Object>, Flux<MessageResponse>> doRequest = params ->
                getServiceMediator().getRestClient().getChannelService().getMessages(getId().asLong(), params);

        return PaginationUtil.paginateBefore(doRequest, MessageResponse::getId, messageId.asLong(), 100)
                .map(MessageBean::new)
                .map(bean -> new Message(getServiceMediator(), bean));
    }

    @Override
    public final Flux<Message> getMessagesAfter(final Snowflake messageId) {
        final Function<Map<String, Object>, Flux<MessageResponse>> doRequest = params ->
                getServiceMediator().getRestClient().getChannelService().getMessages(getId().asLong(), params);

        return PaginationUtil.paginateAfter(doRequest, MessageResponse::getId, messageId.asLong(), 100)
                .map(MessageBean::new)
                .map(bean -> new Message(getServiceMediator(), bean));
    }

    @Override
    public Flux<Message> getPinnedMessages() {
        return getServiceMediator().getRestClient().getChannelService().getPinnedMessages(getId().asLong())
                .map(MessageBean::new)
                .map(bean -> new Message(getServiceMediator(), bean));
    }

    @Override
    MessageChannelBean getData() {
        return (MessageChannelBean) super.getData();
    }
}
