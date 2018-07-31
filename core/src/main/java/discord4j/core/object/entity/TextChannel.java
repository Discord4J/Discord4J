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
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.data.ExtendedInviteBean;
import discord4j.core.object.data.WebhookBean;
import discord4j.core.object.data.stored.TextChannelBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.TextChannelEditSpec;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.core.util.EntityUtil;
import discord4j.rest.json.request.BulkDeleteRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** A Discord text channel. */
public final class TextChannel extends BaseChannel implements GuildChannel, MessageChannel {

    /** Delegates {@link GuildChannel} operations. */
    private final BaseGuildChannel guildChannel;

    /** Delegates {@link MessageChannel} operations. */
    private final BaseMessageChannel messageChannel;

    /**
     * Constructs an {@code TextChannel} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public TextChannel(final ServiceMediator serviceMediator, final TextChannelBean data) {
        super(serviceMediator, data);
        guildChannel = new BaseGuildChannel(serviceMediator, data.getGuildChannel());
        messageChannel = new BaseMessageChannel(serviceMediator, data.getMessageChannel());
    }

    @Override
    public Snowflake getGuildId() {
        return guildChannel.getGuildId();
    }

    @Override
    public Mono<Guild> getGuild() {
        return guildChannel.getGuild();
    }

    @Override
    public Set<PermissionOverwrite> getPermissionOverwrites() {
        return guildChannel.getPermissionOverwrites();
    }

    @Override
    public String getName() {
        return guildChannel.getName();
    }

    @Override
    public Optional<Snowflake> getCategoryId() {
        return guildChannel.getCategoryId();
    }

    @Override
    public Mono<Category> getCategory() {
        return guildChannel.getCategory();
    }

    @Override
    public int getRawPosition() {
        return guildChannel.getRawPosition();
    }

    @Override
    public Mono<Integer> getPosition() {
        return guildChannel.getPosition();
    }

    @Override
    public Optional<Snowflake> getLastMessageId() {
        return messageChannel.getLastMessageId();
    }

    @Override
    public Mono<Message> getLastMessage() {
        return messageChannel.getLastMessage();
    }

    @Override
    public Optional<Instant> getLastPinTimestamp() {
        return messageChannel.getLastPinTimestamp();
    }

    @Override
    public Mono<Message> createMessage(final Consumer<MessageCreateSpec> spec) {
        return messageChannel.createMessage(spec);
    }

    @Override
    public Mono<Message> createMessage(final MessageCreateSpec spec) {
        return messageChannel.createMessage(spec);
    }

    @Override
    public Mono<Void> type() {
        return messageChannel.type();
    }

    @Override
    public Flux<Long> typeUntil(final Publisher<?> until) {
        return messageChannel.typeUntil(until);
    }

    @Override
    public Flux<Message> getMessagesBefore(final Snowflake messageId) {
        return messageChannel.getMessagesBefore(messageId);
    }

    @Override
    public Flux<Message> getMessagesAfter(final Snowflake messageId) {
        return messageChannel.getMessagesAfter(messageId);
    }

    @Override
    public Flux<Message> getPinnedMessages() {
        return messageChannel.getPinnedMessages();
    }

    /**
     * Gets the channel topic.
     *
     * @return The channel topic.
     */
    public String getTopic() {
        return Optional.ofNullable(getData().getTopic()).orElse("");
    }

    @Override
    TextChannelBean getData() {
        return (TextChannelBean) super.getData();
    }

    /**
     * Gets whether this channel is considered NSFW (Not Safe For Work).
     *
     * @return {@code true} if this channel is considered NSFW (Not Safe For Work), {@code false} otherwise.
     */
    public boolean isNsfw() {
        return getData().isNsfw();
    }

    /**
     * Gets the <i>raw</i> mention. This is the format utilized to directly mention another text channel (assuming the
     * text channel exists in context of the mention).
     *
     * @return The <i>raw</i> mention.
     */
    public String getMention() {
        return "<#" + getId().asString() + ">";
    }

