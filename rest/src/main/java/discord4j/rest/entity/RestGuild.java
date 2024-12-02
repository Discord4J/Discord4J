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

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.*;
import discord4j.rest.RestClient;
import discord4j.rest.route.Routes;
import discord4j.rest.util.PaginationUtil;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a guild entity in Discord. Guilds in Discord represent an isolated collection of users and channels,
 * and are often referred to as "servers" in the UI.
 */
public class RestGuild {

    private final RestClient restClient;
    private final long id;

    private RestGuild(RestClient restClient, long id) {
        this.restClient = restClient;
        this.id = id;
    }

    /**
     * Create a {@link RestGuild} for a given ID. This method does not perform any API request.
     *
     * @param restClient the client to make API requests
     * @param id the ID of this entity
     * @return a {@code RestGuild} represented by this {@code id}.
     */
    public static RestGuild create(RestClient restClient, Snowflake id) {
        return new RestGuild(restClient, id.asLong());
    }

    static RestGuild create(RestClient restClient, long id) {
        return new RestGuild(restClient, id);
    }

    /**
     * Returns the ID of this guild.
     *
     * @return The ID of this guild
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    /**
     * Retrieve this guild's data upon subscription.
     *
     * @param withCounts when true, will return approximate member and presence counts for the guild too.
     * otherwise approximate member and presence counts will be null in {@link GuildUpdateData}.
     * @return a {@link Mono} where, upon successful completion, emits the {@link GuildUpdateData} belonging to this
     * entity. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildUpdateData> getData(@Nullable Boolean withCounts) {
        Map<String, Object> queryParams = new HashMap<>();
        Optional.ofNullable(withCounts).ifPresent(value -> queryParams.put("with_counts", value));
        return restClient.getGuildService().getGuild(id, queryParams);
    }

    /**
     * Retrieve this guild's data upon subscription.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link GuildUpdateData} belonging to this
     * entity. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildUpdateData> getData() {
        return getData(true);
    }

    /**
     * Return a {@link RestEmoji} representation under this guild. This method does not perform any API request.
     *
     * @param emojiId the entity ID
     * @return a {@code RestEmoji} with the given ID, under this guild
     */
    public RestEmoji emoji(Snowflake emojiId) {
        return RestEmoji.create(restClient, id, emojiId.asLong());
    }

    /**
     * Return a {@link RestMember} representation under this guild. This method does not perform any API request.
     *
     * @param memberId the entity ID
     * @return a {@code RestMember} with the given ID, under this guild
     */
    public RestMember member(Snowflake memberId) {
        return RestMember.create(restClient, id, memberId.asLong());
    }

    /**
     * Return a {@link RestRole} representation under this guild. This method does not perform any API request.
     *
     * @param roleId the entity ID
     * @return a {@code RestRole} with the given ID, under this guild
     */
    public RestRole role(Snowflake roleId) {
        return RestRole.create(restClient, id, roleId.asLong());
    }

    /**
     * Returns a {@link RestScheduledEvent} representation under this guild.
     * This method does not perform any API request.
     *
     * @param eventId The entity ID
     * @return a {@code RestGuildScheduledEvent} with the given ID, under this guild
     */
    public RestScheduledEvent scheduledEvent(Snowflake eventId) {
        return RestScheduledEvent.create(restClient, id, eventId.asLong());
    }

    /**
     * Modify a guild's settings. Requires the {@link Permission#MANAGE_GUILD} permission. Returns the updated guild
     * object on success.
     *
     * @param request the modify request body
     * @param reason an optional reason for the audit log
     * @return a {@link Mono} where, upon subscription, emits the updated {@link GuildUpdateData} on success. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildUpdateData> modify(GuildModifyRequest request, @Nullable String reason) {
        return restClient.getGuildService().modifyGuild(id, request, reason);
    }

    /**
     * Delete a guild permanently. Requires the {@link Permission#MANAGE_GUILD} permission. Returns empty on success.
     *
     * @return a {@link Mono} where, upon subscription, emits a complete signal on success. If an error is received, it
     * is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return restClient.getGuildService().deleteGuild(id);
    }

    /**
     * Return a {@link Flux} of guild channels.
     *
     * @return a sequence of this guild channels
     */
    public Flux<ChannelData> getChannels() {
        return restClient.getGuildService().getGuildChannels(id);
    }

