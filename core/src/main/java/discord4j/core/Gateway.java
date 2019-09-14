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

package discord4j.core;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.object.Invite;
import discord4j.core.object.Region;
import discord4j.core.object.data.ApplicationInfoBean;
import discord4j.core.object.data.InviteBean;
import discord4j.core.object.data.RegionBean;
import discord4j.core.object.data.WebhookBean;
import discord4j.core.object.data.stored.*;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.GuildCreateSpec;
import discord4j.core.spec.UserEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.PaginationUtil;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.json.GatewayPayload;
import discord4j.rest.RestClient;
import discord4j.rest.json.response.UserGuildResponse;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.voice.VoiceClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An aggregation of all dependencies Discord4J requires to operate with the Discord Gateway, REST API and Voice
 * Gateway. Maintains a set of connections to every shard logged in from the same {@link GatewayBootstrap} and therefore
 * tracks state updates from all connected shards.
 * <p>
 * The following are some of the resources available through this aggregate:
 * <ul>
 *     <li>Access to the base {@link DiscordClient} for direct REST API operations.</li>
 *     <li>Access to {@link CoreResources} like the {@link RestClient} used to perform API requests.</li>
 *     <li>Access to {@link GatewayResources} that configure Gateway operations and coordination among shards.</li>
 *     <li>Access to {@link EventDispatcher} publishing events from all participating shards.</li>
 *     <li>Access to {@link StateHolder} for low-level manipulation of cached Gateway entities. Modifying the underlying
 *     structure during runtime is not recommended and can lead to incorrect or missing values.</li>
 * </ul>
 */
public class Gateway {

    private final DiscordClient discordClient;
    private final CoreResources coreResources;
    private final GatewayResources gatewayResources;
    private final EventDispatcher eventDispatcher;
    private final MonoProcessor<Void> closeProcessor;
    private final StateHolder stateHolder;
    private final Map<Integer, GatewayClient> gatewayClientMap;
    private final Map<Integer, VoiceClient> voiceClientMap;

    protected Gateway(Builder builder) {
        this.discordClient = Objects.requireNonNull(builder.discordClient);
        this.coreResources = Objects.requireNonNull(builder.coreResources);
        this.gatewayResources = Objects.requireNonNull(builder.gatewayResources);
        this.eventDispatcher = Objects.requireNonNull(builder.eventDispatcher);
        this.closeProcessor = Objects.requireNonNull(builder.closeProcessor);
        this.stateHolder = Objects.requireNonNull(builder.stateHolder);
        this.gatewayClientMap = new ConcurrentHashMap<>();
        this.voiceClientMap = new ConcurrentHashMap<>();
    }

    public static Gateway.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private DiscordClient discordClient;
        private CoreResources coreResources;
        private GatewayResources gatewayResources;
        private EventDispatcher eventDispatcher;
        private MonoProcessor<Void> closeProcessor;
        private StateHolder stateHolder;

        protected Builder() {
        }

        public Builder setDiscordClient(DiscordClient discordClient) {
            this.discordClient = discordClient;
            return this;
        }

        public Builder setCoreResources(CoreResources coreResources) {
            this.coreResources = coreResources;
            return this;
        }

        public Builder setGatewayResources(GatewayResources gatewayResources) {
            this.gatewayResources = gatewayResources;
            return this;
        }

        public Builder setEventDispatcher(EventDispatcher eventDispatcher) {
            this.eventDispatcher = eventDispatcher;
            return this;
        }

        public Builder setCloseProcessor(MonoProcessor<Void> closeProcessor) {
            this.closeProcessor = closeProcessor;
            return this;
        }

        public Builder setStateHolder(StateHolder stateHolder) {
            this.stateHolder = stateHolder;
            return this;
        }

