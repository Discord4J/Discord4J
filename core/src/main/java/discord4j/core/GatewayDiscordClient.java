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
import discord4j.common.util.Snowflake;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.object.Invite;
import discord4j.core.object.Region;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.retriever.EntityRetriever;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.core.spec.GuildCreateSpec;
import discord4j.core.spec.UserEditMono;
import discord4j.core.util.ValidationUtil;
import discord4j.discordjson.json.ActivityUpdateRequest;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.RoleData;
import discord4j.discordjson.json.gateway.GuildMembersChunk;
import discord4j.discordjson.json.gateway.RequestGuildMembers;
import discord4j.discordjson.json.gateway.StatusUpdate;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.GatewayClientGroup;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.ShardGatewayPayload;
import discord4j.rest.RestClient;
import discord4j.rest.RestResources;
import discord4j.voice.LocalVoiceConnectionRegistry;
import discord4j.voice.VoiceConnection;
import discord4j.voice.VoiceConnectionFactory;
import discord4j.voice.VoiceConnectionRegistry;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
public class GatewayDiscordClient implements EntityRetriever {

    private static final Logger log = Loggers.getLogger(GatewayDiscordClient.class);

    private final DiscordClient discordClient;
    private final GatewayResources gatewayResources;
    private final MonoProcessor<Void> closeProcessor;
    private final GatewayClientGroup gatewayClientGroup;
    private final VoiceConnectionFactory voiceConnectionFactory;
    private final VoiceConnectionRegistry voiceConnectionRegistry;
    private final EntityRetriever entityRetriever;

