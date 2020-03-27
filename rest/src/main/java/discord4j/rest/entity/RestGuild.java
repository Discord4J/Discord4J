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

package discord4j.rest.entity;

import discord4j.discordjson.json.*;
import discord4j.rest.RestClient;
import discord4j.rest.util.PaginationUtil;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RestGuild {

    private final RestClient restClient;
    private final long id;

    private RestGuild(RestClient restClient, long id) {
        this.restClient = restClient;
        this.id = id;
    }

    public static RestGuild create(RestClient restClient, Snowflake id) {
        return new RestGuild(restClient, id.asLong());
    }

    public static RestGuild create(RestClient restClient, long id) {
        return new RestGuild(restClient, id);
    }

    public Mono<GuildUpdateData> getData() {
        return restClient.getGuildService().getGuild(id);
    }

    public RestEmoji emoji(long emojiId) {
        return RestEmoji.create(restClient, id, emojiId);
    }

    public RestMember member(long memberId) {
        return RestMember.create(restClient, id, memberId);
    }

    public RestRole role(long roleId) {
        return RestRole.create(restClient, id, roleId);
    }

    public Mono<GuildUpdateData> modify(GuildModifyRequest request, @Nullable String reason) {
        return restClient.getGuildService().modifyGuild(id, request, reason);
    }

    public Mono<Void> delete() {
        return restClient.getGuildService().deleteGuild(id);
    }

    public Flux<ChannelData> getChannels() {
        return restClient.getGuildService().getGuildChannels(id);
    }

    public Mono<ChannelData> createChannel(ChannelCreateRequest request, @Nullable String reason) {
        return restClient.getGuildService().createGuildChannel(id, request, reason);
    }

    public Flux<RoleData> modifyChannelPositions(List<PositionModifyRequest> requests) {
        return restClient.getGuildService()
                .modifyGuildChannelPositions(id, requests.toArray(new PositionModifyRequest[0]));
    }

    public Mono<MemberData> getMember(long userId) {
        return restClient.getGuildService().getGuildMember(id, userId);
    }

    public Flux<MemberData> getMembers() {
        Function<Map<String, Object>, Flux<MemberData>> doRequest =
                params -> restClient.getGuildService().getGuildMembers(id, params);
        return PaginationUtil.paginateAfter(doRequest, data -> Snowflake.asLong(data.user().id()), 0, 100);
    }

    public Mono<MemberData> addMember(long userId, GuildMemberAddRequest request) {
        return restClient.getGuildService().addGuildMember(id, userId, request);
    }

    public Mono<Void> modifyMember(long userId, GuildMemberModifyRequest request, @Nullable String reason) {
        return restClient.getGuildService().modifyGuildMember(id, userId, request, reason);
    }

    public Mono<NicknameModifyData> modifyOwnNickname(NicknameModifyData request) {
        return restClient.getGuildService().modifyOwnNickname(id, request);
    }

    public Mono<Void> addMemberRole(long userId, long roleId, @Nullable String reason) {
        return restClient.getGuildService().addGuildMemberRole(id, userId, roleId, reason);
    }

    public Mono<Void> removeMemberRole(long userId, long roleId, @Nullable String reason) {
        return restClient.getGuildService().removeGuildMemberRole(id, userId, roleId, reason);
    }

    public Mono<Void> removeGuildMember(long userId, @Nullable String reason) {
        return restClient.getGuildService().removeGuildMember(id, userId, reason);
    }

    public Flux<BanData> getBans() {
        return restClient.getGuildService().getGuildBans(id);
    }

    public Mono<BanData> getBan(long userId) {
        return restClient.getGuildService().getGuildBan(id, userId);
    }

    public Mono<Void> createBan(long userId, @Nullable Integer deleteMessageDays, @Nullable String reason) {
        Map<String, Object> queryParams = new HashMap<>();
        Optional.ofNullable(deleteMessageDays).ifPresent(value -> queryParams.put("delete-message-days", value));
        Optional.ofNullable(reason).ifPresent(value -> queryParams.put("reason", value));
        return restClient.getGuildService().createGuildBan(id, userId, queryParams, reason);
    }

    public Mono<Void> removeGuildBan(long userId, @Nullable String reason) {
        return restClient.getGuildService().removeGuildBan(id, userId, reason);
    }

    public Flux<RoleData> getRoles() {
        return restClient.getGuildService().getGuildRoles(id);
    }

    public Mono<RoleData> createRole(RoleCreateRequest request, @Nullable String reason) {
        return restClient.getGuildService().createGuildRole(id, request, reason);
    }

    public Flux<RoleData> modifyRolePositions(List<PositionModifyRequest> requests) {
        return restClient.getGuildService().modifyGuildRolePositions(id,
                requests.toArray(new PositionModifyRequest[0]));
    }

    public Mono<RoleData> modifyRole(long roleId, RoleModifyRequest request, @Nullable String reason) {
        return restClient.getGuildService().modifyGuildRole(id, roleId, request, reason);
    }

    public Mono<Void> deleteRole(long roleId, @Nullable String reason) {
        return restClient.getGuildService().deleteGuildRole(id, roleId, reason);
    }

    public Mono<PruneData> getPruneCount(@Nullable Integer days) {
        Map<String, Object> queryParams = new HashMap<>();
        Optional.ofNullable(days).ifPresent(value -> queryParams.put("days", value));
        return restClient.getGuildService().getGuildPruneCount(id, queryParams);
    }

    public Mono<PruneData> beginGuildPrune(long guildId, @Nullable Integer days, @Nullable Boolean computePruneCount,
                                           @Nullable String reason) {
        Map<String, Object> queryParams = new HashMap<>();
        Optional.ofNullable(days).ifPresent(value -> queryParams.put("days", value));
        Optional.ofNullable(computePruneCount).ifPresent(value -> queryParams.put("compute_prune_count", value));
        return restClient.getGuildService().beginGuildPrune(id, queryParams, reason);
    }

    public Flux<RegionData> getRegions() {
        return restClient.getGuildService().getGuildVoiceRegions(id);
    }

    public Flux<InviteData> getInvites() {
        return restClient.getGuildService().getGuildInvites(id);
    }

    public Flux<IntegrationData> getIntegrations() {
        return restClient.getGuildService().getGuildIntegrations(id);
    }

    public Mono<Void> createIntegration(IntegrationCreateRequest request) {
        return restClient.getGuildService().createGuildIntegration(id, request);
    }

    public Mono<Void> modifyIntegration(long integrationId, IntegrationModifyRequest request) {
        return restClient.getGuildService().modifyGuildIntegration(id, integrationId, request);
    }

    public Mono<Void> deleteIntegration(long integrationId) {
        return restClient.getGuildService().deleteGuildIntegration(id, integrationId);
    }

    public Mono<Void> syncIntegration(long integrationId) {
        return restClient.getGuildService().syncGuildIntegration(id, integrationId);
    }

    public Mono<GuildEmbedData> getEmbed() {
        return restClient.getGuildService().getGuildEmbed(id);
    }

    public Mono<GuildEmbedData> modifyEmbed(GuildEmbedModifyRequest request) {
        return restClient.getGuildService().modifyGuildEmbed(id, request);
    }

    // TODO add Get Guild Vanity URL
    // TODO add Get Guild Widget Image

    public Flux<EmojiData> getEmojis() {
        return restClient.getEmojiService().getGuildEmojis(id);
    }

    public Mono<EmojiData> createEmoji(GuildEmojiCreateRequest request, @Nullable String reason) {
        return restClient.getEmojiService().createGuildEmoji(id, request, reason);
    }

    public Flux<WebhookData> getWebhooks() {
        return restClient.getWebhookService().getGuildWebhooks(id);
    }
}
