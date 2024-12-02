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
package discord4j.core.retriever;

import discord4j.common.util.Snowflake;
import discord4j.core.object.ScheduledEventUser;
import discord4j.core.object.VoiceState;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FallbackEntityRetriever implements EntityRetriever {

    private final EntityRetriever first;
    private final EntityRetriever fallback;

    public FallbackEntityRetriever(EntityRetriever first, EntityRetriever fallback) {
        this.first = first;
        this.fallback = fallback;
    }

    @Override
    public Mono<Channel> getChannelById(Snowflake channelId) {
        return first.getChannelById(channelId).switchIfEmpty(fallback.getChannelById(channelId));
    }

    @Override
    public Mono<Guild> getGuildById(Snowflake guildId) {
        return first.getGuildById(guildId).switchIfEmpty(fallback.getGuildById(guildId));
    }

    @Override
    public Mono<GuildSticker> getGuildStickerById(Snowflake guildId, Snowflake stickerId) {
        return first.getGuildStickerById(guildId, stickerId).switchIfEmpty(fallback.getGuildStickerById(guildId, stickerId));
    }

    @Override
    public Mono<GuildEmoji> getGuildEmojiById(Snowflake guildId, Snowflake emojiId) {
        return first.getGuildEmojiById(guildId, emojiId).switchIfEmpty(fallback.getGuildEmojiById(guildId, emojiId));
    }

    @Override
    public Mono<Member> getMemberById(Snowflake guildId, Snowflake userId) {
        return first.getMemberById(guildId, userId).switchIfEmpty(fallback.getMemberById(guildId, userId));
    }

    @Override
    public Mono<Message> getMessageById(Snowflake channelId, Snowflake messageId) {
        return first.getMessageById(channelId, messageId).switchIfEmpty(fallback.getMessageById(channelId, messageId));
    }

    @Override
    public Mono<Role> getRoleById(Snowflake guildId, Snowflake roleId) {
        return first.getRoleById(guildId, roleId).switchIfEmpty(fallback.getRoleById(guildId, roleId));
    }

    @Override
    public Mono<User> getUserById(Snowflake userId) {
        return first.getUserById(userId).switchIfEmpty(fallback.getUserById(userId));
    }

    @Override
    public Flux<Guild> getGuilds() {
        return first.getGuilds().switchIfEmpty(fallback.getGuilds());
    }

    @Override
    public Mono<User> getSelf() {
        return first.getSelf().switchIfEmpty(fallback.getSelf());
    }

    @Override
    public Mono<Member> getSelfMember(Snowflake guildId) {
        return first.getSelfMember(guildId).switchIfEmpty(fallback.getSelfMember(guildId));
    }

    @Override
    public Flux<Member> getGuildMembers(Snowflake guildId) {
        return first.getGuildMembers(guildId).switchIfEmpty(fallback.getGuildMembers(guildId));
    }

    @Override
    public Flux<GuildChannel> getGuildChannels(Snowflake guildId) {
        return first.getGuildChannels(guildId).switchIfEmpty(fallback.getGuildChannels(guildId));
    }

    @Override
    public Flux<Role> getGuildRoles(Snowflake guildId) {
        return first.getGuildRoles(guildId).switchIfEmpty(fallback.getGuildRoles(guildId));
    }

    @Override
    public Flux<GuildEmoji> getGuildEmojis(Snowflake guildId) {
        return first.getGuildEmojis(guildId).switchIfEmpty(fallback.getGuildEmojis(guildId));
    }

    @Override
    public Mono<StageInstance> getStageInstanceByChannelId(Snowflake channelId) {
        return first.getStageInstanceByChannelId(channelId).switchIfEmpty(fallback.getStageInstanceByChannelId(channelId));
    }

    @Override
    public Flux<GuildSticker> getGuildStickers(Snowflake guildId) {
        return first.getGuildStickers(guildId).switchIfEmpty(fallback.getGuildStickers(guildId));
    }

    @Override
    public Mono<ThreadMember> getThreadMemberById(Snowflake threadId, Snowflake userId) {
        return first.getThreadMemberById(threadId, userId).switchIfEmpty(fallback.getThreadMemberById(threadId, userId));
    }

    @Override
    public Flux<ThreadMember> getThreadMembers(Snowflake threadId) {
        return first.getThreadMembers(threadId).switchIfEmpty(fallback.getThreadMembers(threadId));
    }

    @Override
    public Flux<AutoModRule> getGuildAutoModRules(Snowflake guildId) {
        return first.getGuildAutoModRules(guildId).switchIfEmpty(fallback.getGuildAutoModRules(guildId));
    }

    @Override
    public Mono<ScheduledEvent> getScheduledEventById(Snowflake guildId, Snowflake eventId) {
        return first.getScheduledEventById(guildId, eventId).switchIfEmpty(fallback.getScheduledEventById(guildId, eventId));
    }

    @Override
    public Flux<ScheduledEvent> getScheduledEvents(Snowflake guildId) {
        return first.getScheduledEvents(guildId).switchIfEmpty(fallback.getScheduledEvents(guildId));
    }

    @Override
    public Flux<ScheduledEventUser> getScheduledEventUsers(Snowflake guildId, Snowflake eventId) {
        return first.getScheduledEventUsers(guildId, eventId).switchIfEmpty(fallback.getScheduledEventUsers(guildId, eventId));
    }

    @Override
    public Mono<VoiceState> getVoiceStateById(Snowflake guildId, Snowflake userId) {
        return first.getVoiceStateById(guildId, userId);
    }
}
