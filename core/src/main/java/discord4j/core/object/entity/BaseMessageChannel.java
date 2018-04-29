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

import discord4j.core.ServiceMediator;
import discord4j.core.object.entity.bean.MessageBean;
import discord4j.core.object.entity.bean.MessageChannelBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

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
        return Optional.ofNullable(getData().getLastPinTimestamp()).map(Instant::parse);
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
    protected MessageChannelBean getData() {
        return (MessageChannelBean) super.getData();
    }
}
