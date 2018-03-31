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

import discord4j.common.json.payload.dispatch.*;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.*;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.bean.MessageBean;
import discord4j.core.util.ArrayUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class MessageDispatchHandlers {

    static Flux<MessageCreateEvent> messageCreate(DispatchContext<MessageCreate> context) {
        DiscordClient client = context.getServiceMediator().getDiscordClient();
        MessageBean bean = new MessageBean(context.getDispatch().getMessage());
        Message message = new Message(context.getServiceMediator(), bean);

        Mono<Void> saveMessage = context.getServiceMediator().getStoreHolder().getMessageStore()
                .save(bean.getId(), bean);

        return saveMessage
                .thenReturn(new MessageCreateEvent(client, message))
                .flux();
    }

    static Flux<MessageDeleteEvent> messageDelete(DispatchContext<MessageDelete> context) {
        DiscordClient client = context.getServiceMediator().getDiscordClient();
        long messageId = context.getDispatch().getId();
        long channelId = context.getDispatch().getChannelId();

        Mono<Void> deleteMessage = context.getServiceMediator().getStoreHolder().getMessageStore()
                .delete(context.getDispatch().getId());

        return deleteMessage
                .thenReturn(new MessageDeleteEvent(client, messageId, channelId))
                .flux();
    }

    static Flux<MessageBulkDeleteEvent> messageDeleteBulk(DispatchContext<MessageDeleteBulk> context) {
        DiscordClient client = context.getServiceMediator().getDiscordClient();
        long messageIds[] = context.getDispatch().getIds();
        long channelId = context.getDispatch().getChannelId();

        Mono<Void> deleteMessages = context.getServiceMediator().getStoreHolder().getMessageStore()
                .delete(Flux.fromArray(ArrayUtil.toObject(context.getDispatch().getIds())));

        return deleteMessages
                .thenReturn(new MessageBulkDeleteEvent(client, messageIds, channelId))
                .flux();
    }

    static Flux<ReactionAddEvent> messageReactionAdd(DispatchContext<MessageReactionAdd> context) {
        // TODO need MessageBean#reactions and GuildEmoji | Unicode type
        return Flux.empty();
    }

    static Flux<ReactionRemoveEvent> messageReactionRemove(DispatchContext<MessageReactionRemove> context) {
        // TODO need MessageBean#reactions and GuildEmoji | Unicode type
        return Flux.empty();
    }

    static Flux<ReactionRemoveAllEvent> messageReactionRemoveAll(DispatchContext<MessageReactionRemoveAll>
                                                                         context) {
        // TODO need MessageBean#reactions and GuildEmoji | Unicode type
        return Flux.empty();
    }

    static Flux<MessageUpdateEvent> messageUpdate(DispatchContext<MessageUpdate> context) {
        DiscordClient client = context.getServiceMediator().getDiscordClient();
        MessageBean bean = new MessageBean(context.getDispatch().getMessage());
        Message current = new Message(context.getServiceMediator(), bean);

        Mono<Void> saveNew = context.getServiceMediator().getStoreHolder().getMessageStore()
                .save(bean.getId(), bean);

        return context.getServiceMediator().getStoreHolder().getMessageStore()
                .find(bean.getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new MessageUpdateEvent(client, current, new Message(context.getServiceMediator(), old)))
                .switchIfEmpty(saveNew.thenReturn(new MessageUpdateEvent(client, current, null)))
                .flux();
    }
}
