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
package discord4j.rest;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.*;
import discord4j.rest.entity.*;
import discord4j.rest.request.Router;
import discord4j.rest.request.RouterOptions;
import discord4j.rest.service.*;
import discord4j.rest.util.PaginationUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An aggregation of all Discord REST API resources available. Each REST resource uses its own class and uses a
 * common {@link Router} to execute requests.
 */
public class RestClient {

    private final RestResources restResources;
    private final ApplicationService applicationService;
    private final AuditLogService auditLogService;
    private final ChannelService channelService;
    private final EmojiService emojiService;
    private final StickerService stickerService;
    private final GatewayService gatewayService;
    private final GuildService guildService;
    private final InteractionService interactionService;
    private final InviteService inviteService;
    private final StageInstanceService stageInstanceService;
    private final TemplateService templateService;
    private final UserService userService;
    private final VoiceService voiceService;
    private final WebhookService webhookService;
    private final Mono<Long> applicationIdMono;
    private final AutoModService autoModService;
    private final PollService pollService;
    private final MonetizationService monetizationService;

    /**
     * Create a {@link RestClient} with default options, using the given token for authentication.
     *
     * @param token the bot token used for authentication
     * @return a {@link RestClient} configured with the default options
     */
    public static RestClient create(String token) {
        return RestClientBuilder.createRest(token).build();
    }

    /**
     * Obtain a {@link RestClientBuilder} able to create {@link RestClient} instances, using the given token for
     * authentication.
     *
     * @param token the bot token used for authentication
     * @return a {@link RestClientBuilder}
     */
    public static RestClientBuilder<RestClient, RouterOptions> restBuilder(String token) {
        return RestClientBuilder.createRest(token);
    }

    /**
     * Create a new {@link RestClient} using the given {@link Router} as connector to perform requests.
     *
     * @param restResources a set of REST API resources required to operate this client
     */
    protected RestClient(final RestResources restResources) {
        this.restResources = restResources;
        Router router = restResources.getRouter();
        this.applicationService = new ApplicationService(router);
        this.auditLogService = new AuditLogService(router);
        this.channelService = new ChannelService(router);
        this.emojiService = new EmojiService(router);
        this.stickerService = new StickerService(router);
        this.gatewayService = new GatewayService(router);
        this.guildService = new GuildService(router);
        this.interactionService = new InteractionService(router);
        this.inviteService = new InviteService(router);
        this.stageInstanceService = new StageInstanceService(router);
        this.templateService = new TemplateService(router);
        this.userService = new UserService(router);
        this.voiceService = new VoiceService(router);
        this.webhookService = new WebhookService(router);
        this.autoModService = new AutoModService(router);
        this.pollService = new PollService(router);
        this.monetizationService = new MonetizationService(router);

        this.applicationIdMono = getApplicationInfo()
                .map(app -> Snowflake.asLong(app.id()))
                .cache(__ -> Duration.ofMillis(Long.MAX_VALUE), __ -> Duration.ZERO, () -> Duration.ZERO);
    }

    /**
     * Obtain the {@link RestResources} associated with this {@link RestClient}.
     *
     * @return the current {@link RestResources} for this client
     */
    public RestResources getRestResources() {
        return restResources;
    }

    /**
     * Requests to retrieve the channel represented by the supplied ID.
     *
     * @param channelId The ID of the channel.
     * @return A {@link RestChannel} as represented by the supplied ID.
     */
    public RestChannel getChannelById(final Snowflake channelId) {
        return RestChannel.create(this, channelId);
    }

    /**
     * Requests to retrieve the channel represented by the supplied {@link ChannelData}.
     *
     * @param data The data of the channel.
     * @return A {@link RestChannel} as represented by the supplied data.
     */
    public RestChannel restChannel(ChannelData data) {
        return RestChannel.create(this, Snowflake.of(data.id()));
    }

    /**
     * Requests to retrieve the guild represented by the supplied ID.
     *
     * @param guildId The ID of the guild.
     * @return A {@link RestGuild} as represented by the supplied ID.
     */
    public RestGuild getGuildById(final Snowflake guildId) {
        return RestGuild.create(this, guildId);
    }

    /**
     * Requests to retrieve the guild represented by the supplied {@link GuildData}.
     *
     * @param data The data of the guild.
     * @return A {@link RestGuild} as represented by the supplied data.
     */
    public RestGuild restGuild(GuildData data) {
        return RestGuild.create(this, Snowflake.of(data.id()));
    }

    /**
     * Requests to retrieve the guild emoji represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param emojiId The ID of the emoji.
     * @return A {@link RestEmoji} as represented by the supplied IDs.
     */
    public RestEmoji getGuildEmojiById(final Snowflake guildId, final Snowflake emojiId) {
        return RestEmoji.create(this, guildId, emojiId);
    }

