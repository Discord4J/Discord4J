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
package discord4j.core;

import discord4j.core.event.EventDispatcher;
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
import discord4j.gateway.GatewayObserver;
import discord4j.gateway.json.GatewayPayload;
import discord4j.rest.json.response.GatewayResponse;
import discord4j.rest.json.response.UserGuildResponse;
import discord4j.rest.util.RouteUtils;
import discord4j.store.api.util.LongLongTuple2;
import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/** A high-level abstraction of common Discord operations such as entity retrieval and Discord shard manipulation. */
public final class DiscordClient {

    private static final Logger log = Loggers.getLogger(DiscordClient.class);

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    private final AtomicBoolean isDisposed = new AtomicBoolean(true);

    /**
     * Constructs a {@code DiscordClient} with an associated ServiceMediator.
     *
     * @param serviceMediator The ServiceMediator associated to this object.
     */
    DiscordClient(final ServiceMediator serviceMediator) {
        this.serviceMediator = serviceMediator;
    }

    /**
     * Obtain the {@link ServiceMediator} associated with this {@link DiscordClient}. This is an advanced method to
     * access underlying middleware and resources.
     *
     * @return the current {@link ServiceMediator} for this client
     */
    public ServiceMediator getServiceMediator() {
        return serviceMediator;
    }

