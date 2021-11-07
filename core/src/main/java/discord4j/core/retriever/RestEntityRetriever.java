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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.StageInstance;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.RoleData;
import discord4j.discordjson.json.UserGuildData;
import discord4j.rest.RestClient;
import discord4j.rest.util.PaginationUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RestEntityRetriever implements EntityRetriever {

    private final GatewayDiscordClient gateway;
    private final RestClient rest;

    public RestEntityRetriever(GatewayDiscordClient gateway) {
        this.gateway = gateway;
        this.rest = gateway.rest();
    }

    @Override
    public Mono<Channel> getChannelById(Snowflake channelId) {
        return rest.getChannelService()
                .getChannel(channelId.asLong())
                .map(data -> EntityUtil.getChannel(gateway, data));
    }

    @Override
    public Mono<Guild> getGuildById(Snowflake guildId) {
        return rest.getGuildService()
                .getGuild(guildId.asLong())
                .map(this::toGuildData)
                .map(data -> new Guild(gateway, data));
    }

    @Override
    public Mono<GuildEmoji> getGuildEmojiById(Snowflake guildId, Snowflake emojiId) {
        return rest.getEmojiService()
                .getGuildEmoji(guildId.asLong(), emojiId.asLong())
                .map(data -> new GuildEmoji(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<Member> getMemberById(Snowflake guildId, Snowflake userId) {
        return rest.getGuildService()
                .getGuildMember(guildId.asLong(), userId.asLong())
                .map(data -> new Member(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<Message> getMessageById(Snowflake channelId, Snowflake messageId) {
        return rest.getChannelService()
                .getMessage(channelId.asLong(), messageId.asLong())
                .map(data -> new Message(gateway, data));
    }

    @Override
    public Mono<Role> getRoleById(Snowflake guildId, Snowflake roleId) {
        return rest.getGuildService()
                .getGuildRoles(guildId.asLong())
                .filter(response -> response.id().asString().equals(roleId.asString()))
                .singleOrEmpty()
                .map(data -> new Role(gateway, data, guildId.asLong()));
    }

    @Override
    public Mono<User> getUserById(Snowflake userId) {
        return rest.getUserService()
                .getUser(userId.asLong())
                .map(data -> new User(gateway, data));
    }

    @Override
    public Flux<Guild> getGuilds() {
        final Function<Map<String, Object>, Flux<UserGuildData>> makeRequest = params ->
                rest.getUserService().getCurrentUserGuilds(params);
        return PaginationUtil.paginateAfter(makeRequest, data -> Snowflake.asLong(data.id()), 0L, 100)
                .map(UserGuildData::id)
                .flatMap(id -> rest.getGuildService().getGuild(Snowflake.asLong(id)))
                .map(this::toGuildData)
                .map(data -> new Guild(gateway, data));
    }

    @Override
    public Mono<User> getSelf() {
        return rest.getUserService().getCurrentUser().map(data -> new User(gateway, data));
    }

    @Override
    public Mono<Member> getSelfMember(Snowflake guildId) {
        return rest.getSelfMember(guildId).map(data -> new Member(gateway,data, guildId.asLong()));
    }

    @Override
    public Flux<Member> getGuildMembers(Snowflake guildId) {
        Function<Map<String, Object>, Flux<MemberData>> doRequest = params ->
                rest.getGuildService().getGuildMembers(guildId.asLong(), params);

       return PaginationUtil.paginateAfter(doRequest, data -> Snowflake.asLong(data.user().id()), 0, 100)
                        .map(data -> new Member(gateway, data, guildId.asLong()));
    }

    @Override
    public Flux<GuildChannel> getGuildChannels(Snowflake guildId) {
        return rest.getGuildService()
                .getGuildChannels(guildId.asLong())
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(GuildChannel.class);
    }

    @Override
    public Flux<Role> getGuildRoles(Snowflake guildId) {
        return rest.getGuildService()
                .getGuildRoles(guildId.asLong())
                .map(data -> new Role(gateway, data, guildId.asLong()));
    }

    @Override
    public Flux<GuildEmoji> getGuildEmojis(Snowflake guildId) {
        return rest.getEmojiService()
                .getGuildEmojis(guildId.asLong())
                .map(data -> new GuildEmoji(gateway, data, guildId.asLong()));
    }

    private GuildData toGuildData(GuildUpdateData guild) {
        return GuildData.builder()
                .from(guild)
                .roles(guild.roles().stream()
                        .map(RoleData::id)
                        .collect(Collectors.toList()))
                .emojis(guild.emojis().stream()
                        .map(EmojiData::id)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()))
                .channels(Collections.emptyList()) // Can be retrieved with getGuildChannels(id)
                .members(Collections.emptyList()) // Can be retrieved with getGuildMembers(id)
                .joinedAt("") // unable to retrieve this data
                .large(false) // unable to retrieve this data
                .memberCount(0) // unable to retrieve this data
                .build();
    }

    @Override
    public Mono<StageInstance> getStageInstanceByChannelId(Snowflake channelId) {
        return rest.getStageInstanceService()
            .getStageInstance(channelId.asLong())
            .map(data -> new StageInstance(gateway, data));
    }
}
