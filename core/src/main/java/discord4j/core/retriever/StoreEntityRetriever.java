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
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.util.EntityUtil;
import discord4j.store.action.read.*;
import discord4j.store.api.wip.Store;
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
        return store.execute(new GetChannelByIdAction(channelId.asLong()))
                .map(data -> EntityUtil.getChannel(gateway, data));
    }

    @Override
    public Mono<Guild> getGuildById(Snowflake guildId) {
        return store.execute(new GetGuildByIdAction(guildId.asLong()))
                .map(data -> new Guild(gateway, data));
    }

    @Override
    public Mono<GuildEmoji> getGuildEmojiById(Snowflake guildId, Snowflake emojiId) {
        return store.execute(new GetGuildEmojiByIdAction(guildId.asLong(), emojiId.asLong()))
                .map(data -> new GuildEmoji(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<Member> getMemberById(Snowflake guildId, Snowflake userId) {
        return store.execute(new GetMemberByIdAction(guildId.asLong(), userId.asLong()))
                .map(data -> new Member(gateway, data, guildId.asLong()))
                .switchIfEmpty(gateway.requestMembers(guildId, Collections.singleton(userId))
                        .filter(member -> member.getId().equals(userId))
                        .next());
    }

    @Override
    public Mono<Message> getMessageById(Snowflake channelId, Snowflake messageId) {
        return store.execute(new GetMessageByIdAction(channelId.asLong(), messageId.asLong()))
                .map(data -> new Message(gateway, data));
    }

    @Override
    public Mono<Role> getRoleById(Snowflake guildId, Snowflake roleId) {
        return store.execute(new GetRoleByIdAction(guildId.asLong(), roleId.asLong()))
                .map(data -> new Role(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<User> getUserById(Snowflake userId) {
        return store.execute(new GetUserByIdAction(userId.asLong()))
                .map(data -> new User(gateway, data));
    }

    @Override
    public Flux<Guild> getGuilds() {
        return store.execute(new GetGuildsAction())
                .flatMapMany(Flux::fromIterable)
                .map(data -> new Guild(gateway, data));
    }

    @Override
    public Mono<User> getSelf() {
        return getUserById(gateway.getSelfId());
    }

    @Override
    public Flux<Member> getGuildMembers(Snowflake guildId) {
        return store.execute(new GetGuildMembersAction(guildId.asLong()))
                .flatMapMany(list -> {
                    if (list.isComplete()) {
                        return Flux.fromIterable(list)
                                .map(data -> new Member(gateway, data, guildId.asLong()));
                    }
                    return gateway.requestMembers(guildId);
                });
    }

    @Override
    public Flux<GuildChannel> getGuildChannels(Snowflake guildId) {
        return store.execute(new GetGuildChannelsAction(guildId.asLong()))
                .flatMapMany(Flux::fromIterable)
                .map(channelData -> EntityUtil.getChannel(gateway, channelData))
                .cast(GuildChannel.class);
    }

    @Override
    public Flux<Role> getGuildRoles(Snowflake guildId) {
        return store.execute(new GetGuildRolesAction(guildId.asLong()))
                .flatMapMany(Flux::fromIterable)
                .map(roleData -> new Role(gateway, roleData, guildId.asLong()));
    }

    @Override
    public Flux<GuildEmoji> getGuildEmojis(Snowflake guildId) {
        return store.execute(new GetGuildEmojisAction(guildId.asLong()))
                .flatMapMany(Flux::fromIterable)
                .map(emojiData -> new GuildEmoji(gateway, emojiData, guildId.asLong()));
    }
}
