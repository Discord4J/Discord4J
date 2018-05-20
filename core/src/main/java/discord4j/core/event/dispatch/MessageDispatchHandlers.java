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
import discord4j.common.json.EmojiResponse;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.*;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.data.stored.ReactionBean;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.util.ArrayUtil;
import discord4j.gateway.json.dispatch.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

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
        DiscordClient client = context.getServiceMediator().getClient();

        Long emojiId = context.getDispatch().getEmoji().getId();
        String emojiName = context.getDispatch().getEmoji().getName();
        boolean emojiAnimated = context.getDispatch().getEmoji().getAnimated() != null
                && context.getDispatch().getEmoji().getAnimated();

        long userId = context.getDispatch().getUserId();
        long channelId = context.getDispatch().getChannelId();
        long messageId = context.getDispatch().getMessageId();

        Mono<Void> addToMessage = context.getServiceMediator().getStoreHolder().getMessageStore()
                .find(messageId)
                .doOnNext(bean -> {
                    boolean me = context.getServiceMediator().getStoreHolder().getSelfId().get() == userId;

                    if (bean.getReactions() == null) {
                        ReactionBean r = new ReactionBean(1, me, emojiId, emojiName, emojiAnimated);
                        bean.setReactions(new ReactionBean[] { r });
                    } else {
                        int i;
                        for (i = 0; i < bean.getReactions().length; i++) {
                            ReactionBean r = bean.getReactions()[i];
                            if (Objects.equals(r.getEmojiId(), emojiId) && r.getEmojiName().equals(emojiName)) {
                                break;
                            }
                        }

                        if (i < bean.getReactions().length) {
                            ReactionBean existing = bean.getReactions()[i];
                            existing.setMe(me);
                            existing.setCount(existing.getCount() + 1);
                        } else {
                            ReactionBean r = new ReactionBean(1, me, emojiId, emojiName, emojiAnimated);
                            bean.setReactions(ArrayUtil.add(bean.getReactions(), r));
                        }
                    }
                })
                .flatMap(bean ->
                        context.getServiceMediator().getStoreHolder().getMessageStore().save(bean.getId(), bean));

        ReactionEmoji emoji = ReactionEmoji.of(emojiId, emojiName, emojiAnimated);
        return addToMessage.thenReturn(new ReactionAddEvent(client, userId, channelId, messageId, emoji));

    }

    static Mono<ReactionRemoveEvent> messageReactionRemove(DispatchContext<MessageReactionRemove> context) {
        DiscordClient client = context.getServiceMediator().getClient();

        Long emojiId = context.getDispatch().getEmoji().getId();
        String emojiName = context.getDispatch().getEmoji().getName();
        boolean emojiAnimated = context.getDispatch().getEmoji().getAnimated() != null
                && context.getDispatch().getEmoji().getAnimated();

        long userId = context.getDispatch().getUserId();
        long channelId = context.getDispatch().getChannelId();
        long messageId = context.getDispatch().getMessageId();

        Mono<Void> removeFromMessage = context.getServiceMediator().getStoreHolder().getMessageStore()
                .find(messageId)
                .doOnNext(bean -> {
                    int i;
                    // noinspection ConstantConditions reactions must be present if one is being removed
                    for (i = 0; i < bean.getReactions().length; i++) {
                        ReactionBean r = bean.getReactions()[i];
                        if (Objects.equals(r.getEmojiId(), emojiId) && r.getEmojiName().equals(emojiName)) {
                            break;
                        }
                    }

                    ReactionBean existing = bean.getReactions()[i];
                    if (existing.getCount() - 1 == 0) {
                        bean.setReactions(ArrayUtil.remove(bean.getReactions(), existing));
                    } else {
                        existing.setCount(existing.getCount() - 1);

                        if (context.getServiceMediator().getStoreHolder().getSelfId().get() == userId) {
                            existing.setMe(false);
                        }
                    }
                })
                .flatMap(bean ->
                        context.getServiceMediator().getStoreHolder().getMessageStore().save(bean.getId(), bean));

        ReactionEmoji emoji = ReactionEmoji.of(emojiId, emojiName, emojiAnimated);
        return removeFromMessage.thenReturn(new ReactionRemoveEvent(client, userId, channelId, messageId, emoji));
    }

    static Mono<ReactionRemoveAllEvent> messageReactionRemoveAll(DispatchContext<MessageReactionRemoveAll> context) {
        DiscordClient client = context.getServiceMediator().getClient();
        long channelId = context.getDispatch().getChannelId();
        long messageId = context.getDispatch().getMessageId();

        Mono<Void> removeAllFromMessage = context.getServiceMediator().getStoreHolder().getMessageStore()
                .find(messageId)
                .doOnNext(bean -> bean.setReactions(null))
                .flatMap(bean ->
                        context.getServiceMediator().getStoreHolder().getMessageStore().save(bean.getId(), bean));

        return removeAllFromMessage.thenReturn(new ReactionRemoveAllEvent(client, channelId, messageId));
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
