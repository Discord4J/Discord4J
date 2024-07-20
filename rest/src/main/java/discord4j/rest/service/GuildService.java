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

import discord4j.discordjson.json.*;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.util.Multimap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Map;

public class GuildService extends RestService {

    public GuildService(Router router) {
        super(router);
    }

    public Mono<GuildUpdateData> createGuild(GuildCreateRequest request) {
        return Routes.GUILD_CREATE.newRequest()
            .body(request)
            .exchange(getRouter())
            .bodyToMono(GuildUpdateData.class);
    }

    public Mono<GuildUpdateData> getGuild(long guildId, Map<String, Object> queryParams) {
        return Routes.GUILD_GET.newRequest(guildId)
            .query(queryParams)
            .exchange(getRouter())
            .bodyToMono(GuildUpdateData.class);
    }

    public Mono<GuildUpdateData> getGuild(long guildId) {
        return Routes.GUILD_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(GuildUpdateData.class);
    }

    public Mono<GuildUpdateData> modifyGuild(long guildId, GuildModifyRequest request, @Nullable String reason) {
        return Routes.GUILD_MODIFY.newRequest(guildId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(GuildUpdateData.class);
    }

    public Mono<Void> deleteGuild(long guildId) {
        return Routes.GUILD_DELETE.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Flux<ChannelData> getGuildChannels(long guildId) {
        return Routes.GUILD_CHANNELS_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(ChannelData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<ChannelData> createGuildChannel(long guildId, ChannelCreateRequest request, @Nullable String reason) {
        return Routes.GUILD_CHANNEL_CREATE.newRequest(guildId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(ChannelData.class);
    }

    public Flux<Void> modifyGuildChannelPositions(long guildId, ChannelPositionModifyRequest[] request) {
        return Routes.GUILD_CHANNEL_POSITIONS_MODIFY.newRequest(guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(Void[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<MemberData> getGuildMember(long guildId, long userId) {
        return Routes.GUILD_MEMBER_GET.newRequest(guildId, userId)
            .exchange(getRouter())
            .bodyToMono(MemberData.class);
    }

    public Flux<MemberData> getGuildMembers(long guildId, Map<String, Object> queryParams) {
        return Routes.GUILD_MEMBERS_LIST.newRequest(guildId)
            .query(queryParams)
            .exchange(getRouter())
            .bodyToMono(MemberData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Flux<MemberData> searchGuildMembers(long guildId, Map<String, Object> queryParams) {
        return Routes.SEARCH_GUILD_MEMBERS_GET.newRequest(guildId)
            .query(queryParams)
            .exchange(getRouter())
            .bodyToMono(MemberData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<MemberData> addGuildMember(long guildId, long userId, GuildMemberAddRequest request) {
        return Routes.GUILD_MEMBER_ADD.newRequest(guildId, userId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(MemberData.class);
    }

    public Mono<MemberData> modifyGuildMember(long guildId, long userId, GuildMemberModifyRequest request,
                                              @Nullable String reason) {
        return Routes.GUILD_MEMBER_MODIFY.newRequest(guildId, userId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(MemberData.class);
    }

    public Mono<MemberData> modifyCurrentMember(long guildId, CurrentMemberModifyData request) {
        return Routes.CURRENT_MEMBER_MODIFY.newRequest(guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(MemberData.class);
    }

    public Mono<Void> addGuildMemberRole(long guildId, long userId, long roleId, @Nullable String reason) {
        return Routes.GUILD_MEMBER_ROLE_ADD.newRequest(guildId, userId, roleId)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<Void> removeGuildMemberRole(long guildId, long userId, long roleId, @Nullable String reason) {
        return Routes.GUILD_MEMBER_ROLE_REMOVE.newRequest(guildId, userId, roleId)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<Void> removeGuildMember(long guildId, long userId, @Nullable String reason) {
        return Routes.GUILD_MEMBER_REMOVE.newRequest(guildId, userId)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Flux<BanData> getGuildBans(long guildId) {
        return Routes.GUILD_BANS_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(BanData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<BanData> getGuildBan(long guildId, long userId) {
        return Routes.GUILD_BAN_GET.newRequest(guildId, userId)
            .exchange(getRouter())
            .bodyToMono(BanData.class);
    }

    public Mono<Void> createGuildBan(long guildId, long userId, Map<String, Object> queryParams,
                                     @Nullable String reason) {
        return Routes.GUILD_BAN_CREATE.newRequest(guildId, userId)
            .query(queryParams)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<Void> removeGuildBan(long guildId, long userId, @Nullable String reason) {
        return Routes.GUILD_BAN_REMOVE.newRequest(guildId, userId)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<BulkBanResponseData> bulkGuildBan(long guildId, BulkBanRequest request, @Nullable String reason) {
        return Routes.GUILD_BAN_BULK.newRequest(guildId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(BulkBanResponseData.class);
    }

    public Flux<RoleData> getGuildRoles(long guildId) {
        return Routes.GUILD_ROLES_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(RoleData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<RoleData> createGuildRole(long guildId, RoleCreateRequest request, @Nullable String reason) {
        return Routes.GUILD_ROLE_CREATE.newRequest(guildId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(RoleData.class);
    }

    public Flux<RoleData> modifyGuildRolePositions(long guildId, RolePositionModifyRequest[] request) {
        return modifyGuildRolePositions(guildId, request, null);
    }

    public Flux<RoleData> modifyGuildRolePositions(long guildId, RolePositionModifyRequest[] request,
                                                   @Nullable String reason) {
        return Routes.GUILD_ROLE_POSITIONS_MODIFY.newRequest(guildId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(RoleData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<RoleData> modifyGuildRole(long guildId, long roleId, RoleModifyRequest request,
                                          @Nullable String reason) {
        return Routes.GUILD_ROLE_MODIFY.newRequest(guildId, roleId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(RoleData.class);
    }

    public Mono<Void> deleteGuildRole(long guildId, long roleId, @Nullable String reason) {
        return Routes.GUILD_ROLE_DELETE.newRequest(guildId, roleId)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    @Deprecated
    public Mono<PruneData> getGuildPruneCount(long guildId, Map<String, Object> queryParams) {
        return Routes.GUILD_PRUNE_COUNT_GET.newRequest(guildId)
            .query(queryParams)
            .exchange(getRouter())
            .bodyToMono(PruneData.class);
    }

    public Mono<PruneData> getGuildPruneCount(long guildId, Multimap<String, Object> params) {
        return Routes.GUILD_PRUNE_COUNT_GET.newRequest(guildId)
            .query(params)
            .exchange(getRouter())
            .bodyToMono(PruneData.class);
    }

    @Deprecated
    public Mono<PruneData> beginGuildPrune(long guildId, Map<String, Object> queryParams, @Nullable String reason) {
        return Routes.GUILD_PRUNE_BEGIN.newRequest(guildId)
            .query(queryParams)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(PruneData.class);
    }

    public Mono<PruneData> beginGuildPrune(long guildId, Multimap<String, Object> params, @Nullable String reason) {
        return Routes.GUILD_PRUNE_BEGIN.newRequest(guildId)
            .query(params)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(PruneData.class);
    }

    public Flux<RegionData> getGuildVoiceRegions(long guildId) {
        return Routes.GUILD_VOICE_REGIONS_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(RegionData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Flux<InviteData> getGuildInvites(long guildId) {
        return Routes.GUILD_INVITES_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(InviteData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Flux<IntegrationData> getGuildIntegrations(long guildId) {
        return Routes.GUILD_INTEGRATIONS_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(IntegrationData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<Void> createGuildIntegration(long guildId, IntegrationCreateRequest request) {
        return Routes.GUILD_INTEGRATION_CREATE.newRequest(guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<Void> modifyGuildIntegration(long guildId, long integrationId, IntegrationModifyRequest request) {
        return Routes.GUILD_INTEGRATION_MODIFY.newRequest(guildId, integrationId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<Void> deleteGuildIntegration(long guildId, long integrationId) {
        return Routes.GUILD_INTEGRATION_DELETE.newRequest(guildId, integrationId)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<Void> syncGuildIntegration(long guildId, long integrationId) {
        return Routes.GUILD_INTEGRATION_SYNC.newRequest(guildId, integrationId)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<GuildWidgetData> getGuildWidget(long guildId) {
        return Routes.GUILD_WIDGET_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(GuildWidgetData.class);
    }

    public Mono<GuildWidgetData> modifyGuildWidget(long guildId, GuildWidgetModifyRequest request) {
        return Routes.GUILD_WIDGET_MODIFY.newRequest(guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(GuildWidgetData.class);
    }

    public Mono<GuildPreviewData> getGuildPreview(long guildId) {
        return Routes.GUILD_PREVIEW_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(GuildPreviewData.class);
    }

    public Mono<Void> modifySelfVoiceState(long guildId, UpdateCurrentUserVoiceStateRequest request) {
        return Routes.SELF_VOICE_STATE_MODIFY.newRequest(guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<Void> modifyOthersVoiceState(long guildId, long userId, UpdateUserVoiceStateRequest request) {
        return Routes.OTHERS_VOICE_STATE_MODIFY.newRequest(guildId, userId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<ListThreadsData> listActiveGuildThreads(long guildId) {
        return Routes.LIST_ACTIVE_GUILD_THREADS.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(ListThreadsData.class);
    }

    public Mono<GuildScheduledEventData> getScheduledEvent(long guildId, long eventId,
                                                           Map<String, Object> queryParams) {
        return Routes.GUILD_SCHEDULED_EVENT_GET.newRequest(guildId, eventId)
            .query(queryParams)
            .exchange(getRouter())
            .bodyToMono(GuildScheduledEventData.class);
    }

    public Flux<GuildScheduledEventData> getScheduledEvents(long guildId, Map<String, Object> queryParams) {
        return Routes.GUILD_SCHEDULED_EVENTS_GET.newRequest(guildId)
            .query(queryParams)
            .exchange(getRouter())
            .bodyToMono(GuildScheduledEventData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<GuildScheduledEventData> createScheduledEvent(long guildId, GuildScheduledEventCreateRequest request) {
        return Routes.GUILD_SCHEDULED_EVENT_CREATE.newRequest(guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(GuildScheduledEventData.class);
    }

    public Mono<GuildScheduledEventData> modifyScheduledEvent(long guildId, long eventId,
                                                              GuildScheduledEventModifyRequest request,
                                                              @Nullable String reason) {
        return Routes.GUILD_SCHEDULED_EVENT_MODIFY.newRequest(guildId, eventId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(GuildScheduledEventData.class);
    }

    public Mono<Void> deleteScheduledEvent(long guildId, long eventId, @Nullable String reason) {
        return Routes.GUILD_SCHEDULED_EVENT_DELETE.newRequest(guildId, eventId)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Flux<GuildScheduledEventUserData> getScheduledEventUsers(long guildId, long eventId,
                                                                    Map<String, Object> queryParams) {
        return Routes.GUILD_SCHEDULED_EVENT_USERS_GET.newRequest(guildId, eventId)
            .query(queryParams)
            .exchange(getRouter())
            .bodyToMono(GuildScheduledEventUserData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<OnboardingData> getOnboarding(long id) {
        return Routes.GUILD_ONBOARDING_GET.newRequest(id)
            .exchange(getRouter())
            .bodyToMono(OnboardingData.class);
    }

    public Mono<OnboardingData> modifyOnboarding(long id, OnboardingEditData request, @Nullable String reason) {
        return Routes.GUILD_ONBOARDING_MODIFY.newRequest(id)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(OnboardingData.class);
    }
}