    /**
     * Requests to retrieve the guild emoji represented by the supplied ID and {@link EmojiData}.
     *
     * @param guildId The ID of the guild.
     * @param data The data of the emoji.
     * @return A {@link RestEmoji} as represented by the supplied parameters.
     */
    public RestEmoji restGuildEmoji(Snowflake guildId, EmojiData data) {
        return RestEmoji.create(this, guildId,
            Snowflake.of(data.id().orElseThrow(() -> new IllegalArgumentException("Not a guild emoji"))));
    }

    /**
     * Requests to retrieve the guild sticker represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param stickerId The ID of the sticker.
     * @return A {@link RestSticker} as represented by the supplied IDs.
     */
    public RestSticker getGuildStickerById(final Snowflake guildId, final Snowflake stickerId) {
        return RestSticker.create(this, guildId, stickerId);
    }

    /**
     * Requests to retrieve the guild sticker represented by the supplied ID and {@link StickerData}.
     *
     * @param guildId The ID of the guild.
     * @param data The data of the sticker.
     * @return A {@link RestSticker} as represented by the supplied parameters.
     */
    public RestSticker restGuildSticker(Snowflake guildId, StickerData data) {
        return RestSticker.create(this, guildId,
            Snowflake.of(data.id()));
    }

    /**
     * Requests to retrieve the member represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param userId The ID of the user.
     * @return A {@link RestMember} as represented by the supplied IDs.
     */
    public RestMember getMemberById(final Snowflake guildId, final Snowflake userId) {
        return RestMember.create(this, guildId, userId);
    }

    /**
     * Requests to retrieve the member represented by the supplied ID and {@link MemberData}
     *
     * @param guildId The ID of the guild.
     * @param data The data of the user.
     * @return A {@link RestMember} as represented by the supplied parameters.
     */
    public RestMember restMember(Snowflake guildId, MemberData data) {
        return RestMember.create(this, guildId, Snowflake.of(data.user().id()));
    }

    /**
     * Requests to retrieve the bot member from the guild of the supplied ID
     *
     * @param guildId the ID of the guild.
     * @return A {@link RestMember} of the bot user as represented by the supplied ID.
     */
    public RestMember selfRestMember(Snowflake guildId) {
        return RestMember.create(this, guildId, restResources.getSelfId());
    }

    /**
     * Requests to retrieve the message represented by the supplied IDs.
     *
     * @param channelId The ID of the channel.
     * @param messageId The ID of the message.
     * @return A {@link RestMessage} as represented by the supplied IDs.
     */
    public RestMessage getMessageById(final Snowflake channelId, final Snowflake messageId) {
        return RestMessage.create(this, channelId, messageId);
    }

    /**
     * Requests to retrieve the message represented by the supplied {@link MessageData}.
     *
     * @param data The data of the channel.
     * @return A {@link RestMessage} as represented by the supplied data.
     */
    public RestMessage restMessage(MessageData data) {
        return RestMessage.create(this, Snowflake.of(data.channelId()), Snowflake.of(data.id()));
    }

    /**
     * Requests to retrieve the role represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param roleId The ID of the role.
     * @return A {@link RestRole} as represented by the supplied IDs.
     */
    public RestRole getRoleById(final Snowflake guildId, final Snowflake roleId) {
        return RestRole.create(this, guildId, roleId);
    }

    /**
     * Requests to retrieve the role represented by the supplied ID and {@link RoleData}.
     *
     * @param guildId The ID of the guild.
     * @param data The data of the role.
     * @return A {@link RestRole} as represented by the supplied parameters.
     */
    public RestRole restRole(Snowflake guildId, RoleData data) {
        return RestRole.create(this, guildId, Snowflake.of(data.id()));
    }

    /**
     * Requests to retrieve the scheduled event represented by the supplied ID.
     *
     * @param guildId The ID of the guild
     * @param eventId The ID of the event
     * @return A {@link RestScheduledEvent} as represented by the supplied IDs.
     */
    public RestScheduledEvent getScheduledEventById(final Snowflake guildId, final Snowflake eventId) {
        return RestScheduledEvent.create(this, guildId, eventId);
    }

    public RestScheduledEvent restScheduledEvent(Snowflake guildId, GuildScheduledEventData data) {
        return RestScheduledEvent.create(this, guildId, Snowflake.of(data.id()));
    }

    /**
     * Requests to retrieve the user represented by the supplied ID.
     *
     * @param userId The ID of the user.
     * @return A {@link RestUser} as represented by the supplied ID.
     */
    public RestUser getUserById(final Snowflake userId) {
        return RestUser.create(this, userId);
    }