    /**
     * Create a new channel object for the guild. Requires the {@link Permission#MANAGE_CHANNELS} permission. Returns
     * the new channel object on success.
     *
     * @param request the request body
     * @param reason an optional reason for the audit log
     * @return a {@link Mono} where, upon subscription, emits the created {@link ChannelData} on success. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<ChannelData> createChannel(ChannelCreateRequest request, @Nullable String reason) {
        return restClient.getGuildService().createGuildChannel(id, request, reason);
    }

    public Flux<Void> modifyChannelPositions(List<ChannelPositionModifyRequest> requests) {
        return restClient.getGuildService()
            .modifyGuildChannelPositions(id, requests.toArray(new ChannelPositionModifyRequest[0]));
    }

    public Mono<MemberData> getMember(Snowflake userId) {
        return restClient.getGuildService().getGuildMember(id, userId.asLong());
    }

    public Mono<MemberData> getSelfMember() {
        return restClient.getSelf()
                .map(UserData::id)
                .map(Snowflake::of)
                .flatMap(this::getMember);
    }

    public Flux<MemberData> getMembers() {
        Function<Map<String, Object>, Flux<MemberData>> doRequest =
                params -> restClient.getGuildService().getGuildMembers(id, params);
        return PaginationUtil.paginateAfter(doRequest, data -> Snowflake.asLong(data.user().id()), 0, 1000);
    }

    public Flux<MemberData> searchMembers(Map<String, Object> queryParams) {
        return restClient.getGuildService().searchGuildMembers(id, queryParams);
    }

    public Mono<MemberData> addMember(Snowflake userId, GuildMemberAddRequest request) {
        return restClient.getGuildService().addGuildMember(id, userId.asLong(), request);
    }

    public Mono<MemberData> modifyMember(Snowflake userId, GuildMemberModifyRequest request, @Nullable String reason) {
        return restClient.getGuildService().modifyGuildMember(id, userId.asLong(), request, reason);
    }

    public Mono<MemberData> modifyCurrentMember(CurrentMemberModifyData request) {
        return restClient.getGuildService().modifyCurrentMember(id, request);
    }

    public Mono<Void> addMemberRole(Snowflake userId, Snowflake roleId, @Nullable String reason) {
        return restClient.getGuildService().addGuildMemberRole(id, userId.asLong(), roleId.asLong(), reason);
    }

    public Mono<Void> removeMemberRole(Snowflake userId, Snowflake roleId, @Nullable String reason) {
        return restClient.getGuildService().removeGuildMemberRole(id, userId.asLong(), roleId.asLong(), reason);
    }

    public Mono<Void> removeGuildMember(Snowflake userId, @Nullable String reason) {
        return restClient.getGuildService().removeGuildMember(id, userId.asLong(), reason);
    }

    public Flux<BanData> getBans() {
        return restClient.getGuildService().getGuildBans(id);
    }

    public Mono<BanData> getBan(Snowflake userId) {
        return restClient.getGuildService().getGuildBan(id, userId.asLong());
    }

    public Mono<Void> createBan(Snowflake userId, @Nullable Integer deleteMessageDays, @Nullable String reason) {
        Map<String, Object> queryParams = new HashMap<>();
        Optional.ofNullable(deleteMessageDays).ifPresent(value -> queryParams.put("delete_message_days", value));
        Optional.ofNullable(reason).ifPresent(value -> queryParams.put("reason", value));
        return restClient.getGuildService().createGuildBan(id, userId.asLong(), queryParams, reason);
    }

    public Mono<Void> removeGuildBan(Snowflake userId, @Nullable String reason) {
        return restClient.getGuildService().removeGuildBan(id, userId.asLong(), reason);
    }

    public Mono<BulkBanResponseData> bulkGuildBan(BulkBanRequest request, @Nullable String reason) {
        return restClient.getGuildService().bulkGuildBan(id, request, reason);
    }

    public Flux<RoleData> getRoles() {
        return restClient.getGuildService().getGuildRoles(id);
    }

    public Mono<RoleData> createRole(RoleCreateRequest request, @Nullable String reason) {
        return restClient.getGuildService().createGuildRole(id, request, reason);
    }

    public Flux<RoleData> modifyRolePositions(List<RolePositionModifyRequest> requests, @Nullable String reason) {
        return restClient.getGuildService().modifyGuildRolePositions(id,
            requests.toArray(new RolePositionModifyRequest[0]), reason);
    }

    public Flux<RoleData> modifyRolePositions(List<RolePositionModifyRequest> requests) {
        return modifyRolePositions(requests, null);
    }

    public Mono<RoleData> modifyRole(Snowflake roleId, RoleModifyRequest request, @Nullable String reason) {
        return restClient.getGuildService().modifyGuildRole(id, roleId.asLong(), request, reason);
    }

    public Mono<Void> deleteRole(Snowflake roleId, @Nullable String reason) {
        return restClient.getGuildService().deleteGuildRole(id, roleId.asLong(), reason);
    }

    public Mono<PruneData> getPruneCount(@Nullable Integer days) {
        Map<String, Object> queryParams = new HashMap<>();
        Optional.ofNullable(days).ifPresent(value -> queryParams.put("days", value));
        return restClient.getGuildService().getGuildPruneCount(id, queryParams);
    }

    public Mono<PruneData> beginGuildPrune(@Nullable Integer days, @Nullable Boolean computePruneCount,
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

    public Mono<Void> modifyIntegration(Snowflake integrationId, IntegrationModifyRequest request) {
        return restClient.getGuildService().modifyGuildIntegration(id, integrationId.asLong(), request);
    }

    public Mono<Void> deleteIntegration(Snowflake integrationId) {
        return restClient.getGuildService().deleteGuildIntegration(id, integrationId.asLong());
    }

    public Mono<Void> syncIntegration(Snowflake integrationId) {
        return restClient.getGuildService().syncGuildIntegration(id, integrationId.asLong());
    }

    public Mono<GuildWidgetData> getWidget() {
        return restClient.getGuildService().getGuildWidget(id);
    }

    public Mono<GuildWidgetData> modifyWidget(GuildWidgetModifyRequest request) {
        return restClient.getGuildService().modifyGuildWidget(id, request);
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

    public Mono<GuildPreviewData> getPreview() {
        return restClient.getGuildService().getGuildPreview(id);
    }

    public Flux<TemplateData> getTemplates() {
        return restClient.getTemplateService().getTemplates(id);
    }

    public Mono<ListThreadsData> getActiveThreads() {
        return restClient.getGuildService().listActiveGuildThreads(id);
    }

    /**
     * Requests to retrieve the scheduled event under this guild.
     *
     * @param eventId The ID of the event
     * @param withUserCount Whether to optionally include the number of subscribed users
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildScheduledEventData}. If an
     *  error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildScheduledEventData> getScheduledEvent(Snowflake eventId, @Nullable Boolean withUserCount) {
        Map<String, Object> queryParams = new HashMap<>();
        Optional.ofNullable(withUserCount).ifPresent(value -> queryParams.put("with_user_count", value));
        return restClient.getGuildService().getScheduledEvent(id, eventId.asLong(), queryParams);
    }

    /**
     * Requests to retrieve the scheduled events under this guild.
     *
     * @param withUserCount Whether to optionally include the number of subscribed users for each event
     * @return A {@link Flux} that continually emits all the  {@link GuildScheduledEventData} associated with this guild.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<GuildScheduledEventData> getScheduledEvents(@Nullable Boolean withUserCount) {
        Map<String, Object> queryParams = new HashMap<>();
        Optional.ofNullable(withUserCount).ifPresent(value -> queryParams.put("with_user_count", value));
        return restClient.getGuildService().getScheduledEvents(id, queryParams);
    }

    /**
     * Create a new scheduled event for the guild. Requires the {@link Permission#MANAGE_EVENTS} permission. Returns
     * the new event object on success.
     *
     * @param request the request body
     * @return A {@link Mono} where, upon subscription, emits the created {@link GuildScheduledEventData} on success.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildScheduledEventData> createScheduledEvent(GuildScheduledEventCreateRequest request) {
        return restClient.getGuildService().createScheduledEvent(id, request);
    }

    /**
     * Requests to modify a scheduled event. Requires the {@link Permission#MANAGE_EVENTS} permission.
     * Returns the modified event object on success.
     *
     * @param eventId The ID of the event
     * @param request the request body
     * @param reason an optional reason for the audit log
     * @return A {@link Mono} where, upon subscription, emits the modified {@link GuildScheduledEventData} on success.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildScheduledEventData> modifyScheduledEvent(Snowflake eventId,
                                                              GuildScheduledEventModifyRequest request,
                                                              @Nullable String reason) {
        return restClient.getGuildService().modifyScheduledEvent(id, eventId.asLong(), request, reason);
    }

    /**
     * Requests to delete a scheduled event. Requires the {@link Permission#MANAGE_EVENTS} permission.
     *
     * @param eventId The ID of the event
     * @param reason an optional reason for the audit log
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the event has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> deleteScheduledEvent(Snowflake eventId, @Nullable String reason) {
        return restClient.getGuildService().deleteScheduledEvent(id, eventId.asLong(), reason);
    }

    /**
     * Request to retrieve the onboarding of the guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link OnboardingData}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<OnboardingData> getOnboarding() {
        return this.restClient.getGuildService().getOnboarding(this.id);
    }

    /**
     * Request to modify the onboarding of the guild. Requires the {@link Permission#MANAGE_GUILD} and
     * {@link Permission#MANAGE_ROLES} permissions.
     *
     * @param request the request body
     * @param reason an optional reason for the audit log
     * @return A {@link Mono} where, upon successful completion, emits the modified {@link OnboardingData}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<OnboardingData> modifyOnboarding(OnboardingEditData request, @Nullable String reason) {
        return this.restClient.getGuildService().modifyOnboarding(this.id, request, reason);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RestGuild restGuild = (RestGuild) o;
        return id == restGuild.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
