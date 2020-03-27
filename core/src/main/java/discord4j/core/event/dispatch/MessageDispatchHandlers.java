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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.*;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.state.ParameterData;
import discord4j.core.state.StateHolder;
import discord4j.core.util.ListUtil;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

class MessageDispatchHandlers {

    static Mono<MessageCreateEvent> messageCreate(DispatchContext<MessageCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        MessageData message = context.getDispatch().message();
        long messageId = Snowflake.asLong(message.id());
        long channelId = Snowflake.asLong(message.channelId());

        Possible<String> maybeGuildId = context.getDispatch().message().guildId();

        Optional<Member> maybeMember = maybeGuildId.toOptional()
                .map(Long::parseUnsignedLong)
                .flatMap(guildId -> message.member().toOptional()
                        .map(memberData -> new Member(gateway, ImmutableMemberData.builder()
                                .from(ImmutableMemberData.builder()
                                        .user(message.author())
                                        .nick(memberData.nick())
                                        .roles(memberData.roles())
                                        .joinedAt(memberData.joinedAt())
                                        .premiumSince(memberData.premiumSince())
                                        .hoistedRole(memberData.hoistedRole())
                                        .deaf(memberData.deaf())
                                        .mute(memberData.mute())
                                        .build())
                                .user(message.author())
                                .build(), guildId)));

        Mono<Void> saveMessage = context.getStateHolder().getMessageStore()
                .save(messageId, message);

        Mono<Void> editLastMessageId = context.getStateHolder().getChannelStore()
                .find(channelId)
                .map(channel -> ImmutableChannelData.builder()
                        .from(channel)
                        .lastMessageId(Possible.of(Optional.of(message.id())))
                        .build())
                .flatMap(channelBean -> context.getStateHolder().getChannelStore().save(channelId, channelBean));

        return saveMessage
                .and(editLastMessageId)
                .thenReturn(new MessageCreateEvent(gateway, context.getShardInfo(), new Message(gateway, message),
                        maybeGuildId.toOptional()
                                .map(Long::parseUnsignedLong)
                                .orElse(null),
                        maybeMember.orElse(null)));
    }

    static Mono<MessageDeleteEvent> messageDelete(DispatchContext<MessageDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long messageId = Snowflake.asLong(context.getDispatch().id());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());

        Mono<Void> deleteMessage = context.getStateHolder().getMessageStore().delete(messageId);