    /**
     * Requests to edit this text channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link TextChannelEditSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #edit(TextChannelEditSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> edit(final Consumer<TextChannelEditSpec> spec) {
        final TextChannelEditSpec mutatedSpec = new TextChannelEditSpec();
        spec.accept(mutatedSpec);
        return edit(mutatedSpec);
    }

    /**
     * Requests to edit this text channel.
     *
     * @param spec A configured {@link TextChannelEditSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> edit(final TextChannelEditSpec spec) {
        return getServiceMediator().getRestClient().getChannelService()
                .modifyChannel(getId().asLong(), spec.asRequest())
                .map(EntityUtil::getChannelBean)
                .map(bean -> EntityUtil.getChannel(getServiceMediator(), bean))
                .cast(TextChannel.class);
    }

    /**
     * Requests to create an invite.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link InviteCreateSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #createInvite(InviteCreateSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ExtendedInvite}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<ExtendedInvite> createInvite(final Consumer<InviteCreateSpec> spec) {
        final InviteCreateSpec mutatedSpec = new InviteCreateSpec();
        spec.accept(mutatedSpec);
        return createInvite(mutatedSpec);
    }

    /**
     * Requests to create an invite.
     *
     * @param spec A configured {@link InviteCreateSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ExtendedInvite}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<ExtendedInvite> createInvite(final InviteCreateSpec spec) {
        return getServiceMediator().getRestClient().getChannelService()
                .createChannelInvite(getId().asLong(), spec.asRequest())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean));
    }

    /**
     * Requests to bulk delete the supplied message IDs.
     *
     * @param messageIds A {@link Publisher} to supply the message IDs to bulk delete.
     * @return A {@link Flux} that continually emits {@link Snowflake message IDs} that were <b>not</b> bulk deleted
     * (typically if the ID was older than 2 weeks). If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<Snowflake> bulkDelete(final Publisher<Snowflake> messageIds) {
        final Instant timeLimit = Instant.now().minus(Duration.ofDays(14L));
        final Collection<Snowflake> ignoredMessageIds = new ArrayList<>(0);

        final Predicate<Snowflake> filterMessageId = messageId -> {
            if (timeLimit.isAfter(messageId.getTimestamp())) { // REST accepts 2 week old IDs
                ignoredMessageIds.add(messageId);
                return false;
            }

            return true;
        };

        final Predicate<List<String>> filterMessageIdChunk = messageIdChunk -> {
            if (messageIdChunk.size() == 1) { // REST accepts 2 or more items
                ignoredMessageIds.add(Snowflake.of(messageIdChunk.get(0)));
                return false;
            }

            return !messageIdChunk.isEmpty();
        };

        return Flux.defer(() -> messageIds).distinct()
                .filter(filterMessageId)
                .map(Snowflake::asString)
                .buffer(100) // REST accepts 100 IDs
                .filter(filterMessageIdChunk)
                .map(messageIdChunk -> messageIdChunk.toArray(new String[messageIdChunk.size()]))
                .flatMap(messageIdChunk -> getServiceMediator().getRestClient().getChannelService()
                        .bulkDeleteMessages(getId().asLong(), new BulkDeleteRequest(messageIdChunk)))
                .thenMany(Flux.fromIterable(ignoredMessageIds));
    }

    /**
     * Requests to create a webhook.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link WebhookCreateSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #createWebhook(WebhookCreateSpec)}.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> createWebhook(final Consumer<WebhookCreateSpec> spec) {
        final WebhookCreateSpec mutatedSpec = new WebhookCreateSpec();
        spec.accept(mutatedSpec);
        return createWebhook(mutatedSpec);
    }

    /**
     * Requests to create a webhook.
     *
     * @param spec A configured {@link WebhookCreateSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> createWebhook(final WebhookCreateSpec spec) {
        return getServiceMediator().getRestClient().getWebhookService()
                .createWebhook(getId().asLong(), spec.asRequest())
                .map(WebhookBean::new)
                .map(bean -> new Webhook(getServiceMediator(), bean));
    }

    /**
     * Requests to retrieve the webhooks of the channel.
     *
     * @return A {@link Flux} that continually emits the {@link Webhook webhooks} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<Webhook> getWebhooks() {
        return getServiceMediator().getRestClient().getWebhookService()
                .getChannelWebhooks(getId().asLong())
                .map(WebhookBean::new)
                .map(bean -> new Webhook(getServiceMediator(), bean));
    }
}