    /**
     * Requests to retrieve the user represented by the supplied {@link UserData}.
     *
     * @param data The data of the user.
     * @return A {@link RestUser} as represented by the supplied data.
     */
    public RestUser restUser(UserData data) {
        return RestUser.create(this, Snowflake.of(data.id()));
    }

    /**
     * Requests to retrieve the webhook represented by the supplied ID.
     *
     * @param webhookId The ID of the webhook.
     * @return A {@link RestWebhook} as represented by the supplied ID.
     */
    public RestWebhook getWebhookById(final Snowflake webhookId) {
        return RestWebhook.create(this, webhookId);
    }

    /**
     * Requests to retrieve the webhook represented by the supplied {@link WebhookData}.
     *
     * @param data The data of the webhook.
     * @return A {@link RestWebhook} as represented by the supplied ID.
     */
    public RestWebhook restWebhook(WebhookData data) {
        return RestWebhook.create(this, Snowflake.of(data.id()));
    }

    /**
     * Requests to retrieve the application info.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link ApplicationInfoData}. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationInfoData> getApplicationInfo() {
        return this.getApplicationService()
                .getCurrentApplicationInfo();
    }

    /**
     * Requests to retrieve the guilds the current client is in.
     *
     * @return A {@link Flux} that continually emits the {@link PartialGuildData guilds} that the current client is
     * in. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<UserGuildData> getGuilds() {
        final Function<Map<String, Object>, Flux<UserGuildData>> makeRequest = params ->
                this.getUserService()
                        .getCurrentUserGuilds(params);

        return PaginationUtil.paginateAfter(makeRequest, data -> Snowflake.asLong(data.id()), 0L, 100);
    }

    /**
     * Requests to retrieve the voice regions that are available.
     *
     * @return A {@link Flux} that continually emits the {@link RegionData regions} that are available. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<RegionData> getRegions() {
        return this.getVoiceService().getVoiceRegions();
    }

    /**
     * Requests to retrieve the bot user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the bot {@link UserData user}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<UserData> getSelf() {
        return userService.getCurrentUser();
    }

    /**
     * Requests to retrieve the bot user, represented as a member of the guild of the supplied ID
     *
     * @param guildId The ID of the guild
     * @return a {@link Mono} where, upon successful completion, emits the bot {@link MemberData member}. If an error is
     *         received, it is emitted through the {@code Mono}.
     */
    public Mono<MemberData> getSelfMember(Snowflake guildId) {
        return guildService.getGuildMember(guildId.asLong(), restResources.getSelfId().asLong());
    }

