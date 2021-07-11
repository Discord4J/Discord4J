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
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Webhook;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.core.spec.legacy.LegacyMessageCreateSpec;
import discord4j.core.spec.legacy.LegacyWebhookCreateSpec;
import discord4j.discordjson.json.ChannelData;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

class BaseTopLevelGuildMessageChannel extends BaseCategorizableChannel implements TopLevelGuildMessageChannel {

    private final BaseGuildMessageChannel guildMessageChannel;

    BaseTopLevelGuildMessageChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
        this.guildMessageChannel = new BaseGuildMessageChannel(gateway, data);
    }

    @Override
    public Flux<Snowflake> bulkDelete(Publisher<Snowflake> messageIds) {
        return guildMessageChannel.bulkDelete(messageIds);
    }

    @Override
    public Flux<Message> bulkDeleteMessages(Publisher<Message> messages) {
        return guildMessageChannel.bulkDeleteMessages(messages);
    }

    @Override
    public Flux<Member> getMembers() {
        return guildMessageChannel.getMembers();
    }

    @Override
    public Optional<Snowflake> getLastMessageId() {
        return guildMessageChannel.getLastMessageId();
    }

    @Override
    public Mono<Message> getLastMessage() {
        return guildMessageChannel.getLastMessage();
    }

    @Override
    public Mono<Message> getLastMessage(EntityRetrievalStrategy retrievalStrategy) {
        return guildMessageChannel.getLastMessage(retrievalStrategy);
    }

    @Override
    public Optional<Instant> getLastPinTimestamp() {
        return guildMessageChannel.getLastPinTimestamp();
    }

    @Override
    public Mono<Message> createMessage(Consumer<? super LegacyMessageCreateSpec> spec) {
        return guildMessageChannel.createMessage(spec);
    }

    @Override
    public Mono<Message> createMessage(MessageCreateSpec spec) {
        return null;
    }

    @Override
    public Mono<Void> type() {
        return guildMessageChannel.type();
    }

    @Override
    public Flux<Long> typeUntil(Publisher<?> until) {
        return guildMessageChannel.typeUntil(until);
    }

    @Override
    public Flux<Message> getMessagesBefore(Snowflake messageId) {
        return guildMessageChannel.getMessagesBefore(messageId);
    }

    @Override
    public Flux<Message> getMessagesAfter(Snowflake messageId) {
        return guildMessageChannel.getMessagesAfter(messageId);
    }

    @Override
    public Mono<Message> getMessageById(Snowflake id) {
        return guildMessageChannel.getMessageById(id);
    }

    @Override
    public Mono<Message> getMessageById(Snowflake id, EntityRetrievalStrategy retrievalStrategy) {
        return guildMessageChannel.getMessageById(id, retrievalStrategy);
    }

    @Override
    public Flux<Message> getPinnedMessages() {
        return guildMessageChannel.getPinnedMessages();
    }

    @Override
    public Optional<String> getTopic() {
        return guildMessageChannel.getTopic();
    }

    @Override
    public Mono<Webhook> createWebhook(Consumer<? super LegacyWebhookCreateSpec> spec) {
        return guildMessageChannel.createWebhook(spec);
    }

    @Override
    public Mono<Webhook> createWebhook(WebhookCreateSpec spec) {
        return guildMessageChannel.createWebhook(spec);
    }

    @Override
    public Flux<Webhook> getWebhooks() {
        return guildMessageChannel.getWebhooks();
    }
}
