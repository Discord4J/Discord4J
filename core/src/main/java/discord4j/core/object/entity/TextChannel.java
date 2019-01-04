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
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.data.ExtendedInviteBean;
import discord4j.core.object.data.WebhookBean;
import discord4j.core.object.data.stored.TextChannelBean;
import discord4j.core.object.trait.Categorizable;
import discord4j.core.object.trait.Invitable;
import discord4j.core.object.util.PermissionSet;
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
public final class TextChannel extends BaseChannel implements Categorizable, GuildChannel, Invitable, MessageChannel {

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
    public Set<ExtendedPermissionOverwrite> getPermissionOverwrites() {
        return guildChannel.getPermissionOverwrites();
    }

    @Override
    public Optional<ExtendedPermissionOverwrite> getOverwriteForMember(Snowflake memberId) {
        return guildChannel.getOverwriteForMember(memberId);
    }

    @Override
    public Optional<ExtendedPermissionOverwrite> getOverwriteForRole(Snowflake roleId) {
        return guildChannel.getOverwriteForRole(roleId);
    }

    @Override
    public Mono<PermissionSet> getEffectivePermissions(Snowflake memberId) {
        return guildChannel.getEffectivePermissions(memberId);
    }

    @Override
    public String getName() {
        return guildChannel.getName();
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
    public Mono<Void> addMemberOverwrite(Snowflake memberId, PermissionOverwrite overwrite) {
        return guildChannel.addMemberOverwrite(memberId, overwrite);
    }

    @Override
    public Mono<Void> addRoleOverwrite(Snowflake roleId, PermissionOverwrite overwrite) {
        return guildChannel.addRoleOverwrite(roleId, overwrite);
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
    public Mono<Message> getMessageById(final Snowflake id) {
        return messageChannel.getMessageById(id);
    }

    @Override
    public Flux<Message> getPinnedMessages() {
        return messageChannel.getPinnedMessages();
    }

    @Override
    TextChannelBean getData() {
        return (TextChannelBean) super.getData();
    }

    @Override
    public Optional<Snowflake> getCategoryId() {
        return Optional.ofNullable(getData().getGuildChannel().getParentId()).map(Snowflake::of);
    }

    @Override
    public Mono<Category> getCategory() {
        return Mono.justOrEmpty(getCategoryId()).flatMap(getClient()::getChannelById).cast(Category.class);
    }

    @Override
    public Mono<ExtendedInvite> createInvite(final InviteCreateSpec spec) {
        return getServiceMediator().getRestClient().getChannelService()
                .createChannelInvite(getId().asLong(), spec.asRequest())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean));
    }

    @Override
    public Flux<ExtendedInvite> getInvites() {
        return getServiceMediator().getRestClient().getChannelService()
                .getChannelInvites(getId().asLong())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean));
    }

    /**
     * Gets the channel topic, if present
     *
     * @return The channel topic, if present.
     */
    public Optional<String> getTopic() {
        return Optional.ofNullable(getData().getTopic());
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
     * Gets the amount of seconds an user has to wait before sending another message (0-120).
     * <p>
     * Bots, as well as users with the permission {@code manage_messages} or {@code manage_channel}, are unaffected.
     *
     * @return The amount of seconds an user has to wait before sending another message (0-120).
     */
    public int getRateLimitPerUser() {
        return getData().getRateLimitPerUser();
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

    @Override
    public String toString() {
        return "TextChannel{" +
                "guildChannel=" + guildChannel +
                ", messageChannel=" + messageChannel +
                "} " + super.toString();
    }
}