    /**
     * Requests to retrieve the channel represented by the supplied ID.
     *
     * @param channelId The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Channel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Channel> getChannelById(final Snowflake channelId) {
        final Mono<ChannelBean> channel = serviceMediator.getStateHolder().getChannelStore()
                .find(channelId.asLong());

        final Mono<ChannelBean> rest = serviceMediator.getRestClient().getChannelService()
                .getChannel(channelId.asLong())
                .map(ChannelBean::new)
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));

        return channel
                .switchIfEmpty(rest)
                .map(channelBean -> EntityUtil.getChannel(serviceMediator, channelBean));
    }

    /**
     * Requests to retrieve the guild represented by the supplied ID.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuildById(final Snowflake guildId) {
        return serviceMediator.getStateHolder().getGuildStore()
                .find(guildId.asLong())
                .cast(BaseGuildBean.class)
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuild(guildId.asLong())
                        .map(BaseGuildBean::new)
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex())))
                .map(baseGuildBean -> new Guild(serviceMediator, baseGuildBean));
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
        return serviceMediator.getStateHolder().getGuildEmojiStore()
                .find(emojiId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getEmojiService()
                        .getGuildEmoji(guildId.asLong(), emojiId.asLong())
                        .map(GuildEmojiBean::new)
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex())))
                .map(guildEmojiBean -> new GuildEmoji(serviceMediator, guildEmojiBean, guildId.asLong()));
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
        final Mono<MemberBean> member = serviceMediator.getStateHolder().getMemberStore()
                .find(LongLongTuple2.of(guildId.asLong(), userId.asLong()))
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuildMember(guildId.asLong(), userId.asLong())
                        .map(MemberBean::new)
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex())));

        return member.flatMap(memberBean -> getUserBean(userId).map(userBean ->
                new Member(serviceMediator, memberBean, userBean, guildId.asLong())));
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
        return serviceMediator.getStateHolder().getMessageStore()
                .find(messageId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getChannelService()
                        .getMessage(channelId.asLong(), messageId.asLong())
                        .map(MessageBean::new)
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex())))
                .map(messageBean -> new Message(serviceMediator, messageBean));
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
        return serviceMediator.getStateHolder().getRoleStore()
                .find(roleId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuildRoles(guildId.asLong())
                        .filter(response -> response.getId() == roleId.asLong())
                        .map(RoleBean::new)
                        .singleOrEmpty()
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex())))
                .map(roleBean -> new Role(serviceMediator, roleBean, guildId.asLong()));
    }

    /**
     * Requests to retrieve the user represented by the supplied ID.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUserById(final Snowflake userId) {
        return getUserBean(userId).map(userBean -> new User(serviceMediator, userBean));
    }

    /**
     * Requests to retrieve the webhook represented by the supplied ID.
     *
     * @param webhookId The ID of the webhook.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> getWebhookById(final Snowflake webhookId) {
        return serviceMediator.getRestClient().getWebhookService()
                .getWebhook(webhookId.asLong())
                .map(WebhookBean::new)
                .map(webhookBean -> new Webhook(serviceMediator, webhookBean))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to retrieve the application info.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link ApplicationInfo application info}. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationInfo> getApplicationInfo() {
        return serviceMediator.getRestClient().getApplicationService()
                .getCurrentApplicationInfo()
                .map(ApplicationInfoBean::new)
                .map(applicationInfoBean -> new ApplicationInfo(serviceMediator, applicationInfoBean))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to retrieve the guilds the current client is in.
     *
     * @return A {@link Flux} that continually emits the {@link Guild guilds} that the current client is in. If an error
     * is received, it is emitted through the {@code Flux}.
     */
    public Flux<Guild> getGuilds() {
        final Function<Map<String, Object>, Flux<UserGuildResponse>> makeRequest = params ->
                serviceMediator.getRestClient().getUserService()
                        .getCurrentUserGuilds(params)
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));

        return serviceMediator.getStateHolder().getGuildStore()
                .values()
                .cast(BaseGuildBean.class)
                .switchIfEmpty(PaginationUtil.paginateAfter(makeRequest, UserGuildResponse::getId, 0L, 100)
                        .map(UserGuildResponse::getId)
                        .filter(id -> (id >> 22) % getConfig().getShardCount() == getConfig().getShardIndex())
                        .flatMap(serviceMediator.getRestClient().getGuildService()::getGuild)
                        .map(BaseGuildBean::new)
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex())))
                .map(bean -> new Guild(serviceMediator, bean));
    }

    /**
     * Retrieve the currently stored (cached) users.
     *
     * @return A {@link Flux} that continually emits the {@link User users} that the current client has stored. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<User> getUsers() {
        return serviceMediator.getStateHolder().getUserStore()
                .values()
                .map(bean -> new User(serviceMediator, bean));
    }

    /**
     * Requests to retrieve the voice regions that are available.
     *
     * @return A {@link Flux} that continually emits the {@link Region regions} that are available. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<Region> getRegions() {
        return serviceMediator.getRestClient().getVoiceService().getVoiceRegions()
                .map(RegionBean::new)
                .map(bean -> new Region(serviceMediator, bean))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to retrieve the bot user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the bot {@link User user}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getSelf() {
        final long selfId = serviceMediator.getStateHolder().getSelfId().get();
        return Mono.just(selfId)
                .filter(it -> it != 0)
                .map(Snowflake::of)
                .flatMap(this::getUserById)
                .switchIfEmpty(serviceMediator.getRestClient().getUserService()
                        .getCurrentUser()
                        .map(UserBean::new)
                        .map(bean -> new User(serviceMediator, bean))
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex())));
    }

    /**
     * Gets the bot user's ID. This may not be present if this client is not yet logged in.
     *
     * @return The bot user's ID.
     */
    public Optional<Snowflake> getSelfId() {
        return Optional.of(serviceMediator.getStateHolder().getSelfId().get())
                .filter(it -> it != 0)
                .map(Snowflake::of);
    }

    /**
     * Logs in the client to the gateway.
     *
     * @return A {@link Mono} that completes (either successfully or with an error) when the client disconnects from the
     * gateway without a reconnect attempt. It is recommended to call this from {@code main} and as a final statement
     * invoke {@link Mono#block()}.
     */
    public Mono<Void> login() {
        return Mono
                .fromRunnable(() -> {
                    if (!isDisposed.compareAndSet(true, false)) {
                        throw new AlreadyConnectedException();
                    }
                })
                .then(serviceMediator.getRestClient().getGatewayService().getGateway())
                .transform(loginSequence(serviceMediator.getGatewayClient()));
    }

    private Function<Mono<GatewayResponse>, Mono<Void>> loginSequence(GatewayClient client) {
        return sequence -> sequence
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()))
                .flatMap(response -> client.execute(
                        RouteUtils.expandQuery(response.getUrl(),
                                serviceMediator.getClientConfig().getGatewayParameters()),
                        GatewayObserver.NOOP_LISTENER))
                .then(serviceMediator.getStateHolder().invalidateStores())
                .then(serviceMediator.getStoreService().dispose())
                .doOnCancel(this::setDisposed)
                .doOnTerminate(this::setDisposed);
    }

    private void setDisposed() {
        if (!isDisposed.compareAndSet(false, true)) {
            log.warn("Shard {} was already disposed", serviceMediator.getClientConfig().getShardIndex());
        }
    }

    /**
     * Logs out the client from the gateway.
     *
     * @return a {@link Mono} deferring completion until this client has completely disconnected from the gateway
     */
    public Mono<Void> logout() {
        return serviceMediator.getGatewayClient().close(false);
    }

    /**
     * Returns whether this client is currently connected to Discord Gateway.
     *
     * @return true if the gateway connection is currently established, false otherwise.
     */
    public boolean isConnected() {
        return serviceMediator.getGatewayClient().isConnected();
    }

    /**
     * Gets the amount of time it last took Discord Gateway to respond to a heartbeat with an ack.
     *
     * @return the duration which Discord took to respond to the last heartbeat with an ack.
     */
    public Duration getResponseTime() {
        return serviceMediator.getGatewayClient().getResponseTime();
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

        return serviceMediator.getRestClient().getGuildService()
                .createGuild(mutatedSpec.asRequest())
                .map(BaseGuildBean::new)
                .map(bean -> new Guild(serviceMediator, bean))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Update this client {@link Presence}.
     *
     * @param presence The updated client presence.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> updatePresence(final Presence presence) {
        return Mono.fromRunnable(() -> serviceMediator.getGatewayClient().sender().next(
                GatewayPayload.statusUpdate(presence.asStatusUpdate())));
    }

    /**
     * Requests to retrieve an invite.
     *
     * @param inviteCode The code for the invite (e.g. "xdYkpp").
     * @return A {@link Mono} where, upon successful completion, emits the {@link Invite} as represented by the
     * supplied invite code. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Invite> getInvite(final String inviteCode) {
        return serviceMediator.getRestClient().getInviteService()
                .getInvite(inviteCode)
                .map(InviteBean::new)
                .map(bean -> new Invite(serviceMediator, bean))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
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

        return serviceMediator.getRestClient().getUserService()
                .modifyCurrentUser(mutatedSpec.asRequest())
                .map(UserBean::new)
                .map(bean -> new User(serviceMediator, bean))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Gets the event dispatcher, allowing reactive subscription of client events.
     *
     * @return an EventDispatcher associated with this client.
     */
    public EventDispatcher getEventDispatcher() {
        return serviceMediator.getEventDispatcher();
    }

    /**
     * Gets the configuration for this client.
     *
     * @return The configuration for this client.
     */
    public ClientConfig getConfig() {
        return serviceMediator.getClientConfig();
    }

    /**
     * Requests to retrieve the user bean represented by the supplied ID.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link UserBean} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    private Mono<UserBean> getUserBean(final Snowflake userId) {
        return serviceMediator.getStateHolder().getUserStore()
                .find(userId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getUserService()
                        .getUser(userId.asLong())
                        .map(UserBean::new)
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex())));
    }
}
