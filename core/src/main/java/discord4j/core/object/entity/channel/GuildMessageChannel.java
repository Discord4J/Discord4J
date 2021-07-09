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

import discord4j.core.object.ExtendedInvite;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Webhook;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.InviteCreateMono;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.core.spec.WebhookCreateMono;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.core.spec.legacy.LegacyInviteCreateSpec;
import discord4j.core.spec.legacy.LegacyWebhookCreateSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

public interface GuildMessageChannel extends CategorizableChannel, MessageChannel {

    /**
     * Gets the channel topic, if present.
     *
     * @return The channel topic, if present.
     */
    @Deprecated
    Optional<String> getTopic();

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
    Flux<Snowflake> bulkDelete(Publisher<Snowflake> messageIds);

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
    Flux<Message> bulkDeleteMessages(Publisher<Message> messages);

    /**
     * Requests to create a webhook.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyWebhookCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createWebhook(WebhookCreateSpec)} or {@link #createWebhook(String)} which offer an
     * immutable approach to build specs
     */
    @Deprecated
    Mono<Webhook> createWebhook(final Consumer<? super LegacyWebhookCreateSpec> spec);

    /**
     * Requests to create a webhook. Properties specifying how to create the webhook can be set via the {@code withXxx}
     * methods of the returned {@link WebhookCreateMono}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    @Deprecated
    default WebhookCreateMono createWebhook(String name) {
        return WebhookCreateMono.of(name, this);
    }

    /**
     * Requests to create a webhook.
     *
     * @param spec an immutable object that specifies how to create the webhook
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    Mono<Webhook> createWebhook(WebhookCreateSpec spec);

    /**
     * Requests to retrieve the webhooks of the channel.
     *
     * @return A {@link Flux} that continually emits the {@link Webhook webhooks} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    @Deprecated
    Flux<Webhook> getWebhooks();

    /**
     * Returns all members in the guild which have access to <b>view</b> this channel.
     *
     * @return A {@link Flux} that continually emits all members from {@link Guild#getMembers()} which have access to
     * view this channel {@link discord4j.rest.util.Permission#VIEW_CHANNEL}
     */
    Flux<Member> getMembers();

    @Override
    @Deprecated
    Optional<Snowflake> getCategoryId();

    @Override
    @Deprecated
    Mono<Category> getCategory();

    @Override
    @Deprecated
    Mono<Category> getCategory(EntityRetrievalStrategy retrievalStrategy);

    @Override
    @Deprecated
    Mono<ExtendedInvite> createInvite(final Consumer<? super LegacyInviteCreateSpec> spec);

    @Override
    @Deprecated
    default InviteCreateMono createInvite() {
        return CategorizableChannel.super.createInvite();
    }
    @Override
    @Deprecated
    Mono<ExtendedInvite> createInvite(InviteCreateSpec spec);

    @Override
    @Deprecated
    Flux<ExtendedInvite> getInvites();
}