        return context.getStateHolder().getMessageStore()
                .find(messageId)
                .flatMap(deleteMessage::thenReturn)
                .map(messageBean -> new MessageDeleteEvent(gateway, context.getShardInfo(), messageId, channelId,
                        new Message(gateway, messageBean)))
                .defaultIfEmpty(new MessageDeleteEvent(gateway, context.getShardInfo(), messageId, channelId, null));
    }

    static Mono<MessageBulkDeleteEvent> messageDeleteBulk(DispatchContext<MessageDeleteBulk> context) {
        GatewayDiscordClient gateway = context.getGateway();
        List<Long> messageIds = context.getDispatch().ids().stream()
                .map(Long::parseUnsignedLong)
                .collect(Collectors.toList());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        long guildId = Snowflake.asLong(context.getDispatch().guildId().get()); // always present

        Mono<Void> deleteMessages = context.getStateHolder().getMessageStore()
                .delete(Flux.fromIterable(messageIds));

        return Flux.fromIterable(messageIds)
                .flatMap(context.getStateHolder().getMessageStore()::find)
                .map(messageBean -> new Message(gateway, messageBean))
                .collect(Collectors.toSet())
                .flatMap(deleteMessages::thenReturn)
                .map(messages -> new MessageBulkDeleteEvent(gateway, context.getShardInfo(), messageIds, channelId,
                        guildId, messages))
                .defaultIfEmpty(new MessageBulkDeleteEvent(gateway, context.getShardInfo(), messageIds, channelId,
                        guildId, Collections.emptySet()));
    }

    static Mono<ReactionAddEvent> messageReactionAdd(DispatchContext<MessageReactionAdd> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long userId = Snowflake.asLong(context.getDispatch().userId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        long messageId = Snowflake.asLong(context.getDispatch().messageId());
        Long guildId = context.getDispatch().guildId()
                .toOptional()
                .map(Long::parseUnsignedLong)
                .orElse(null);

        MemberData memberData = context.getDispatch().member().toOptional().orElse(null);

        Mono<Void> addToMessage = context.getStateHolder().getMessageStore()
                .find(messageId)
                .zipWith(context.getStateHolder().getParameterStore().find(StateHolder.SELF_ID_PARAMETER_KEY)
                        .switchIfEmpty(Mono.just(new ParameterData())))
                .map(t2 -> {
                    boolean me = Objects.equals(userId, t2.getT2().getValue());
                    MessageData oldMessage = t2.getT1();
                    ImmutableMessageData.Builder newMessageBuilder = ImmutableMessageData.builder().from(oldMessage);

                    if (oldMessage.reactions().isAbsent()) {
                        newMessageBuilder.reactions(Possible.of(Collections.singletonList(
                                ImmutableReactionData.builder()
                                        .count(1)
                                        .me(me)
                                        .emoji(context.getDispatch().emoji())
                                        .build())));
                    } else {
                        List<ReactionData> reactions = oldMessage.reactions().get();
                        int i;
                        for (i = 0; i < reactions.size(); i++) {
                            ReactionData r = reactions.get(i);
                            // (non-null id && matching id) OR (null id && matching name)
                            boolean emojiHasId = context.getDispatch().emoji().id().isPresent();
                            if ((emojiHasId && context.getDispatch().emoji().id().equals(r.emoji().id()))
                                    || (!emojiHasId && context.getDispatch().emoji().name().equals(r.emoji().name()))) {
                                break;
                            }
                        }

                        if (i < reactions.size()) {
                            // message already has this reaction: bump 1
                            ReactionData oldExisting = reactions.get(i);
                            ReactionData newExisting = ImmutableReactionData.builder()
                                    .from(oldExisting)
                                    .me(oldExisting.me() || me)
                                    .count(oldExisting.count() + 1)
                                    .build();
                            newMessageBuilder.reactions(ListUtil.replace(oldMessage.reactions(),
                                    oldExisting, newExisting));
                        } else {
                            // message doesn't have this reaction: create
                            ReactionData reaction = ImmutableReactionData.builder()
                                    .emoji(context.getDispatch().emoji())
                                    .me(me)
                                    .count(1)
                                    .build();
                            newMessageBuilder.reactions(ListUtil.add(oldMessage.reactions(), reaction));
                        }
                    }

                    return newMessageBuilder.build();
                })
                .flatMap(message -> context.getStateHolder().getMessageStore().save(messageId, message));

        Long emojiId = context.getDispatch().emoji().id()
                .map(Long::parseUnsignedLong)
                .orElse(null);
        String emojiName = context.getDispatch().emoji().name()
                .orElse(null);
        boolean emojiAnimated = context.getDispatch().emoji().animated()
                .toOptional()
                .orElse(false);
        ReactionEmoji emoji = ReactionEmoji.of(emojiId, emojiName, emojiAnimated);
        @SuppressWarnings("ConstantConditions")
        Member member = memberData != null ? new Member(gateway, memberData, guildId) : null;

        return addToMessage.thenReturn(new ReactionAddEvent(gateway, context.getShardInfo(), userId, channelId,
                messageId, guildId, emoji, member));
    }

    static Mono<ReactionRemoveEvent> messageReactionRemove(DispatchContext<MessageReactionRemove> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long userId = Snowflake.asLong(context.getDispatch().userId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        long messageId = Snowflake.asLong(context.getDispatch().messageId());
        Long guildId = context.getDispatch().guildId()
                .toOptional()
                .map(Long::parseUnsignedLong)
                .orElse(null);

        Mono<Void> removeFromMessage = context.getStateHolder().getMessageStore()
                .find(messageId)
                .filter(message -> !message.reactions().isAbsent())
                .zipWith(context.getStateHolder().getParameterStore()
                        .find(StateHolder.SELF_ID_PARAMETER_KEY)
                        .switchIfEmpty(Mono.just(new ParameterData())))
                .map(t2 -> {
                    boolean me = Objects.equals(userId, t2.getT2().getValue());
                    MessageData oldMessage = t2.getT1();
                    ImmutableMessageData.Builder newMessageBuilder = ImmutableMessageData.builder().from(oldMessage);

                    List<ReactionData> reactions = oldMessage.reactions().get();
                    int i;
                    // filter covers getReactions() null case
                    for (i = 0; i < reactions.size(); i++) {
                        ReactionData r = reactions.get(i);
                        // (non-null id && matching id) OR (null id && matching name)
                        boolean emojiHasId = context.getDispatch().emoji().id().isPresent();
                        if ((emojiHasId && context.getDispatch().emoji().id().equals(r.emoji().id()))
                                || (!emojiHasId && context.getDispatch().emoji().name().equals(r.emoji().name()))) {
                            break;
                        }
                    }

                    ReactionData existing = reactions.get(i);
                    if (existing.count() - 1 == 0) {
                        newMessageBuilder.reactions(ListUtil.remove(oldMessage.reactions(),
                                reaction -> reaction.equals(existing)));
                    } else {
                        ReactionData newExisting = ImmutableReactionData.builder()
                                .from(existing)
                                .count(existing.count() - 1)
                                .me(!me && existing.me())
                                .build();
                        newMessageBuilder.reactions(ListUtil.replace(oldMessage.reactions(), existing, newExisting));
                    }
                    return newMessageBuilder.build();
                })
                .flatMap(message -> context.getStateHolder().getMessageStore().save(messageId, message));

        Long emojiId = context.getDispatch().emoji().id()
                .map(Long::parseUnsignedLong)
                .orElse(null);
        String emojiName = context.getDispatch().emoji().name()
                .orElse(null);
        boolean emojiAnimated = context.getDispatch().emoji().animated()
                .toOptional()
                .orElse(false);
        ReactionEmoji emoji = ReactionEmoji.of(emojiId, emojiName, emojiAnimated);
        return removeFromMessage.thenReturn(new ReactionRemoveEvent(gateway, context.getShardInfo(), userId,
                channelId, messageId, guildId, emoji));
    }

    static Mono<ReactionRemoveAllEvent> messageReactionRemoveAll(DispatchContext<MessageReactionRemoveAll> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        long messageId = Snowflake.asLong(context.getDispatch().messageId());
        Long guildId = context.getDispatch().guildId()
                .toOptional()
                .map(Long::parseUnsignedLong)
                .orElse(null);

        Mono<Void> removeAllFromMessage = context.getStateHolder().getMessageStore()
                .find(messageId)
                .map(message -> ImmutableMessageData.builder()
                        .from(message)
                        .reactions(Possible.absent())
                        .build())
                .flatMap(message -> context.getStateHolder().getMessageStore().save(messageId, message));

        return removeAllFromMessage.thenReturn(new ReactionRemoveAllEvent(gateway, context.getShardInfo(), channelId,
                messageId, guildId));
    }

    static Mono<MessageUpdateEvent> messageUpdate(DispatchContext<MessageUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        PartialMessageData messageData = context.getDispatch().message();

        long channelId = Snowflake.asLong(messageData.channelId());
        long messageId = Snowflake.asLong(messageData.id());
        Long guildId = messageData.guildId()
                .toOptional()
                .map(Long::parseUnsignedLong)
                .orElse(null);

        String currentContent = messageData.content().toOptional().orElse(null);
        List<Embed> embedList = messageData.embeds()
                .stream()
                .map(embedData -> new Embed(gateway, embedData))
                .collect(Collectors.toList());

        Mono<MessageUpdateEvent> update = context.getStateHolder().getMessageStore()
                .find(messageId)
                .flatMap(oldMessageData -> {
                    // updating the content and embed of the bean in the store
                    Message oldMessage = new Message(gateway, oldMessageData);

                    boolean contentChanged = !Objects.equals(oldMessageData.content(), messageData.content());
                    boolean embedsChanged = !Objects.equals(oldMessageData.embeds(), messageData.embeds());

                    MessageData newMessageData = ImmutableMessageData.builder()
                            .from(oldMessageData)
                            .content(messageData.content().toOptional().orElse(oldMessageData.content()))
                            .embeds(messageData.embeds())
                            .mentions(messageData.mentions())
                            .mentionRoles(messageData.mentionRoles())
                            .mentionEveryone(messageData.mentionEveryone().toOptional().orElse(oldMessageData.mentionEveryone()))
                            .editedTimestamp(messageData.editedTimestamp())
                            .build();

                    MessageUpdateEvent event = new MessageUpdateEvent(gateway, context.getShardInfo(), messageId,
                            channelId, guildId, oldMessage, contentChanged, currentContent, embedsChanged, embedList);

                    return context.getStateHolder().getMessageStore()
                            .save(messageId, newMessageData)
                            .thenReturn(event);
                });

        MessageUpdateEvent event = new MessageUpdateEvent(gateway, context.getShardInfo(), messageId, channelId,
                guildId, null, !messageData.content().isAbsent(),
                currentContent, !messageData.embeds().isEmpty(), embedList);

        return update.defaultIfEmpty(event);
    }

    static <T> Possible<T> newPossibleIfPresent(Possible<T> oldPossible, Possible<T> newPossible) {
        return newPossible.isAbsent() ? oldPossible : newPossible;
    }
}
