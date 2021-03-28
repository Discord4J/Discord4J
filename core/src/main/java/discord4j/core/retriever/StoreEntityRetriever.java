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
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Template;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.state.StateView;
import discord4j.core.util.EntityUtil;
import discord4j.gateway.intent.Intent;
import discord4j.store.api.util.LongLongTuple2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

public class StoreEntityRetriever implements EntityRetriever {

    private final GatewayDiscordClient gateway;
    private final StateView stateView;

    public StoreEntityRetriever(GatewayDiscordClient gateway) {
        this.gateway = gateway;
        this.stateView = gateway.getGatewayResources().getStateView();
    }

    @Override
    public Mono<Channel> getChannelById(Snowflake channelId) {
        return stateView.getChannelStore()
                .find(channelId.asLong())
                .map(data -> EntityUtil.getChannel(gateway, data));
    }

    @Override
    public Mono<Guild> getGuildById(Snowflake guildId) {
        return stateView.getGuildStore()
                .find(guildId.asLong())
                .map(data -> new Guild(gateway, data));
    }

    @Override
    public Mono<GuildEmoji> getGuildEmojiById(Snowflake guildId, Snowflake emojiId) {
        return stateView.getGuildEmojiStore()
                .find(emojiId.asLong())
                .map(data -> new GuildEmoji(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<Member> getMemberById(Snowflake guildId, Snowflake userId) {
        return stateView.getMemberStore()
                .find(LongLongTuple2.of(guildId.asLong(), userId.asLong()))
                .map(data -> new Member(gateway, data, guildId.asLong()))
                .switchIfEmpty(gateway.requestMembers(guildId, Collections.singleton(userId))
                        .filter(member -> member.getId().equals(userId))
                        .next());
    }

    @Override
    public Mono<Message> getMessageById(Snowflake channelId, Snowflake messageId) {
        return stateView.getMessageStore()
                .find(messageId.asLong())
                .map(data -> new Message(gateway, data));
    }

    @Override
    public Mono<Role> getRoleById(Snowflake guildId, Snowflake roleId) {
        return stateView.getRoleStore()
                .find(roleId.asLong())
                .map(data -> new Role(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<User> getUserById(Snowflake userId) {
        return stateView.getUserStore()
            .find(userId.asLong())
            .map(data -> new User(gateway, data));
    }

    @Override
    public Mono<Template> getTemplateByCode(String code) {
        return stateView.getTemplateStore()
            .find(code)
            .map(data -> new Template(gateway, data));
    }

    @Override
    public Flux<Guild> getGuilds() {
        return stateView.getGuildStore().values().map(data -> new Guild(gateway, data));
    }

    @Override
    public Flux<Template> getGuildTemplates(Snowflake guildId) {
        return stateView.getTemplateStore().values().map(data -> new Template(gateway, data));
    }

    @Override
    public Mono<User> getSelf() {
        return getUserById(gateway.getSelfId());
    }

    @Override
    public Flux<Member> getGuildMembers(Snowflake guildId) {
        return stateView.getGuildStore().find(guildId.asLong())
                .filterWhen(guild -> Mono.justOrEmpty(gateway.getGatewayResources().getIntents().toOptional())
                        .filter(intents -> intents.contains(Intent.GUILDS))
                        .flatMap(intents -> new Guild(gateway, guild).getVoiceStates().count())
                        .map(count -> guild.memberCount() - count == 1)
                        .defaultIfEmpty(true)) // Fix for https://github.com/Discord4J/Discord4J/issues/708
                .flatMapMany(guildData -> Flux.fromIterable(guildData.members())
                        .flatMap(memberId -> stateView.getMemberStore()
                                .find(LongLongTuple2.of(guildId.asLong(), Snowflake.asLong(memberId))))
                        .map(member -> new Member(gateway, member, guildId.asLong())))
                .switchIfEmpty(gateway.requestMembers(guildId))
                .onErrorResume(TimeoutException.class, t -> Mono.empty());
    }

    @Override
    public Flux<GuildChannel> getGuildChannels(Snowflake guildId) {
        return stateView.getGuildStore().find(guildId.asLong())
                .flatMapMany(guildData -> Flux.fromIterable(guildData.channels())
                        .flatMap(channelId -> stateView.getChannelStore().find(Snowflake.asLong(channelId)))
                        .map(channelData -> EntityUtil.getChannel(gateway, channelData))
                        .cast(GuildChannel.class));
    }

    @Override
    public Flux<Role> getGuildRoles(Snowflake guildId) {
        return stateView.getGuildStore().find(guildId.asLong())
                .flatMapMany(guildData -> Flux.fromIterable(guildData.roles())
                        .flatMap(roleId -> stateView.getRoleStore().find(Snowflake.asLong(roleId)))
                        .map(roleData -> new Role(gateway, roleData, guildId.asLong())));
    }

    @Override
    public Flux<GuildEmoji> getGuildEmojis(Snowflake guildId) {
        return stateView.getGuildStore().find(guildId.asLong())
                .flatMapMany(guildData -> Flux.fromIterable(guildData.emojis())
                        .flatMap(emojiId -> stateView.getGuildEmojiStore().find(Snowflake.asLong(emojiId)))
                        .map(emojiData -> new GuildEmoji(gateway, emojiData, guildId.asLong())));
    }
}
