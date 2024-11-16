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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.*;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.PartialMessageUpdateData;
import discord4j.discordjson.json.gateway.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class MessageDispatchHandlers {

    static Mono<MessageCreateEvent> messageCreate(DispatchContext<MessageCreate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        MessageData message = context.getDispatch().message();

        Optional<Member> maybeMember = context.getDispatch().message().guildId().toOptional()
                .map(Snowflake::asLong)
                .flatMap(guildId -> message.member().toOptional()
                        .map(memberData -> new Member(gateway, MemberData.builder()
                                .from(MemberData.builder()
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

        return Mono.just(new MessageCreateEvent(gateway, context.getShardInfo(), new Message(gateway, message),
                        context.getDispatch().message().guildId().toOptional()
                                .map(Snowflake::asLong)
                                .orElse(null),
                        maybeMember.orElse(null)));
    }

    static Mono<MessageDeleteEvent> messageDelete(DispatchContext<MessageDelete, MessageData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long messageId = Snowflake.asLong(context.getDispatch().id());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        Long guildId = context.getDispatch().guildId()
            .toOptional()
            .map(Snowflake::asLong)
            .orElse(null);

        Message oldMessage = context.getOldState()
                .map(data -> new Message(gateway, data))
                .orElse(null);

        return Mono.just(new MessageDeleteEvent(gateway, context.getShardInfo(), messageId, channelId, guildId,
                oldMessage));
    }

    static Mono<MessageBulkDeleteEvent> messageDeleteBulk(DispatchContext<MessageDeleteBulk, Set<MessageData>> context) {
        GatewayDiscordClient gateway = context.getGateway();
        List<Long> messageIds = context.getDispatch().ids().stream()
                .map(Snowflake::asLong)
                .collect(Collectors.toList());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        long guildId = Snowflake.asLong(context.getDispatch().guildId().get()); // always present

        Set<Message> deletedMessages = context.getOldState()
                .map(oldState -> oldState.stream()
                        .map(data -> new Message(gateway, data))
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        return Mono.just(new MessageBulkDeleteEvent(gateway, context.getShardInfo(), messageIds, channelId,
                        guildId, deletedMessages));
    }

    static Mono<ReactionAddEvent> messageReactionAdd(DispatchContext<MessageReactionAdd, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long userId = Snowflake.asLong(context.getDispatch().userId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        long messageId = Snowflake.asLong(context.getDispatch().messageId());
        long messageAuthorId = Snowflake.asLong(context.getDispatch().messageAuthorId().toOptional().orElse(Id.of(0L)));
        Long guildId = context.getDispatch().guildId()
                .toOptional()
                .map(Snowflake::asLong)
                .orElse(null);

        MemberData memberData = context.getDispatch().member().toOptional().orElse(null);
        List<String> burstColors = context.getDispatch().burstColors().toOptional().orElse(Collections.emptyList());
        boolean burst = context.getDispatch().burst();
        int type = context.getDispatch().type();

        Long emojiId = context.getDispatch().emoji().id()
                .map(Snowflake::asLong)
                .orElse(null);
        String emojiName = context.getDispatch().emoji().name()
                .orElse(null);
        boolean emojiAnimated = context.getDispatch().emoji().animated()
                .toOptional()
                .orElse(false);
        ReactionEmoji emoji = ReactionEmoji.of(emojiId, emojiName, emojiAnimated);
        @SuppressWarnings("ConstantConditions")
        Member member = memberData != null ? new Member(gateway, memberData, guildId) : null;

        return Mono.just(new ReactionAddEvent(gateway, context.getShardInfo(), userId, channelId,
                messageId, guildId, emoji, member, messageAuthorId, burst, burstColors, type));
    }

    static Mono<ReactionRemoveEvent> messageReactionRemove(DispatchContext<MessageReactionRemove, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long userId = Snowflake.asLong(context.getDispatch().userId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        long messageId = Snowflake.asLong(context.getDispatch().messageId());
        Long guildId = context.getDispatch().guildId()
                .toOptional()
                .map(Snowflake::asLong)
                .orElse(null);
        boolean burst = context.getDispatch().burst();
        int type = context.getDispatch().type();

        Long emojiId = context.getDispatch().emoji().id()
                .map(Snowflake::asLong)
                .orElse(null);
        String emojiName = context.getDispatch().emoji().name()
                .orElse(null);
        boolean emojiAnimated = context.getDispatch().emoji().animated()
                .toOptional()
                .orElse(false);
        ReactionEmoji emoji = ReactionEmoji.of(emojiId, emojiName, emojiAnimated);
        return Mono.just(new ReactionRemoveEvent(gateway, context.getShardInfo(), userId,
                channelId, messageId, guildId, emoji, burst, type));
    }

    static Mono<ReactionRemoveEmojiEvent> messageReactionRemoveEmoji(DispatchContext<MessageReactionRemoveEmoji, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        long messageId = Snowflake.asLong(context.getDispatch().messageId());
        Long guildId = context.getDispatch().guildId()
                .toOptional()
                .map(Snowflake::asLong)
                .orElse(null);

        Long emojiId = context.getDispatch().emoji().id()
                .map(Snowflake::asLong)
                .orElse(null);
        String emojiName = context.getDispatch().emoji().name()
                .orElse(null);
        boolean emojiAnimated = context.getDispatch().emoji().animated()
                .toOptional()
                .orElse(false);
        ReactionEmoji emoji = ReactionEmoji.of(emojiId, emojiName, emojiAnimated);
        return Mono.just(new ReactionRemoveEmojiEvent(gateway, context.getShardInfo(), channelId,
                messageId, guildId, emoji));
    }

    static Mono<ReactionRemoveAllEvent> messageReactionRemoveAll(DispatchContext<MessageReactionRemoveAll, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        long messageId = Snowflake.asLong(context.getDispatch().messageId());
        Long guildId = context.getDispatch().guildId()
                .toOptional()
                .map(Snowflake::asLong)
                .orElse(null);

        return Mono.just(new ReactionRemoveAllEvent(gateway, context.getShardInfo(), channelId,
                messageId, guildId));
    }

    static Mono<MessageUpdateEvent> messageUpdate(DispatchContext<MessageUpdate, MessageData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        PartialMessageUpdateData messageData = context.getDispatch().message();

        long channelId = Snowflake.asLong(messageData.channelId());
        long messageId = Snowflake.asLong(messageData.id());
        Long guildId = messageData.guildId()
                .toOptional()
                .map(Snowflake::asLong)
                .orElse(null);

        String currentContent = messageData.content().toOptional().orElse(null);
        List<Embed> embedList = messageData.embeds()
                .stream()
                .map(embedData -> new Embed(gateway, embedData))
                .collect(Collectors.toList());

        Message oldMessage = context.getOldState()
            .map(data -> new Message(gateway, data))
            .orElse(null);

        return Mono.just(new MessageUpdateEvent(gateway, context.getShardInfo(), messageId, channelId,
                guildId, oldMessage, !messageData.content().isAbsent(),
                currentContent, !messageData.embeds().isEmpty(), embedList));
    }
}
