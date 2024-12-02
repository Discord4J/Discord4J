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

import discord4j.common.store.Store;
import discord4j.common.store.action.read.ReadActions;
import discord4j.common.store.api.object.ExactResultNotAvailableException;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ScheduledEventUser;
import discord4j.core.object.VoiceState;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.GuildScheduledEventUserData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class StoreEntityRetriever implements EntityRetriever {

    private final GatewayDiscordClient gateway;
    private final Store store;

    public StoreEntityRetriever(GatewayDiscordClient gateway) {
        this.gateway = gateway;
        this.store = gateway.getGatewayResources().getStore();
    }

    @Override
    public Mono<Channel> getChannelById(Snowflake channelId) {
        return Mono.from(store.execute(ReadActions.getChannelById(channelId.asLong())))
                .map(data -> EntityUtil.getChannel(gateway, data));
    }

    @Override
    public Mono<Guild> getGuildById(Snowflake guildId) {
        return Mono.from(store.execute(ReadActions.getGuildById(guildId.asLong())))
                .map(data -> new Guild(gateway, data));
    }

    @Override
    public Mono<GuildSticker> getGuildStickerById(Snowflake guildId, Snowflake stickerId) {
        return Mono.from(store.execute(ReadActions.getStickerById(guildId.asLong(), stickerId.asLong())))
            .map(data -> new GuildSticker(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<GuildEmoji> getGuildEmojiById(Snowflake guildId, Snowflake emojiId) {
        return Mono.from(store.execute(ReadActions.getEmojiById(guildId.asLong(), emojiId.asLong())))
                .map(data -> new GuildEmoji(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<Member> getMemberById(Snowflake guildId, Snowflake userId) {
        return Mono.from(store.execute(ReadActions.getMemberById(guildId.asLong(), userId.asLong())))
                .map(data -> new Member(gateway, data, guildId.asLong()))
                .onErrorResume(ExactResultNotAvailableException.class, ignored -> gateway
                        .requestMembers(guildId, Collections.singleton(userId))
                        .filter(member -> member.getId().equals(userId))
                        .next());
    }

    @Override
    public Mono<Message> getMessageById(Snowflake channelId, Snowflake messageId) {
        return Mono.from(store.execute(ReadActions.getMessageById(channelId.asLong(), messageId.asLong())))
                .map(data -> new Message(gateway, data));
    }

    @Override
    public Mono<Role> getRoleById(Snowflake guildId, Snowflake roleId) {
        return Mono.from(store.execute(ReadActions.getRoleById(guildId.asLong(), roleId.asLong())))
                .map(data -> new Role(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<User> getUserById(Snowflake userId) {
        return Mono.from(store.execute(ReadActions.getUserById(userId.asLong())))
                .map(data -> new User(gateway, data));
    }

    @Override
    public Flux<Guild> getGuilds() {
        return Flux.from(store.execute(ReadActions.getGuilds()))
                .map(data -> new Guild(gateway, data));
    }

    @Override
    public Mono<User> getSelf() {
        return getUserById(gateway.getSelfId());
    }

    @Override
    public Mono<Member> getSelfMember(Snowflake guildId) {
        return getMemberById(guildId, gateway.getSelfId());
    }

    @Override
    public Flux<Member> getGuildMembers(Snowflake guildId) {
        return Flux.from(store.execute(ReadActions.getExactMembersInGuild(guildId.asLong())))
                .map(data -> new Member(gateway, data, guildId.asLong()))
                .onErrorResume(ExactResultNotAvailableException.class, e -> gateway.requestMembers(guildId));
    }

    @Override
    public Flux<GuildChannel> getGuildChannels(Snowflake guildId) {
        return Flux.from(store.execute(ReadActions.getChannelsInGuild(guildId.asLong())))
                .map(channelData -> EntityUtil.getChannel(gateway, channelData))
                .ofType(GuildChannel.class);
    }

    @Override
    public Flux<Role> getGuildRoles(Snowflake guildId) {
        return Flux.from(store.execute(ReadActions.getRolesInGuild(guildId.asLong())))
                .map(roleData -> new Role(gateway, roleData, guildId.asLong()));
    }

    @Override
    public Flux<GuildEmoji> getGuildEmojis(Snowflake guildId) {
        return Flux.from(store.execute(ReadActions.getEmojisInGuild(guildId.asLong())))
                .map(emojiData -> new GuildEmoji(gateway, emojiData, guildId.asLong()));
    }

    @Override
    public Mono<StageInstance> getStageInstanceByChannelId(Snowflake channelId) {
        return Mono.from(store.execute(ReadActions.getStageInstanceByChannelId(channelId.asLong())))
                .map(data -> new StageInstance(gateway, data));
    }

    @Override
    public Flux<GuildSticker> getGuildStickers(Snowflake guildId) {
        return Flux.from(store.execute(ReadActions.getStickersInGuild(guildId.asLong())))
                .map(data -> new GuildSticker(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<ThreadMember> getThreadMemberById(Snowflake threadId, Snowflake userId) {
        return Mono.from(store.execute(ReadActions.getThreadMemberById(threadId.asLong(), userId.asLong())))
                .map(data -> new ThreadMember(gateway, data));
    }

    @Override
    public Flux<ThreadMember> getThreadMembers(Snowflake threadId) {
        return Flux.from(store.execute(ReadActions.getMembersInThread(threadId.asLong())))
                .map(data -> new ThreadMember(gateway, data));
    }

    @Override
    public Flux<AutoModRule> getGuildAutoModRules(Snowflake guildId) {
        return Flux.from(store.execute(ReadActions.getAutoModRulesInGuild(guildId.asLong())))
            .map(data -> new AutoModRule(gateway, data));
    }

    @Override
    public Mono<ScheduledEvent> getScheduledEventById(Snowflake guildId, Snowflake eventId) {
        return Mono.from(store.execute(ReadActions.getScheduledEventById(guildId.asLong(), eventId.asLong())))
            .map(data -> new ScheduledEvent(gateway, data));
    }

    @Override
    public Flux<ScheduledEvent> getScheduledEvents(Snowflake guildId) {
        return Flux.from(store.execute(ReadActions.getScheduledEventsInGuild(guildId.asLong())))
            .map(data -> new ScheduledEvent(gateway, data));
    }

    @Override
    public Flux<ScheduledEventUser> getScheduledEventUsers(Snowflake guildId, Snowflake eventId) {
        return Flux.from(store.execute(ReadActions.getScheduledEventsUsers(guildId.asLong(), eventId.asLong())))
            .flatMap(id -> Mono.from(store.execute(ReadActions.getUserById(id.asLong()))))
            .map(userData -> new ScheduledEventUser(gateway, GuildScheduledEventUserData.builder()
                .user(userData)
                .guildScheduledEventId(eventId.asLong())
                .build(), guildId));
    }

    @Override
    public Mono<VoiceState> getVoiceStateById(Snowflake guildId, Snowflake userId) {
        return Mono.from(store.execute(ReadActions.getVoiceStateById(guildId.asLong(), userId.asLong())))
            .map(bean -> new VoiceState(gateway, bean));
    }
}