    /**
     * Requests to create a guild.
     *
     * @param request A {@link GuildCreateRequest} as request body.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link GuildUpdateData}. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildUpdateData> createGuild(GuildCreateRequest request) {
        return guildService.createGuild(request);
    }

    /**
     * Requests to retrieve an invite.
     *
     * @param inviteCode The code for the invite (e.g. "xdYkpp").
     * @return A {@link Mono} where, upon successful completion, emits the {@link InviteData} as represented by the
     * supplied invite code. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<InviteData> getInvite(final String inviteCode) {
        return inviteService.getInvite(inviteCode);
    }

    /**
     * Requests to retrieve an invite.
     *
     * @param inviteCode The code for the invite (e.g. "xdYkpp").
     * @param withCounts whether the invite should contain approximate member counts
     * @param withExpiration whether the invite should contain the expiration date
     * @param guildScheduledEventId the guild scheduled event to include with the invite, can be {@code null}
     * @return A {@link Mono} where, upon successful completion, emits the {@link InviteData} as represented by the
     * supplied invite code. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<InviteData> getInvite(String inviteCode, boolean withCounts, boolean withExpiration,
                                      @Nullable Snowflake guildScheduledEventId) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("with_counts", withCounts);
        queryParams.put("with_expiration", withExpiration);
        if (guildScheduledEventId != null) {
            queryParams.put("guild_scheduled_event_id", guildScheduledEventId.asString());
        }
        return inviteService.getInvite(inviteCode, queryParams);
    }

    /**
     * Requests to retrieve an template.
     *
     * @param templateCode The code for the template (e.g. "hgM48av5Q69A").
     * @return A {@link Mono} where, upon successful completion, emits the {@link TemplateData} as represented by the
     * supplied template code. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<TemplateData> getTemplate(final String templateCode) {
        return templateService.getTemplate(templateCode);
    }

    /**
     * Requests to edit this client (i.e., modify the current bot user).
     *
     * @param request A {@link UserModifyRequest} as request body.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link UserData}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<UserData> edit(UserModifyRequest request) {
        return userService.modifyCurrentUser(request);
    }

    /**
     * Requests to retrieve a stage instance.
     *
     * @param channelId The channel id associated to the stage instance.
     * @return A {@link Mono} where, upon successful completion, emits the {@link StageInstanceData} associated to the
     * supplied channel ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<StageInstanceData> getStageInstance(final Snowflake channelId) {
        return stageInstanceService.getStageInstance(channelId.asLong());
    }

    /**
     * Access a low-level representation of the API endpoints for the Application resource.
     *
     * @return a handle to perform low-level requests to the API
     */
    public ApplicationService getApplicationService() {
        return applicationService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Audit Log resource.
     *
     * @return a handle to perform low-level requests to the API
     */
    public AuditLogService getAuditLogService() {
        return auditLogService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Channel resource. It is recommended you use
     * methods like {@link #getChannelById(Snowflake)}, {@link #restChannel(ChannelData)} or
     * {@link RestChannel#create(RestClient, Snowflake)}.
     *
     * @return a handle to perform low-level requests to the API
     */
    public ChannelService getChannelService() {
        return channelService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Guild Emoji resource. It is recommended you use
     * methods like {@link #getGuildEmojiById(Snowflake, Snowflake)}, {@link #restGuildEmoji(Snowflake, EmojiData)} or
     * {@link RestEmoji#create(RestClient, Snowflake, Snowflake)}.
     *
     * @return a handle to perform low-level requests to the API
     */
    public EmojiService getEmojiService() {
        return emojiService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Guild Sticker resource. It is recommended you use
     * methods like {@link #getGuildStickerById(Snowflake, Snowflake)}, {@link #restGuildSticker(Snowflake, StickerData)} or
     * {@link RestSticker#create(RestClient, Snowflake, Snowflake)}.
     *
     * @return a handle to perform low-level requests to the API
     */
    public StickerService getStickerService() {
        return stickerService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Gateway resource.
     *
     * @return a handle to perform low-level requests to the API
     */
    public GatewayService getGatewayService() {
        return gatewayService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Guild resource. It is recommended you use
     * methods like {@link #getGuildById(Snowflake)}, {@link #restGuild(GuildData)} or
     * {@link RestGuild#create(RestClient, Snowflake)}.
     *
     * @return a handle to perform low-level requests to the API
     */
    public GuildService getGuildService() {
        return guildService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Interaction resource.
     * @return a handle to perform low-level requests to the API
     */
    public InteractionService getInteractionService() {
        return interactionService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Invite resource. It is recommended you use
     * methods like {@link #getInvite(String)}, or {@link RestInvite#create(RestClient, String)}.
     *
     * @return a handle to perform low-level requests to the API
     */
    public InviteService getInviteService() {
        return inviteService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Stage Instance resource. It is recommended you use
     * the {@link #getStageInstance(Snowflake)} method.
     *
     * @return a handle to perform low-level requests to the API
     */
    public StageInstanceService getStageInstanceService() {
        return this.stageInstanceService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Template resource. It is recommended you use
     * methods like {@link #getTemplate(String)}, or {@link RestGuildTemplate#create(RestClient, String)}.
     *
     * @return a handle to perform low-level requests to the API
     */
    public TemplateService getTemplateService() {
        return templateService;
    }

    /**
     * Access a low-level representation of the API endpoints for the User resource. It is recommended you use
     * methods like {@link #getUserById(Snowflake)}, {@link #restUser(UserData)} or
     * {@link RestUser#create(RestClient, Snowflake)}.
     *
     * @return a handle to perform low-level requests to the API
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Voice Region resource.
     *
     * @return a handle to perform low-level requests to the API
     */
    public VoiceService getVoiceService() {
        return voiceService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Webhook resource. It is recommended you use
     * methods like {@link #getWebhookById(Snowflake)}, {@link #restWebhook(WebhookData)} or
     * {@link RestWebhook#create(RestClient, Snowflake)}.
     *
     * @return a handle to perform low-level requests to the API
     */
    public WebhookService getWebhookService() {
        return webhookService;
    }

    /**
     * Access a low-level representation of the API endpoints for the AutoMod resource.
     *
     * @return a handle to perform low-level requests to the API
     */
    public AutoModService getAutoModService() {
        return autoModService;
    }

    /**
     * Access a low-level representation of the API endpoints for the Monetization resource.
     * @return a handle to perform low-level requests to the API
     */
    public MonetizationService getMonetizationService() {
        return monetizationService;
    }

    public Mono<Long> getApplicationId() {
        return applicationIdMono;
    }

    public PollService getPollService() {
        return this.pollService;
    }
}
