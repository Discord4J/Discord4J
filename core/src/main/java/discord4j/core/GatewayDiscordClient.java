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
import discord4j.common.LogUtil;
import discord4j.common.ReactorResources;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.object.Invite;
import discord4j.core.object.Region;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.core.spec.GuildCreateSpec;
import discord4j.core.spec.UserEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.StatusUpdate;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.GatewayClientGroup;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.ShardGatewayPayload;
import discord4j.rest.RestClient;
import discord4j.rest.RestResources;
import discord4j.rest.util.PaginationUtil;
import discord4j.rest.util.Snowflake;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.voice.VoiceConnectionFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Map;
import java.util.Optional;
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
 *     <li>Access to {@link GatewayResources} that configure Gateway stores, actions and coordination among shards.</li>
 *     <li>Access to {@link EventDispatcher} publishing events from all participating shards.</li>
 * </ul>
 */
public class GatewayDiscordClient {

    private static final Logger log = Loggers.getLogger(GatewayDiscordClient.class);

    private final DiscordClient discordClient;
    private final GatewayResources gatewayResources;
    private final MonoProcessor<Void> closeProcessor;
    private final GatewayClientGroup gatewayClientGroup;
    private final VoiceConnectionFactory voiceConnectionFactory;

    public GatewayDiscordClient(DiscordClient discordClient, GatewayResources gatewayResources,
                                MonoProcessor<Void> closeProcessor, GatewayClientGroup gatewayClientGroup,
                                VoiceConnectionFactory voiceConnectionFactory) {
        this.discordClient = discordClient;
        this.gatewayResources = gatewayResources;
        this.closeProcessor = closeProcessor;
        this.gatewayClientGroup = gatewayClientGroup;
        this.voiceConnectionFactory = voiceConnectionFactory;
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
     * @return the {@link RestResources} for the parent {@link DiscordClient}
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
     * Returns a {@link GatewayClient} instance created by this {@link GatewayDiscordClient}, which is associated to
     * a given shard index.
     *
     * @param shardId the shard index used to get the client instance
     * @return a {@link GatewayClient} instance represented by the given shard, if present
     */
    public Optional<GatewayClient> getGatewayClient(int shardId) {
        return gatewayClientGroup.find(shardId);
    }

    /**
     * Returns the {@link GatewayClientGroup} capable of performing operations across all {@link GatewayClient}
     * instances created or managed by this {@link GatewayDiscordClient}.
     *
     * @return a {@link GatewayClientGroup} to aggregate gateway operations
     */
    public GatewayClientGroup getGatewayClientGroup() {
        return gatewayClientGroup;
    }

    /**
     * Returns the {@link VoiceConnectionFactory} instance created by this {@link GatewayDiscordClient}.
     *
     * @return a {@link VoiceConnectionFactory} instance capable of initiating voice connections
     */
    public VoiceConnectionFactory getVoiceConnectionFactory() {
        return voiceConnectionFactory;
    }

    /**
     * Returns the {@link RestClient} used to execute REST API requests.
     *
     * @return the {@link RestClient} tied to this Gateway client.
     */
    public RestClient getRestClient() {
        return rest();
    }

    /**
     * Requests to retrieve the channel represented by the supplied ID. If the channel is not found in the store, an
     * attempt to query the REST API will be made. If this behavior is not desired, see {@link #getChannel(Snowflake)}.
     *
     * @param channelId The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Channel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    @Deprecated
    public Mono<Channel> getChannelById(final Snowflake channelId) {
        final Mono<ChannelData> channel = gatewayResources.getStateView().getChannelStore()
                .find(channelId.asLong());

        final Mono<ChannelData> rest = getRestClient().getChannelService()
                .getChannel(channelId.asLong());

        return channel
                .switchIfEmpty(rest)
                .map(channelBean -> EntityUtil.getChannel(this, channelBean));
    }

    /**
     * Requests to retrieve the channel represented by the supplied ID. If the channel is not found in the store, the
     * resulting {@link Mono} will be empty.
     *
     * @param channelId The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Channel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Channel> getChannel(final Snowflake channelId) {
        return gatewayResources.getStateView().getChannelStore()
                .find(channelId.asLong())
                .map(channelBean -> EntityUtil.getChannel(this, channelBean));
    }

    /**
     * Requests to retrieve the guild represented by the supplied ID. If the channel is not found in the store, an
     * attempt to query the REST API will be made. If this behavior is not desired, see {@link #getChannel(Snowflake)}.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    @Deprecated
    public Mono<Guild> getGuildById(final Snowflake guildId) {
        return gatewayResources.getStateView().getGuildStore()
                .find(guildId.asLong())
                .switchIfEmpty(getRestClient().getGuildService()
                        .getGuild(guildId.asLong())
                        .flatMap(this::toGuildData))
                .map(data -> new Guild(this, data));
    }

    /**
     * Requests to retrieve the guild represented by the supplied ID. If the guild is not found in the store, the
     * resulting {@link Mono} will be empty.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(final Snowflake guildId) {
        return gatewayResources.getStateView().getGuildStore()
                .find(guildId.asLong())
                .map(data -> new Guild(this, data));
    }

    /**
     * Requests to retrieve the guild emoji represented by the supplied IDs. If the emoji is not found in the store,
     * an attempt to query the REST API will be made. If this behavior is not desired, see
     * {@link #getGuildEmoji(Snowflake, Snowflake)}.
     *
     * @param guildId The ID of the guild.
     * @param emojiId The ID of the emoji.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    @Deprecated
    public Mono<GuildEmoji> getGuildEmojiById(final Snowflake guildId, final Snowflake emojiId) {
        return gatewayResources.getStateView().getGuildEmojiStore()
                .find(emojiId.asLong())
                .switchIfEmpty(getRestClient().getEmojiService()
                        .getGuildEmoji(guildId.asLong(), emojiId.asLong()))
                .map(data -> new GuildEmoji(this, data, guildId.asLong()));
    }

    /**
     * Requests to retrieve the guild emoji represented by the supplied IDs. If the emoji is not found in the store,
     * the resulting {@link Mono} will be empty.
     *
     * @param guildId The ID of the guild.
     * @param emojiId The ID of the emoji.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> getGuildEmoji(final Snowflake guildId, final Snowflake emojiId) {
        return gatewayResources.getStateView().getGuildEmojiStore()
                .find(emojiId.asLong())
                .map(data -> new GuildEmoji(this, data, guildId.asLong()));
    }

    /**
     * Requests to retrieve the member represented by the supplied IDs. If the member is not found in the store, an
     * attempt to query the REST API will be made. If this behavior is not desired, see
     * {@link #getMember(Snowflake, Snowflake)}
     *
     * @param guildId The ID of the guild.
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    @Deprecated
    public Mono<Member> getMemberById(final Snowflake guildId, final Snowflake userId) {
        final Mono<MemberData> member = gatewayResources.getStateView().getMemberStore()
                .find(LongLongTuple2.of(guildId.asLong(), userId.asLong()))
                .switchIfEmpty(getRestClient().getGuildService()
                        .getGuildMember(guildId.asLong(), userId.asLong()));

        return member.map(memberData -> new Member(this, memberData, guildId.asLong()));
    }

    /**
     * Requests to retrieve the member represented by the supplied IDs. If the member is not found in the store, the
     * resulting {@link Mono} will be empty.
     *
     * @param guildId The ID of the guild.
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMember(final Snowflake guildId, final Snowflake userId) {
        return gatewayResources.getStateView().getMemberStore()
                .find(LongLongTuple2.of(guildId.asLong(), userId.asLong()))
                .map(memberData -> new Member(this, memberData, guildId.asLong()));
    }

    /**
     * Requests to retrieve the message represented by the supplied IDs. If the message is not found in the store, an
     * attempt to query the REST API will be made. If this behavior is not desired, see
     * {@link #getMessage(Snowflake, Snowflake)}
     *
     * @param channelId The ID of the channel.
     * @param messageId The ID of the message.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    @Deprecated
    public Mono<Message> getMessageById(final Snowflake channelId, final Snowflake messageId) {
        return gatewayResources.getStateView().getMessageStore()
                .find(messageId.asLong())
                .switchIfEmpty(getRestClient().getChannelService()
                        .getMessage(channelId.asLong(), messageId.asLong()))
                .map(data -> new Message(this, data));
    }

    /**
     * Requests to retrieve the message represented by the supplied IDs. If the message is not found in the store, the
     * resulting {@link Mono} will be empty.
     *
     * @param channelId The ID of the channel.
     * @param messageId The ID of the message.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessage(final Snowflake channelId, final Snowflake messageId) {
        return gatewayResources.getStateView().getMessageStore()
                .find(messageId.asLong())
                .map(data -> new Message(this, data));
    }

    /**
     * Requests to retrieve the role represented by the supplied IDs. If the role is not found in the store, an
     * attempt to query the REST API will be made. If this behavior is not desired, see
     * {@link #getRole(Snowflake, Snowflake)}
     *
     * @param guildId The ID of the guild.
     * @param roleId The ID of the role.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    @Deprecated
    public Mono<Role> getRoleById(final Snowflake guildId, final Snowflake roleId) {
        return gatewayResources.getStateView().getRoleStore()
                .find(roleId.asLong())
                .switchIfEmpty(getRestClient().getGuildService()
                        .getGuildRoles(guildId.asLong())
                        .filter(response -> response.id().equals(roleId.asString()))
                        .singleOrEmpty())
                .map(roleBean -> new Role(this, roleBean, guildId.asLong()));
    }

    /**
     * Requests to retrieve the role represented by the supplied IDs. If the role is not found in the store, the
     * resulting {@link Mono} will be empty.
     *
     * @param guildId The ID of the guild.
     * @param roleId The ID of the role.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRole(final Snowflake guildId, final Snowflake roleId) {
        return gatewayResources.getStateView().getRoleStore()
                .find(roleId.asLong())
                .map(roleBean -> new Role(this, roleBean, guildId.asLong()));
    }

    /**
     * Requests to retrieve the user represented by the supplied ID. If the user is not found in the store, an
     * attempt to query the REST API will be made. If this behavior is not desired, see {@link #getUser(Snowflake)}
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    @Deprecated
    public Mono<User> getUserById(final Snowflake userId) {
        return gatewayResources.getStateView().getUserStore()
                .find(userId.asLong())
                .switchIfEmpty(getRestClient().getUserService()
                        .getUser(userId.asLong()))
                .map(userBean -> new User(this, userBean));
    }

    /**
     * Requests to retrieve the user represented by the supplied ID. If the user is not found in the store, the
     * resulting {@link Mono} will be empty.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser(final Snowflake userId) {
        return gatewayResources.getStateView().getUserStore()
                .find(userId.asLong())
                .map(userBean -> new User(this, userBean));
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
                .map(data -> new Webhook(this, data));
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
                .map(data -> new ApplicationInfo(this, data));
    }

    /**
     * Requests to retrieve the guilds the current client is in.
     *
     * @return A {@link Flux} that continually emits the {@link Guild guilds} that the current client is in. If an error
     * is received, it is emitted through the {@code Flux}.
     */
    @Deprecated
    public Flux<Guild> getGuilds() {
        final Function<Map<String, Object>, Flux<UserGuildData>> makeRequest = params ->
                getRestClient().getUserService().getCurrentUserGuilds(params);

        return gatewayResources.getStateView().getGuildStore()
                .values()
                .switchIfEmpty(PaginationUtil.paginateAfter(makeRequest, data -> Snowflake.asLong(data.id()),
                        0L, 100)
                        .map(UserGuildData::id)
                        //.filter(id -> (id >> 22) % getConfig().getShardCount() == getConfig().getShardIndex())
                        .flatMap(id -> getRestClient().getGuildService().getGuild(Snowflake.asLong(id)))
                        .flatMap(this::toGuildData))
                .map(data -> new Guild(this, data));
    }

    /**
     * Requests to retrieve the guilds the current client is in that are cached in the guild store.
     *
     * @return A {@link Flux} that continually emits the {@link Guild guilds} that the current client is in. If an error
     * is received, it is emitted through the {@code Flux}.
     */
    public Flux<Guild> getGuildsFromStore() {
        return gatewayResources.getStateView().getGuildStore().values().map(data -> new Guild(this, data));
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
                .map(data -> new Region(this, data));
    }

    /**
     * Requests to retrieve the bot user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the bot {@link User user}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getSelf() {
        return getSelfId()
                .flatMap(this::getUserById)
                .switchIfEmpty(getRestClient().getUserService()
                        .getCurrentUser()
                        .map(data -> new User(this, data)));
    }

    /**
     * Gets the bot user's ID.
     *
     * @return The bot user's ID.
     */
    public Mono<Snowflake> getSelfId() {
        return gatewayResources.getStateView().getSelfId().map(Snowflake::of);
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
                .flatMap(this::toGuildData)
                .map(data -> new Guild(this, data));
    }

    /**
     * Update the bot's {@link Presence} (client status) for every shard in this shard group.
     *
     * @param statusUpdate The updated client status.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> updatePresence(final StatusUpdate statusUpdate) {
        return gatewayClientGroup.multicast(GatewayPayload.statusUpdate(statusUpdate));
    }

    /**
     * Update the bot's {@link Presence} (status) for the given shard index, provided it belongs in this shard group.
     *
     * @param presence The updated client presence.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> updatePresence(final Presence presence, final int shardId) {
        return gatewayClientGroup.unicast(ShardGatewayPayload.statusUpdate(presence.asStatusUpdate(), shardId));
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
                .map(data -> new Invite(this, data));
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
                .map(data -> new User(this, data));
    }

    /**
     * Disconnects this {@link GatewayDiscordClient} from Discord upon subscribing. All {@link GatewayClient}
     * instances in this shard group will attempt to close their current Gateway session and complete this
     * {@link Mono} after all of them have disconnected.
     *
     * @return A {@link Mono} that upon subscription, will disconnect each Gateway connection established by this
     * {@link GatewayDiscordClient} and complete after all of them have closed.
     */
    public Mono<Void> logout() {
        return gatewayClientGroup.logout();
    }

    /**
     * Return a {@link Mono} that signals completion when all {@link GatewayClient} instances in this shard group have
     * disconnected.
     *
     * @return a {@link Mono} that will complete once all {@link GatewayClient} instances in this shard group have
     * disconnected.
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
     * <p>
     * A recommended pattern to use this method is wrapping your code that may throw exceptions within a {@code
     * flatMap} block and use {@link Mono#onErrorResume(Function)}, {@link Flux#onErrorResume(Function)} or
     * equivalent methods to maintain the sequence active:
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
     *
     * @param eventClass the event class to obtain events from
     * @param <E> the type of the event class
     * @return a new {@link reactor.core.publisher.Flux} with the requested events
     */
    public <E extends Event> Flux<E> on(Class<E> eventClass) {
        return getEventDispatcher().on(eventClass)
                .subscriberContext(ctx -> ctx.put(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(hashCode())));
    }

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type, processing them through a given
     * {@link Function}. Errors occurring within the mapper will be logged and discarded, preventing the termination of
     * the "infinite" event sequence.
     * <p>
     * There are multiple ways of using this event handling method, for example:
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
                .flatMap(event -> Flux.defer(() -> mapper.apply(event))
                        .onErrorResume(t -> {
                            log.warn("Error while handling {} in shard [{}]", eventClass.getSimpleName(),
                                    event.getShardInfo().format(), t);
                            return Mono.empty();
                        }));
    }

    private Mono<GuildData> toGuildData(GuildUpdateData guild) {
        return gatewayResources.getStateView().getGuildStore()
                .find(Snowflake.asLong(guild.id()))
                .map(current -> ImmutableGuildData.builder()
                        .from(guild)
                        .roles(guild.roles().stream()
                                .map(RoleData::id)
                                .collect(Collectors.toList()))
                        .emojis(guild.emojis().stream()
                                .map(EmojiData::id)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList()))
                        .channels(current.channels())
                        .members(current.members())
                        .build());
    }
}