    public GatewayDiscordClient(DiscordClient discordClient, GatewayResources gatewayResources,
                                MonoProcessor<Void> closeProcessor, GatewayClientGroup gatewayClientGroup,
                                VoiceConnectionFactory voiceConnectionFactory,
                                EntityRetrievalStrategy entityRetrievalStrategy) {
        this.discordClient = discordClient;
        this.gatewayResources = gatewayResources;
        this.closeProcessor = closeProcessor;
        this.gatewayClientGroup = gatewayClientGroup;
        this.voiceConnectionFactory = voiceConnectionFactory;
        this.voiceConnectionRegistry = new LocalVoiceConnectionRegistry();
        this.entityRetriever = entityRetrievalStrategy.apply(this);
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
     * Return the {@link VoiceConnectionRegistry} for this {@link GatewayDiscordClient}. This allows you to retrieve
     * currently registered {@link VoiceConnection} instances.
     *
     * @return a {@link VoiceConnectionRegistry} for voice connections
     */
    public VoiceConnectionRegistry getVoiceConnectionRegistry() {
        return voiceConnectionRegistry;
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
     * Gets the bot user's ID.
     *
     * @return The bot user's ID.
     */
    public Snowflake getSelfId() {
        return getCoreResources().getSelfId();
    }

    /**
     * Requests to create a guild.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> createGuild(final Consumer<? super GuildCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    GuildCreateSpec mutatedSpec = new GuildCreateSpec();
                    spec.accept(mutatedSpec);
                    return getRestClient().getGuildService().createGuild(mutatedSpec.asRequest());
                })
                .map(data -> new Guild(this, toGuildData(data)));
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
                .channels(Collections.emptyList())
                .members(Collections.emptyList())
                .joinedAt(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now())) // we just created this
                .large(false)
                .memberCount(1)
                .build();
    }

    /**
     * Update the bot's {@link Presence} (client status) for every shard in this shard group.
     * <p>
     * Factories exist to build an {@link StatusUpdate} object to update the bot's status:
     * <ul>
     *     <li>{@link Presence#online()} and {@link Presence#online(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#idle()} and {@link Presence#idle(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#doNotDisturb()} and {@link Presence#doNotDisturb(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#invisible()}</li>
     * </ul>
     * <p>
     * Factories exist to build an {@link ActivityUpdateRequest} object for {@link StatusUpdate}:
     * <ul>
     *     <li>{@link Activity#listening(String)}</li>
     *     <li>{@link Activity#playing(String)}</li>
     *     <li>{@link Activity#streaming(String, String)}</li>
     *     <li>{@link Activity#watching(String)}</li>
     * </ul>
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
     * <p>
     * Factories exist to build an {@link StatusUpdate} object to update the bot's status:
     * <ul>
     *     <li>{@link Presence#online()} and {@link Presence#online(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#idle()} and {@link Presence#idle(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#doNotDisturb()} and {@link Presence#doNotDisturb(ActivityUpdateRequest)}</li>
     *     <li>{@link Presence#invisible()}</li>
     * </ul>
     * <p>
     * Factories exist to build an {@link ActivityUpdateRequest} object for {@link StatusUpdate}:
     * <ul>
     *     <li>{@link Activity#listening(String)}</li>
     *     <li>{@link Activity#playing(String)}</li>
     *     <li>{@link Activity#streaming(String, String)}</li>
     *     <li>{@link Activity#watching(String)}</li>
     * </ul>
     *
     * @param statusUpdate The updated client presence.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> updatePresence(final StatusUpdate statusUpdate, final int shardId) {
        return gatewayClientGroup.unicast(ShardGatewayPayload.statusUpdate(statusUpdate, shardId));
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
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link User}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public UserEditMono edit() {
        return new UserEditMono(this);
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
     * @return a new {@link Flux} with the type resulting from the given event mapper
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

    /**
     * Return all {@link Member members} from the given {@link Guild guildId} using the current Gateway connection.
     * This method performs a check to validate whether the given guild's data can be obtained from this
     * {@link GatewayDiscordClient}.
     *
     * @param guildId the {@link Snowflake} of the guild to obtain members from
     * @return a {@link Flux} of {@link Member} for the given {@link Guild}. If an error occurs, it is emitted through
     * the {@link Flux}.
     */
    public Flux<Member> requestMembers(Snowflake guildId) {
        return requestMembers(RequestGuildMembers.builder()
                .guildId(guildId.asString())
                .query("")
                .limit(0)
                .build());
    }

    /**
     * Return a set of {@link Member members} from the given {@link Guild guildId} using the current Gateway connection.
     * This method performs a check to validate whether the given guild's data can be obtained from this
     * {@link GatewayDiscordClient}.
     *
     * @param guildId the {@link Snowflake} of the guild to obtain members from
     * @param userIds the {@link Snowflake} set of users to request
     * @return a {@link Flux} of {@link Member} for the given {@link Guild}. If an error occurs, it is emitted through
     * the {@link Flux}.
     */
    public Flux<Member> requestMembers(Snowflake guildId, Set<Snowflake> userIds) {
        return requestMembers(RequestGuildMembers.builder()
                .guildId(guildId.asString())
                .userIds(userIds.stream().map(Snowflake::asString).collect(Collectors.toList()))
                .limit(0)
                .build());
    }

    /**
     * Submit a {@link RequestGuildMembers} payload using the current Gateway connection and wait for its completion,
     * delivering {@link Member} elements asynchronously through a {@link Flux}. This method performs a check to
     * validate whether the given guild's data can be obtained from this {@link GatewayDiscordClient}.
     *
     * @param request the member request to submit. Create one using {@link RequestGuildMembers#builder()}.
     * @return a {@link Flux} of {@link Member} for the given {@link Guild}. If an error occurs, it is emitted through
     * the {@link Flux}.
     */
    public Flux<Member> requestMembers(RequestGuildMembers request) {
        try {
            ValidationUtil.validateRequestGuildMembers(request, gatewayResources.getIntents());
        } catch (Throwable t) {
            return Flux.error(t);
        }
        Snowflake guildId = Snowflake.of(request.guildId());
        int shardId = gatewayClientGroup.computeShardIndex(guildId);
        String nonce = String.valueOf(System.nanoTime());
        Supplier<Flux<Member>> incomingMembers = () -> gatewayClientGroup.find(shardId)
                .map(gatewayClient -> gatewayClient.dispatch()
                        .ofType(GuildMembersChunk.class)
                        .takeUntilOther(closeProcessor)
                        .filter(chunk -> chunk.nonce().toOptional()
                                .map(s -> s.equals(nonce))
                                .orElse(false))
                        .takeUntil(chunk -> chunk.chunkIndex() + 1 == chunk.chunkCount())
                        .flatMapIterable(chunk -> chunk.members().stream()
                                .map(data -> new Member(this, data, guildId.asLong()))
                                .collect(Collectors.toList())))
                .orElseThrow(() -> new IllegalStateException("Unable to find gateway client"));
        return getGuildById(guildId) // check if this operation is valid otherwise request+waiting will hang
                .then(gatewayClientGroup.unicast(ShardGatewayPayload.requestGuildMembers(
                        RequestGuildMembers.builder()
                                .from(request)
                                .nonce(nonce)
                                .build(), shardId)))
                .thenMany(Flux.defer(incomingMembers));
    }

    /**
     * Applies the given strategy to retrieve entities using this {@link GatewayDiscordClient}.
     *
     * @param retrievalStrategy the strategy to apply
     * @return an EntityRetriever able to retrieve entities using the given strategy
     */
    public EntityRetriever withRetrievalStrategy(EntityRetrievalStrategy retrievalStrategy) {
        return retrievalStrategy.apply(this);
    }

    @Override
    public Mono<Channel> getChannelById(Snowflake channelId) {
        return entityRetriever.getChannelById(channelId);
    }

    @Override
    public Mono<Guild> getGuildById(Snowflake guildId) {
        return entityRetriever.getGuildById(guildId);
    }

    @Override
    public Mono<GuildEmoji> getGuildEmojiById(Snowflake guildId, Snowflake emojiId) {
        return entityRetriever.getGuildEmojiById(guildId, emojiId);
    }

    @Override
    public Mono<Member> getMemberById(Snowflake guildId, Snowflake userId) {
        return entityRetriever.getMemberById(guildId, userId);
    }

    @Override
    public Mono<Message> getMessageById(Snowflake channelId, Snowflake messageId) {
        return entityRetriever.getMessageById(channelId, messageId);
    }

    @Override
    public Mono<Role> getRoleById(Snowflake guildId, Snowflake roleId) {
        return entityRetriever.getRoleById(guildId, roleId);
    }

    @Override
    public Mono<User> getUserById(Snowflake userId) {
        return entityRetriever.getUserById(userId);
    }

    @Override
    public Flux<Guild> getGuilds() {
        return entityRetriever.getGuilds();
    }

    @Override
    public Mono<User> getSelf() {
        return entityRetriever.getSelf();
    }

    @Override
    public Flux<Member> getGuildMembers(Snowflake guildId) {
        return entityRetriever.getGuildMembers(guildId);
    }

    @Override
    public Flux<GuildChannel> getGuildChannels(Snowflake guildId) {
        return entityRetriever.getGuildChannels(guildId);
    }

    @Override
    public Flux<Role> getGuildRoles(Snowflake guildId) {
        return entityRetriever.getGuildRoles(guildId);
    }

    @Override
    public Flux<GuildEmoji> getGuildEmojis(Snowflake guildId) {
        return entityRetriever.getGuildEmojis(guildId);
    }
}
