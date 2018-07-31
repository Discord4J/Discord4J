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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.service;

import discord4j.common.json.GuildMemberResponse;
import discord4j.common.json.RoleResponse;
import discord4j.rest.json.request.*;
import discord4j.rest.json.response.*;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class GuildService extends RestService {

    public GuildService(Router router) {
        super(router);
    }

    public Mono<GuildResponse> createGuild(GuildCreateRequest request) {
        return Routes.GUILD_CREATE.newRequest()
                .body(request)
                .exchange(getRouter());
    }

    public Mono<GuildResponse> getGuild(long guildId) {
        return Routes.GUILD_GET.newRequest(guildId)
                .exchange(getRouter());
    }

    public Mono<GuildResponse> modifyGuild(long guildId, GuildModifyRequest request) {
        return Routes.GUILD_MODIFY.newRequest(guildId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> deleteGuild(long guildId) {
        return Routes.GUILD_DELETE.newRequest(guildId)
                .exchange(getRouter());
    }

    public Flux<ChannelResponse> getGuildChannels(long guildId) {
        return Routes.GUILD_CHANNELS_GET.newRequest(guildId)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<ChannelResponse> createGuildChannel(long guildId, ChannelCreateRequest request) {
        return Routes.GUILD_CHANNEL_CREATE.newRequest(guildId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> modifyGuildChannelPositions(long guildId, PositionModifyRequest[] request) {
        return Routes.GUILD_CHANNEL_POSITIONS_MODIFY.newRequest(guildId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<GuildMemberResponse> getGuildMember(long guildId, long userId) {
        return Routes.GUILD_MEMBER_GET.newRequest(guildId, userId)
                .exchange(getRouter());
    }

    public Flux<GuildMemberResponse> getGuildMembers(long guildId, Map<String, Object> queryParams) {
        return Routes.GUILD_MEMBERS_LIST.newRequest(guildId)
                .query(queryParams)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<GuildMemberResponse> addGuildMember(long guildId, long userId, GuildMemberAddRequest request) {
        return Routes.GUILD_MEMBER_ADD.newRequest(guildId, userId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> modifyGuildMember(long guildId, long userId, GuildMemberModifyRequest request) {
        return Routes.GUILD_MEMBER_MODIFY.newRequest(guildId, userId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<NicknameModifyResponse> modifyOwnNickname(long guildId, NicknameModifyRequest request) {
        return Routes.NICKNAME_MODIFY_OWN.newRequest(guildId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> addGuildMemberRole(long guildId, long userId, long roleId) {
        return Routes.GUILD_MEMBER_ROLE_ADD.newRequest(guildId, userId, roleId)
                .exchange(getRouter());
    }

    public Mono<Void> removeGuildMemberRole(long guildId, long userId, long roleId) {
        return Routes.GUILD_MEMBER_ROLE_REMOVE.newRequest(guildId, userId, roleId)
                .exchange(getRouter());
    }

    public Mono<Void> removeGuildMember(long guildId, long userId) {
        return Routes.GUILD_MEMBER_REMOVE.newRequest(guildId, userId)
                .exchange(getRouter());
    }

    public Flux<BanResponse> getGuildBans(long guildId) {
        return Routes.GUILD_BANS_GET.newRequest(guildId)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<BanResponse> getGuildBan(long guildId, long userId) {
        return Routes.GUILD_BAN_GET.newRequest(guildId, userId)
                .exchange(getRouter());
    }

    public Mono<Void> createGuildBan(long guildId, long userId, Map<String, Object> queryParams) {
        return Routes.GUILD_BAN_CREATE.newRequest(guildId, userId)
                .query(queryParams)
                .exchange(getRouter());
    }

    public Mono<Void> removeGuildBan(long guildId, long userId) {
        return Routes.GUILD_BAN_REMOVE.newRequest(guildId, userId)
                .exchange(getRouter());
    }

    public Flux<RoleResponse> getGuildRoles(long guildId) {
        return Routes.GUILD_ROLES_GET.newRequest(guildId)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<RoleResponse> createGuildRole(long guildId, RoleCreateRequest request) {
        return Routes.GUILD_ROLE_CREATE.newRequest(guildId)
                .body(request)
                .exchange(getRouter());
    }

    public Flux<RoleResponse> modifyGuildRolePositions(long guildId, PositionModifyRequest[] request) {
        return Routes.GUILD_ROLE_POSITIONS_MODIFY.newRequest(guildId)
                .body(request)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<RoleResponse> modifyGuildRole(long guildId, long roleId, RoleModifyRequest request) {
        return Routes.GUILD_ROLE_MODIFY.newRequest(guildId, roleId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> deleteGuildRole(long guildId, long roleId) {
        return Routes.GUILD_ROLE_DELETE.newRequest(guildId, roleId)
                .exchange(getRouter());
    }

    public Mono<PruneResponse> getGuildPruneCount(long guildId, Map<String, Object> queryParams) {
        return Routes.GUILD_PRUNE_COUNT_GET.newRequest(guildId)
                .query(queryParams)
                .exchange(getRouter());
    }

    public Mono<PruneResponse> beginGuildPrune(long guildId, Map<String, Object> queryParams) {
        return Routes.GUILD_PRUNE_BEGIN.newRequest(guildId)
                .query(queryParams)
                .exchange(getRouter());
    }

    public Flux<VoiceRegionResponse> getGuildVoiceRegions(long guildId) {
        return Routes.GUILD_VOICE_REGIONS_GET.newRequest(guildId)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Flux<InviteResponse> getGuildInvites(long guildId) {
        return Routes.GUILD_INVITES_GET.newRequest(guildId)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Flux<IntegrationResponse> getGuildIntegrations(long guildId) {
        return Routes.GUILD_INTEGRATIONS_GET.newRequest(guildId)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<Void> createGuildIntegration(long guildId, IntegrationCreateRequest request) {
        return Routes.GUILD_INTEGRATION_CREATE.newRequest(guildId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> modifyGuildIntegration(long guildId, long integrationId, IntegrationModifyRequest request) {
        return Routes.GUILD_INTEGRATION_MODIFY.newRequest(guildId, integrationId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> deleteGuildIntegration(long guildId, long integrationId) {
        return Routes.GUILD_INTEGRATION_DELETE.newRequest(guildId, integrationId)
                .exchange(getRouter());
    }

    public Mono<Void> syncGuildIntegration(long guildId, long integrationId) {
        return Routes.GUILD_INTEGRATION_SYNC.newRequest(guildId, integrationId)
                .exchange(getRouter());
    }

    public Mono<GuildEmbedResponse> getGuildEmbed(long guildId) {
        return Routes.GUILD_EMBED_GET.newRequest(guildId)
                .exchange(getRouter());
    }

    public Mono<GuildEmbedResponse> modifyGuildEmbed(long guildId, GuildEmbedModifyRequest request) {
        return Routes.GUILD_EMBED_MODIFY.newRequest(guildId)
                .body(request)
                .exchange(getRouter());
    }
}
