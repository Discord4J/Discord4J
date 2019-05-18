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

import discord4j.core.ServiceMediator;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.data.ExtendedInviteBean;
import discord4j.core.object.data.WebhookBean;
import discord4j.core.object.data.stored.ChannelBean;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Webhook;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.rest.json.request.BulkDeleteRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** An internal implementation of {@link GuildMessageChannel} designed to streamline inheritance. */
class BaseGuildMessageChannel extends BaseChannel implements GuildMessageChannel {

    /** Delegates {@link GuildChannel} operations. */
    private final BaseGuildChannel guildChannel;

    /** Delegates {@link MessageChannel} operations. */
    private final BaseMessageChannel messageChannel;

    /** Delegates {@link CategorizableInvitableChannel} operations. */
    private final BaseCategorizableInvitableChannel categorizableInvitableChannel;

    /**
     * Constructs an {@code BaseGuildMessageChannel} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    BaseGuildMessageChannel(final ServiceMediator serviceMediator, final ChannelBean data) {
        super(serviceMediator, data);
        guildChannel = new BaseGuildChannel(serviceMediator, data);
        messageChannel = new BaseMessageChannel(serviceMediator, data);
        categorizableInvitableChannel = new BaseCategorizableInvitableChannel(serviceMediator, data);
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
    public Mono<Void> addMemberOverwrite(Snowflake memberId, PermissionOverwrite overwrite, @Nullable String reason) {
        return guildChannel.addMemberOverwrite(memberId, overwrite, reason);
    }

    @Override
    public Mono<Void> addRoleOverwrite(Snowflake roleId, PermissionOverwrite overwrite, @Nullable String reason) {
        return guildChannel.addRoleOverwrite(roleId, overwrite, reason);
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
    public Mono<Message> createMessage(final Consumer<? super MessageCreateSpec> spec) {
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
    public Optional<Snowflake> getCategoryId() {
        return categorizableInvitableChannel.getCategoryId();
    }

    @Override
    public Mono<Category> getCategory() {
        return categorizableInvitableChannel.getCategory();
    }

    @Override
    public Mono<ExtendedInvite> createInvite(final Consumer<? super InviteCreateSpec> spec) {
        return categorizableInvitableChannel.createInvite(spec);
    }

    @Override
    public Flux<ExtendedInvite> getInvites() {
        return categorizableInvitableChannel.getInvites();
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

        final Function<List<String>, Mono<Boolean>> filterMessageIdChunk = messageIdChunk ->
                Mono.just(messageIdChunk.get(0)) // REST accepts 2 or more items
                        .filter(ignore -> messageIdChunk.size() == 1)
                        .flatMap(id -> getServiceMediator().getRestClient().getChannelService()
                                .deleteMessage(getId().asLong(), Long.parseLong(id), null)
                                .thenReturn(id)
                                .subscriberContext(ctx -> ctx.put("shard",
                                        getServiceMediator().getClientConfig().getShardIndex())))
                        .hasElement()
                        .map(identity -> !identity);

        return Flux.defer(() -> messageIds)
                .distinct()
                .filter(filterMessageId)
                .map(Snowflake::asString)
                .buffer(100) // REST accepts 100 IDs
                .filterWhen(filterMessageIdChunk)
                .map(messageIdChunk -> messageIdChunk.toArray(new String[messageIdChunk.size()]))
                .flatMap(messageIdChunk -> getServiceMediator().getRestClient().getChannelService()
                        .bulkDeleteMessages(getId().asLong(), new BulkDeleteRequest(messageIdChunk))
                        .subscriberContext(ctx -> ctx.put("shard",
                                getServiceMediator().getClientConfig().getShardIndex())))
                .thenMany(Flux.fromIterable(ignoredMessageIds));
    }

    /**
     * Requests to create a webhook.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link WebhookCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> createWebhook(final Consumer<? super WebhookCreateSpec> spec) {
        final WebhookCreateSpec mutatedSpec = new WebhookCreateSpec();
        spec.accept(mutatedSpec);

        return getServiceMediator().getRestClient().getWebhookService()
                .createWebhook(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                .map(WebhookBean::new)
                .map(bean -> new Webhook(getServiceMediator(), bean))
                .subscriberContext(ctx -> ctx.put("shard", getServiceMediator().getClientConfig().getShardIndex()));
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
                .map(bean -> new Webhook(getServiceMediator(), bean))
                .subscriberContext(ctx -> ctx.put("shard", getServiceMediator().getClientConfig().getShardIndex()));
    }

    @Override
    public String toString() {
        return "GuildMessageChannel{" +
                "guildChannel=" + guildChannel +
                ", messageChannel=" + messageChannel +
                "} " + super.toString();
    }
}
