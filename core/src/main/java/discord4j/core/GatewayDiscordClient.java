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

import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
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
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Collections;
import java.util.Map;
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
 *     <li>Access to the base {@link DiscordClient} for direct REST API operations through {@link #rest()}.</li>
 *     <li>Access to {@link CoreResources} like the {@link RestClient} used to perform API requests.</li>
 *     <li>Access to {@link GatewayResources} that configure Gateway operations and coordination among shards.</li>
 *     <li>Access to {@link EventDispatcher} publishing events from all participating shards.</li>
 *     <li>Access to {@link StateHolder} for low-level manipulation of cached Gateway entities. Modifying the underlying
 *     structure during runtime is not recommended and can lead to incorrect or missing values.</li>
 * </ul>
 */
public class GatewayDiscordClient {

    private static final Logger log = Loggers.getLogger(GatewayDiscordClient.class);

    private final DiscordClient discordClient;
    private final GatewayResources gatewayResources;
    private final MonoProcessor<Void> closeProcessor;
    private final Map<Integer, GatewayClient> gatewayClientMap;
    private final Map<Integer, VoiceClient> voiceClientMap;

    public GatewayDiscordClient(DiscordClient discordClient, GatewayResources gatewayResources,
                                MonoProcessor<Void> closeProcessor, Map<Integer, GatewayClient> gatewayClientMap,
                                Map<Integer, VoiceClient> voiceClientMap) {
        this.discordClient = discordClient;
        this.gatewayResources = gatewayResources;
        this.closeProcessor = closeProcessor;
        this.gatewayClientMap = gatewayClientMap;
        this.voiceClientMap = voiceClientMap;
    }

    /**
     * Access the parent {@link DiscordClient} capable of performing direct REST API requests and REST entities.
     *
     * @return the {@link DiscordClient} that created this {@link GatewayDiscordClient}
     */
    public DiscordClient rest() {
        return discordClient;
    }

    /**
     * Returns the set of resources essential to operate on a {@link DiscordClient} for entity manipulation,
     * scheduling and API communication, like the {@link RestClient}, {@link JacksonResources} and
     * {@link ReactorResources}.
     *
     * @return the {@link CoreResources} for the parent {@link DiscordClient}
     */
    public CoreResources getCoreResources() {
        return discordClient.getCoreResources();
    }

    /**
     * Returns the set of resources essential to build {@link GatewayClient} instances and manage multiple Discord
     * Gateway connections.
     *
     * @return the {@link GatewayResources} tied to this {@link GatewayDiscordClient}
     */
    public GatewayResources getGatewayResources() {
        return gatewayResources;
    }

    /**
     * Distributes events to subscribers. Starting from v3.1, the {@link EventDispatcher} is capable of distributing
     * events from all {@link GatewayClient} connections (shards) that were specified when this
     * {@link GatewayDiscordClient} was created.
     *
     * @return the {@link EventDispatcher} tied to this {@link GatewayDiscordClient}
     */
    public EventDispatcher getEventDispatcher() {
        return gatewayResources.getEventDispatcher();
    }

    /**
     * Returns an unmodifiable view of the {@link GatewayClient} instances created by this
     * {@link GatewayDiscordClient}. The returned map uses shard index for keys.
     *
     * @return a map of {@link GatewayClient} instances
     */
    public Map<Integer, GatewayClient> getGatewayClientMap() {
        return Collections.unmodifiableMap(gatewayClientMap);
    }

    /**
     * Returns an unmodifiable view of the {@link VoiceClient} instances created by this
     * {@link GatewayDiscordClient}. The returned map uses shard index for keys.
     *
     * @return a map of {@link VoiceClient} instances
     */
    public Map<Integer, VoiceClient> getVoiceClientMap() {
        return Collections.unmodifiableMap(voiceClientMap);
    }

    /**
     * Returns the {@link RestClient} used to execute REST API requests.
     *
     * @return the {@link RestClient} tied to the parent {@link DiscordClient} through {@link CoreResources}
     */
    public RestClient getRestClient() {
        return getCoreResources().getRestClient();
    }

    /**
     * Requests to retrieve the channel represented by the supplied ID.
     *
     * @param channelId The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Channel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Channel> getChannelById(final Snowflake channelId) {
        final Mono<ChannelBean> channel = gatewayResources.getStateView().getChannelStore()
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
        return gatewayResources.getStateView().getGuildStore()
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
        return gatewayResources.getStateView().getGuildEmojiStore()
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
        final Mono<MemberBean> member = gatewayResources.getStateView().getMemberStore()
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
        return gatewayResources.getStateView().getMessageStore()
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
        return gatewayResources.getStateView().getRoleStore()
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

        return gatewayResources.getStateView().getGuildStore()
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
        return gatewayResources.getStateView().getUserStore()
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
        final long selfId = gatewayResources.getStateView().getSelfId();
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
     * Gets the bot user's ID.
     *
     * @return The bot user's ID.
     */
    public Snowflake getSelfId() {
        return Snowflake.of(gatewayResources.getStateView().getSelfId());
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
        return gatewayResources.getStateView().getUserStore()
                .find(userId.asLong())
                .switchIfEmpty(getRestClient().getUserService()
                        .getUser(userId.asLong())
                        .map(UserBean::new));
    }

    /**
     * Disconnects this {@link GatewayDiscordClient} from Discord upon subscribing. All joining {@link GatewayClient
     * GatewayClients} will attempt to gracefully close and complete this {@link Mono} after all of them have
     * disconnected.
     *
     * @return A {@link Mono} that upon subscription, will disconnect each connection established by this
     * {@link GatewayDiscordClient} and complete after all of them have closed.
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
     * @return a {@link Mono} that will complete once all {@link GatewayClient} instances connected to this
     * {@link GatewayDiscordClient} have disconnected.
     */
    public Mono<Void> onDisconnect() {
        return closeProcessor;
    }

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type.
     * <p>
     * <strong>Note: </strong> Errors occurring while processing events will terminate your sequence. If you wish to use
     * a version capable of handling errors for you, use {@link #on(Class, Function)}. See
     * <a href="https://github.com/reactive-streams/reactive-streams-jvm#1.7">Reactive Streams Spec</a>
     * explaining this behavior.
     * </p>
     * <p>
     * A recommended pattern to use this method is wrapping your code that may throw exceptions within a {@code
     * flatMap} block and use {@link Mono#onErrorResume(Function)}, {@link Flux#onErrorResume(Function)} or
     * equivalent methods to maintain the sequence active:
     * </p>
     * <pre>
     * client.on(MessageCreateEvent.class)
     *     .flatMap(event -&gt; myCodeThatMightThrow(event)
     *             .onErrorResume(error -&gt; {
     *                 // log and then discard the error to keep the sequence alive
     *                 log.error("Failed to handle event!", error);
     *                 return Mono.empty();
     *             }))
     *     .subscribe();
     * </pre>
     * <p>
     * For more alternatives to handling errors, please see
     * <a href="https://github.com/Discord4J/Discord4J/wiki/Error-Handling">Error Handling</a> wiki page.
     * </p>
     *
     * @param eventClass the event class to obtain events from
     * @param <E> the type of the event class
     * @return a new {@link reactor.core.publisher.Flux} with the requested events
     */
    public <E extends Event> Flux<E> on(Class<E> eventClass) {
        return getEventDispatcher().on(eventClass);
    }

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type, processing them through a given
     * {@link Function}. Errors occurring within the mapper will be logged and discarded, preventing the termination of
     * the "infinite" event sequence.
     * <p>
     * There are multiple ways of using this event handling method, for example:
     * </p>
     * <pre>
     * client.on(MessageCreateEvent.class, event -> {
     *         // myCodeThatMightThrow should return a Reactor type (Mono or Flux)
     *         return myCodeThatMightThrow(event);
     *     })
     *     .subscribe();
     *
     * client.on(MessageCreateEvent.class, event -> {
     *         // myCodeThatMightThrow *can* be blocking
     *         myCodeThatMightThrow(event);
     *         return Mono.empty(); // but we have to return a Reactor type
     *     })
     *     .subscribe();
     * </pre>
     * <p>
     * Continuing the chain after {@code on(class, event -> ...)} will require your own error handling strategy.
     * Check the docs for {@link #on(Class)} for more details.
     * </p>
     *
     * @param eventClass the event class to obtain events from
     * @param mapper an event mapping function called on each event. If you do not wish to perform further operations
     * you can return {@code Mono.empty()}.
     * @param <E> the type of the event class
     * @param <T> the type of the event mapper function
     * @return a new {@link reactor.core.publisher.Flux} with the type resulting from the given event mapper
     */
    public <E extends Event, T> Flux<T> on(Class<E> eventClass, Function<E, Publisher<T>> mapper) {
        return on(eventClass)
                .flatMap(event -> Flux.from(mapper.apply(event))
                        .onErrorResume(t -> {
                            log.warn("Error while handling {} in shard [{}]", eventClass.getSimpleName(),
                                    event.getShardInfo().format(), t);
                            return Mono.empty();
                        }));
    }
}
