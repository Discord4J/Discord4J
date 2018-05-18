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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.event.dispatch;

import discord4j.common.jackson.Possible;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.*;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.entity.Message;
import discord4j.core.util.ArrayUtil;
import discord4j.gateway.json.dispatch.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class MessageDispatchHandlers {

    static Mono<MessageCreateEvent> messageCreate(DispatchContext<MessageCreate> context) {
        DiscordClient client = context.getServiceMediator().getClient();
        MessageBean bean = new MessageBean(context.getDispatch());
        Message message = new Message(context.getServiceMediator(), bean);

        Mono<Void> saveMessage = context.getServiceMediator().getStoreHolder().getMessageStore()
                .save(bean.getId(), bean);

        return saveMessage
                .thenReturn(new MessageCreateEvent(client, message));
    }

    static Mono<MessageDeleteEvent> messageDelete(DispatchContext<MessageDelete> context) {
        DiscordClient client = context.getServiceMediator().getClient();
        long messageId = context.getDispatch().getId();
        long channelId = context.getDispatch().getChannelId();

        Mono<Void> deleteMessage = context.getServiceMediator().getStoreHolder().getMessageStore()
                .delete(context.getDispatch().getId());

        return deleteMessage
                .thenReturn(new MessageDeleteEvent(client, messageId, channelId));
    }

    static Mono<MessageBulkDeleteEvent> messageDeleteBulk(DispatchContext<MessageDeleteBulk> context) {
        DiscordClient client = context.getServiceMediator().getClient();
        long messageIds[] = context.getDispatch().getIds();
        long channelId = context.getDispatch().getChannelId();
        long guildId = context.getDispatch().getGuildId();

        Mono<Void> deleteMessages = context.getServiceMediator().getStoreHolder().getMessageStore()
                .delete(Flux.fromArray(ArrayUtil.toObject(context.getDispatch().getIds())));

        return deleteMessages
                .thenReturn(new MessageBulkDeleteEvent(client, messageIds, channelId, guildId));
    }

    static Mono<ReactionAddEvent> messageReactionAdd(DispatchContext<MessageReactionAdd> context) {
        // TODO need MessageBean#reactions and GuildEmoji | Unicode type
        return Mono.empty();
    }

    static Mono<ReactionRemoveEvent> messageReactionRemove(DispatchContext<MessageReactionRemove> context) {
        // TODO need MessageBean#reactions and GuildEmoji | Unicode type
        return Mono.empty();
    }

    static Mono<ReactionRemoveAllEvent> messageReactionRemoveAll(DispatchContext<MessageReactionRemoveAll>
                                                                         context) {
        // TODO need MessageBean#reactions and GuildEmoji | Unicode type
        return Mono.empty();
    }

    static Mono<MessageUpdateEvent> messageUpdate(DispatchContext<MessageUpdate> context) {
        DiscordClient client = context.getServiceMediator().getClient();

        long messageId = context.getDispatch().getId();
        long channelId = context.getDispatch().getChannelId();
        long guildId = context.getDispatch().getGuildId();

        Possible<String> content = context.getDispatch().getContent();
        boolean contentChanged = content == null || !content.isAbsent();
        String currentContent = content == null || content.isAbsent() ? null : content.get();

        Mono<MessageUpdateEvent> update = context.getServiceMediator().getStoreHolder().getMessageStore()
                .find(messageId)
                .flatMap(oldBean -> {
                    // updating the content and embed of the bean in the store
                    Message old = new Message(context.getServiceMediator(), oldBean);
                    MessageBean newBean = new MessageBean(oldBean);
                    newBean.setContent(currentContent);

                    // TODO embed

                    return context.getServiceMediator().getStoreHolder().getMessageStore()
                            .save(newBean.getId(), newBean)
                            .thenReturn(new MessageUpdateEvent(client, messageId, channelId, guildId, old,
                                    contentChanged, newBean.getContent()));
                });

        return update
                .defaultIfEmpty(new MessageUpdateEvent(client, messageId, channelId, guildId, null, contentChanged,
                        currentContent));
    }
}