        public Gateway build() {
            return new Gateway(this);
        }
    }

    public DiscordClient getDiscordClient() {
        return discordClient;
    }

    public CoreResources getCoreResources() {
        return coreResources;
    }

    public GatewayResources getGatewayResources() {
        return gatewayResources;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public Mono<Void> getCloseProcessor() {
        return closeProcessor;
    }

    public StateHolder getStateHolder() {
        return stateHolder;
    }

    public Map<Integer, GatewayClient> getGatewayClientMap() {
        return gatewayClientMap;
    }

    public Map<Integer, VoiceClient> getVoiceClientMap() {
        return voiceClientMap;
    }

    public RestClient getRestClient() {
        return coreResources.getRestClient();
    }

    /**
     * Requests to retrieve the channel represented by the supplied ID.
     *
     * @param channelId The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Channel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Channel> getChannelById(final Snowflake channelId) {
        final Mono<ChannelBean> channel = stateHolder.getChannelStore()
                .find(channelId.asLong());

        final Mono<ChannelBean> rest = getRestClient().getChannelService()
                .getChannel(channelId.asLong())
                .map(ChannelBean::new);

        return channel
                .switchIfEmpty(rest)
                .map(channelBean -> EntityUtil.getChannel(this, channelBean));
    }

    /**
     * Requests to retrieve the guild represented by the supplied ID.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuildById(final Snowflake guildId) {
        return stateHolder.getGuildStore()
                .find(guildId.asLong())
                .cast(BaseGuildBean.class)
                .switchIfEmpty(getRestClient().getGuildService()
                        .getGuild(guildId.asLong())
                        .map(BaseGuildBean::new))
                .map(baseGuildBean -> new Guild(this, baseGuildBean));
    }

    /**
     * Requests to retrieve the guild emoji represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param emojiId The ID of the emoji.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> getGuildEmojiById(final Snowflake guildId, final Snowflake emojiId) {
        return stateHolder.getGuildEmojiStore()
                .find(emojiId.asLong())
                .switchIfEmpty(getRestClient().getEmojiService()
                        .getGuildEmoji(guildId.asLong(), emojiId.asLong())
                        .map(GuildEmojiBean::new))
                .map(guildEmojiBean -> new GuildEmoji(this, guildEmojiBean, guildId.asLong()));
    }

    /**
     * Requests to retrieve the member represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMemberById(final Snowflake guildId, final Snowflake userId) {
        final Mono<MemberBean> member = stateHolder.getMemberStore()
                .find(LongLongTuple2.of(guildId.asLong(), userId.asLong()))
                .switchIfEmpty(getRestClient().getGuildService()
                        .getGuildMember(guildId.asLong(), userId.asLong())
                        .map(MemberBean::new));

        return member.flatMap(memberBean -> getUserBean(userId).map(userBean ->
                new Member(this, memberBean, userBean, guildId.asLong())));
    }

    /**
     * Requests to retrieve the message represented by the supplied IDs.
     *
     * @param channelId The ID of the channel.
     * @param messageId The ID of the message.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessageById(final Snowflake channelId, final Snowflake messageId) {
        return stateHolder.getMessageStore()
                .find(messageId.asLong())
                .switchIfEmpty(getRestClient().getChannelService()
                        .getMessage(channelId.asLong(), messageId.asLong())
                        .map(MessageBean::new))
                .map(messageBean -> new Message(this, messageBean));
    }

    /**
     * Requests to retrieve the role represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param roleId The ID of the role.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRoleById(final Snowflake guildId, final Snowflake roleId) {
        return stateHolder.getRoleStore()
                .find(roleId.asLong())
                .switchIfEmpty(getRestClient().getGuildService()
                        .getGuildRoles(guildId.asLong())
                        .filter(response -> response.getId() == roleId.asLong())
                        .map(RoleBean::new)
                        .singleOrEmpty())
                .map(roleBean -> new Role(this, roleBean, guildId.asLong()));
    }

    /**
     * Requests to retrieve the user represented by the supplied ID.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUserById(final Snowflake userId) {
        return getUserBean(userId).map(userBean -> new User(this, userBean));
    }

    /**
     * Requests to retrieve the webhook represented by the supplied ID.
     *
     * @param webhookId The ID of the webhook.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> getWebhookById(final Snowflake webhookId) {
        return getRestClient().getWebhookService()
                .getWebhook(webhookId.asLong())
                .map(WebhookBean::new)
                .map(webhookBean -> new Webhook(this, webhookBean));
    }

    /**
     * Requests to retrieve the application info.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link ApplicationInfo application info}. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationInfo> getApplicationInfo() {
        return getRestClient().getApplicationService()
                .getCurrentApplicationInfo()
                .map(ApplicationInfoBean::new)
                .map(applicationInfoBean -> new ApplicationInfo(this, applicationInfoBean));
    }

    /**
     * Requests to retrieve the guilds the current client is in.
     *
     * @return A {@link Flux} that continually emits the {@link Guild guilds} that the current client is in. If an error
     * is received, it is emitted through the {@code Flux}.
     */
    public Flux<Guild> getGuilds() {
        final Function<Map<String, Object>, Flux<UserGuildResponse>> makeRequest = params ->
                getRestClient().getUserService()
                        .getCurrentUserGuilds(params);

        return stateHolder.getGuildStore()
                .values()
                .cast(BaseGuildBean.class)
                .switchIfEmpty(PaginationUtil.paginateAfter(makeRequest, UserGuildResponse::getId, 0L, 100)
                        .map(UserGuildResponse::getId)
                        //.filter(id -> (id >> 22) % getConfig().getShardCount() == getConfig().getShardIndex())
                        .flatMap(getRestClient().getGuildService()::getGuild)
                        .map(BaseGuildBean::new))
                .map(bean -> new Guild(this, bean));
    }

    /**
     * Retrieve the currently stored (cached) users.
     *
     * @return A {@link Flux} that continually emits the {@link User users} that the current client has stored. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<User> getUsers() {
        return stateHolder.getUserStore()
                .values()
                .map(bean -> new User(this, bean));
    }

    /**
     * Requests to retrieve the voice regions that are available.
     *
     * @return A {@link Flux} that continually emits the {@link Region regions} that are available. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<Region> getRegions() {
        return getRestClient().getVoiceService().getVoiceRegions()
                .map(RegionBean::new)
                .map(bean -> new Region(this, bean));
    }

    /**
     * Requests to retrieve the bot user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the bot {@link User user}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getSelf() {
        final long selfId = stateHolder.getSelfId().get();
        return Mono.just(selfId)
                .filter(it -> it != 0)
                .map(Snowflake::of)
                .flatMap(this::getUserById)
                .switchIfEmpty(getRestClient().getUserService()
                        .getCurrentUser()
                        .map(UserBean::new)
                        .map(bean -> new User(this, bean)));
    }

    /**
     * Gets the bot user's ID. This may not be present if this client is not yet logged in.
     *
     * @return The bot user's ID.
     */
    public Optional<Snowflake> getSelfId() {
        return Optional.of(stateHolder.getSelfId().get())
                .filter(it -> it != 0)
                .map(Snowflake::of);
    }

    /**
     * Requests to create a guild.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> createGuild(final Consumer<? super GuildCreateSpec> spec) {
        final GuildCreateSpec mutatedSpec = new GuildCreateSpec();
        spec.accept(mutatedSpec);

        return getRestClient().getGuildService()
                .createGuild(mutatedSpec.asRequest())
                .map(BaseGuildBean::new)
                .map(bean -> new Guild(this, bean));
    }

    /**
     * Update this client {@link Presence}.
     *
     * @param presence The updated client presence.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> updatePresence(final int shardId, final Presence presence) {
        return getGatewayClientMap().get(shardId).send(
                Mono.just(GatewayPayload.statusUpdate(presence.asStatusUpdate())));
    }

    /**
     * Requests to retrieve an invite.
     *
     * @param inviteCode The code for the invite (e.g. "xdYkpp").
     * @return A {@link Mono} where, upon successful completion, emits the {@link Invite} as represented by the
     * supplied invite code. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Invite> getInvite(final String inviteCode) {
        return getRestClient().getInviteService()
                .getInvite(inviteCode)
                .map(InviteBean::new)
                .map(bean -> new Invite(this, bean));
    }

    /**
     * Requests to edit this client (i.e., modify the current bot user).
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link UserEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link User}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<User> edit(final Consumer<? super UserEditSpec> spec) {
        final UserEditSpec mutatedSpec = new UserEditSpec();
        spec.accept(mutatedSpec);

        return getRestClient().getUserService()
                .modifyCurrentUser(mutatedSpec.asRequest())
                .map(UserBean::new)
                .map(bean -> new User(this, bean));
    }

    /**
     * Requests to retrieve the user bean represented by the supplied ID.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link UserBean} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    private Mono<UserBean> getUserBean(final Snowflake userId) {
        return stateHolder.getUserStore()
                .find(userId.asLong())
                .switchIfEmpty(getRestClient().getUserService()
                        .getUser(userId.asLong())
                        .map(UserBean::new));
    }

    /**
     * Disconnects this {@link Gateway} from Discord upon subscribing. All joining {@link GatewayClient
     * GatewayClients} will attempt to gracefully close and complete this {@link Mono} after all of them have
     * disconnected.
     *
     * @return A {@link Mono} that, on subscription, will disconnect each connection established by this
     * {@link Gateway} and complete after all of them have closed.
     */
    public Mono<Void> logout() {
        return Mono.whenDelayError(gatewayClientMap.values().stream()
                .map(client -> client.close(false))
                .collect(Collectors.toList()));
    }

    /**
     * Return a {@link Mono} that signals completion when all joining {@link GatewayClient GatewayClients} have
     * disconnected.
     *
     * @return
     */
    public Mono<Void> onDisconnect() {
        return closeProcessor;
    }

    public <T extends Event> Flux<T> on(Class<T> eventClass) {
        return getEventDispatcher().on(eventClass);
    }

    public <T extends Event> Flux<T> on(Class<T> eventClass, Function<T, Mono<Void>> action) {
        return getEventDispatcher().on(eventClass, action);
    }
}
