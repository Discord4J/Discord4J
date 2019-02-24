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
import discord4j.common.jackson.PossibleLong;
import discord4j.common.json.EmbedResponse;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.event.domain.message.*;
import discord4j.core.object.Embed;
import discord4j.core.object.data.stored.MemberBean;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.data.stored.ReactionBean;
import discord4j.core.object.data.stored.UserBean;
import discord4j.core.object.data.stored.embed.EmbedBean;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.util.ArrayUtil;
import discord4j.gateway.json.dispatch.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class MessageDispatchHandlers {

    static Mono<MessageCreateEvent> messageCreate(DispatchContext<MessageCreate> context) {
        DiscordClient client = context.getServiceMediator().getClient();

        MessageBean bean = new MessageBean(context.getDispatch());
        Message message = new Message(context.getServiceMediator(), bean);

        Long guildId = context.getDispatch().getGuildId();
        MessageCreate.MessageMember memberResponse = context.getDispatch().getMember();
        Member member = null;

        if (guildId != null && memberResponse != null) {
            UserBean authorUser = new UserBean(context.getDispatch().getAuthor());
            MemberBean authorMember = new MemberBean(context.getDispatch().getMember());
            member = new Member(context.getServiceMediator(), authorMember, authorUser, guildId);
        }

        Mono<Void> saveMessage = context.getServiceMediator().getStateHolder().getMessageStore()
                .save(bean.getId(), bean);

        Mono<Void> editLastMessageId = context.getServiceMediator().getStateHolder().getTextChannelStore()
                .find(bean.getChannelId())
                .doOnNext(channelBean -> channelBean.getMessageChannel().setLastMessageId(bean.getId()))
                .flatMap(channelBean -> context.getServiceMediator().getStateHolder().getTextChannelStore()
                        .save(channelBean.getId(), channelBean));

        return saveMessage
                .and(editLastMessageId)
                .thenReturn(new MessageCreateEvent(client, message, guildId, member));
    }

    static Mono<MessageDeleteEvent> messageDelete(DispatchContext<MessageDelete> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        long messageId = context.getDispatch().getId();
        long channelId = context.getDispatch().getChannelId();

        Mono<Void> deleteMessage = serviceMediator.getStateHolder().getMessageStore()
                .delete(context.getDispatch().getId());

        return serviceMediator.getStateHolder().getMessageStore()
                .find(messageId)
                .flatMap(deleteMessage::thenReturn)
                .map(messageBean -> new MessageDeleteEvent(client, messageId, channelId,
                        new Message(serviceMediator, messageBean)))
                .defaultIfEmpty(new MessageDeleteEvent(client, messageId, channelId, null));
    }

    static Mono<MessageBulkDeleteEvent> messageDeleteBulk(DispatchContext<MessageDeleteBulk> context) {
        DiscordClient client = context.getServiceMediator().getClient();
        long messageIds[] = context.getDispatch().getIds();
        long channelId = context.getDispatch().getChannelId();
        long guildId = context.getDispatch().getGuildId();

        Mono<Void> deleteMessages = context.getServiceMediator().getStateHolder().getMessageStore()
                .delete(Flux.fromArray(ArrayUtil.toObject(messageIds)));

        return Flux.fromArray(ArrayUtil.toObject(messageIds))
                .flatMap(context.getServiceMediator().getStateHolder().getMessageStore()::find)
                .map(messageBean -> new Message(context.getServiceMediator(), messageBean))
                .collect(Collectors.toSet())
                .flatMap(deleteMessages::thenReturn)
                .map(messages -> new MessageBulkDeleteEvent(client, messageIds, channelId, guildId, messages))
                .defaultIfEmpty(
                        new MessageBulkDeleteEvent(client, messageIds, channelId, guildId, Collections.emptySet()));

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
        Long guildId = context.getDispatch().getGuildId();

        Mono<Void> addToMessage = context.getServiceMediator().getStateHolder().getMessageStore()
                .find(messageId)
                .map(oldBean -> {
                    boolean me = context.getServiceMediator().getStateHolder().getSelfId().get() == userId;
                    MessageBean newBean = new MessageBean(oldBean);

                    if (oldBean.getReactions() == null) {
                        ReactionBean r = new ReactionBean(1, me, emojiId, emojiName, emojiAnimated);
                        newBean.setReactions(new ReactionBean[] { r });
                    } else {
                        int i;
                        for (i = 0; i < oldBean.getReactions().length; i++) {
                            ReactionBean r = oldBean.getReactions()[i];
                            if (Objects.equals(r.getEmojiId(), emojiId) && r.getEmojiName().equals(emojiName)) {
                                break;
                            }
                        }

                        if (i < oldBean.getReactions().length) {
                            ReactionBean oldExisting = oldBean.getReactions()[i];
                            ReactionBean newExisting = new ReactionBean(oldExisting);
                            newExisting.setMe(me);
                            newExisting.setCount(oldExisting.getCount() + 1);
                            ArrayUtil.replace(newBean.getReactions(), oldExisting, newExisting);
                        } else {
                            ReactionBean r = new ReactionBean(1, me, emojiId, emojiName, emojiAnimated);
                            newBean.setReactions(ArrayUtil.add(oldBean.getReactions(), r));
                        }
                    }

                    return newBean;
                })
                .flatMap(bean ->
                        context.getServiceMediator().getStateHolder().getMessageStore().save(bean.getId(), bean));

        ReactionEmoji emoji = ReactionEmoji.of(emojiId, emojiName, emojiAnimated);
        return addToMessage.thenReturn(new ReactionAddEvent(client, userId, channelId, messageId, guildId, emoji));

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
        Long guildId = context.getDispatch().getGuildId();

        Mono<Void> removeFromMessage = context.getServiceMediator().getStateHolder().getMessageStore()
                .find(messageId)
                .map(oldBean -> {
                    int i;
                    // noinspection ConstantConditions reactions must be present if one is being removed
                    for (i = 0; i < oldBean.getReactions().length; i++) {
                        ReactionBean r = oldBean.getReactions()[i];
                        if (Objects.equals(r.getEmojiId(), emojiId) && r.getEmojiName().equals(emojiName)) {
                            break;
                        }
                    }

                    MessageBean newBean = new MessageBean(oldBean);
                    ReactionBean existing = oldBean.getReactions()[i];
                    if (existing.getCount() - 1 == 0) {
                        newBean.setReactions(ArrayUtil.remove(oldBean.getReactions(), existing));
                    } else {
                        ReactionBean newExisting = new ReactionBean(existing);
                        newExisting.setCount(existing.getCount() - 1);

                        if (context.getServiceMediator().getStateHolder().getSelfId().get() == userId) {
                            newExisting.setMe(false);
                        }

                        ArrayUtil.replace(newBean.getReactions(), existing, newExisting);
                    }
                    return newBean;
                })
                .flatMap(bean ->
                        context.getServiceMediator().getStateHolder().getMessageStore().save(bean.getId(), bean));

        ReactionEmoji emoji = ReactionEmoji.of(emojiId, emojiName, emojiAnimated);
        return removeFromMessage.thenReturn(new ReactionRemoveEvent(client, userId, channelId, messageId, guildId,
                                                                    emoji));
    }

    static Mono<ReactionRemoveAllEvent> messageReactionRemoveAll(DispatchContext<MessageReactionRemoveAll> context) {
        DiscordClient client = context.getServiceMediator().getClient();
        long channelId = context.getDispatch().getChannelId();
        long messageId = context.getDispatch().getMessageId();
        Long guildId = context.getDispatch().getGuildId();

        Mono<Void> removeAllFromMessage = context.getServiceMediator().getStateHolder().getMessageStore()
                .find(messageId)
                .map(MessageBean::new)
                .doOnNext(bean -> bean.setReactions(null))
                .flatMap(bean ->
                        context.getServiceMediator().getStateHolder().getMessageStore().save(bean.getId(), bean));

        return removeAllFromMessage.thenReturn(new ReactionRemoveAllEvent(client, channelId, messageId, guildId));
    }

    static Mono<MessageUpdateEvent> messageUpdate(DispatchContext<MessageUpdate> context) {
        DiscordClient client = context.getServiceMediator().getClient();

        long messageId = context.getDispatch().getId();
        long channelId = context.getDispatch().getChannelId();
        Long guildId = context.getDispatch().getGuildId();

        Possible<String> content = context.getDispatch().getContent();
        boolean contentChanged = content == null || !content.isAbsent();
        String currentContent = content == null || content.isAbsent() ? null : content.get();

        Possible<EmbedResponse[]> embeds = context.getDispatch().getEmbeds();
        boolean embedsChanged = embeds == null || !embeds.isAbsent();
        EmbedResponse[] currentEmbeds = embeds == null || embeds.isAbsent() ? null : embeds.get();

        EmbedBean[] embedBeans = currentEmbeds == null ? new EmbedBean[0] :
                Arrays.stream(currentEmbeds)
                        .map(EmbedBean::new)
                        .toArray(EmbedBean[]::new);

        List<Embed> embedList = Arrays.stream(embedBeans)
                .map(bean -> new Embed(context.getServiceMediator(), bean))
                .collect(Collectors.toList());

        Mono<MessageUpdateEvent> update = context.getServiceMediator().getStateHolder().getMessageStore()
                .find(messageId)
                .flatMap(oldBean -> {
                    // updating the content and embed of the bean in the store
                    Message old = new Message(context.getServiceMediator(), oldBean);
                    MessageBean newBean = new MessageBean(oldBean);

                    newBean.setContent(currentContent);
                    newBean.setEmbeds(embedBeans);

                    MessageUpdateEvent event = new MessageUpdateEvent(client, messageId, channelId, guildId, old,
                            contentChanged, currentContent, embedsChanged, embedList);

                    return context.getServiceMediator().getStateHolder().getMessageStore()
                            .save(newBean.getId(), newBean)
                            .thenReturn(event);
                });

        MessageUpdateEvent event = new MessageUpdateEvent(client, messageId, channelId, guildId, null, contentChanged,
                currentContent, embedsChanged, embedList);

        return update.defaultIfEmpty(event);
    }
}
