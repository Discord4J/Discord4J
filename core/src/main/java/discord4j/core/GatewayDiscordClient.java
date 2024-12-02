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
import discord4j.common.annotations.Experimental;
import discord4j.common.store.action.read.ReadActions;
import discord4j.common.util.Snowflake;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.Event;
import discord4j.core.object.GuildTemplate;
import discord4j.core.object.Invite;
import discord4j.core.object.Region;
import discord4j.core.object.ScheduledEventUser;
import discord4j.core.object.VoiceState;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.monetization.Entitlement;
import discord4j.core.object.monetization.SKU;
import discord4j.core.object.entity.channel.StageChannel;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Presence;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.retriever.EntityRetriever;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.core.spec.CreateTestEntitlementMono;
import discord4j.core.spec.EntitlementListRequestFlux;
import discord4j.core.spec.GuildCreateMono;
import discord4j.core.spec.GuildCreateSpec;
import discord4j.core.spec.UserEditMono;
import discord4j.core.spec.UserEditSpec;
import discord4j.core.spec.legacy.LegacyGuildCreateSpec;
import discord4j.core.spec.legacy.LegacyUserEditSpec;
import discord4j.core.util.ValidationUtil;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.RoleData;
import discord4j.discordjson.json.UpdateCurrentUserVoiceStateRequest;
import discord4j.discordjson.json.gateway.GuildMembersChunk;
import discord4j.discordjson.json.gateway.RequestGuildMembers;
import discord4j.discordjson.possible.Possible;
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
import io.netty.handler.timeout.TimeoutException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static discord4j.common.LogUtil.format;

/**
 * An aggregation of all dependencies Discord4J requires to operate with the Discord Gateway, REST API and Voice
 * Gateway. Maintains a set of connections to every shard logged in from the same {@link GatewayBootstrap} and therefore
 * tracks state updates from all connected shards.
 * <p>
 * The following are some resources available through this aggregate:
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
    private final Mono<Void> onDisconnect;
    private final GatewayClientGroup gatewayClientGroup;
    private final VoiceConnectionFactory voiceConnectionFactory;
    private final VoiceConnectionRegistry voiceConnectionRegistry;
    private final EntityRetriever entityRetriever;
    private final Set<String> completingChunkNonces;

    public GatewayDiscordClient(DiscordClient discordClient, GatewayResources gatewayResources,
                                Mono<Void> onDisconnect, GatewayClientGroup gatewayClientGroup,
                                VoiceConnectionFactory voiceConnectionFactory,
                                EntityRetrievalStrategy entityRetrievalStrategy,
                                Set<String> completingChunkNonces) {
        this.discordClient = discordClient;
        this.gatewayResources = gatewayResources;
        this.onDisconnect = onDisconnect;
        this.gatewayClientGroup = gatewayClientGroup;
        this.voiceConnectionFactory = voiceConnectionFactory;
        this.voiceConnectionRegistry = new LocalVoiceConnectionRegistry();
        this.entityRetriever = entityRetrievalStrategy.apply(this);
        this.completingChunkNonces = completingChunkNonces;
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
     * Requests to retrieve the webhook represented by the supplied ID. The bot must have the MANAGE_WEBHOOKS
     * permission in the webhook's channel.
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
     * Requests to retrieve the webhook represented by the supplied ID and token. Doesn't
     * return the user who created the webhook object. Doesn't require the bot to have the MANAGE_WEBHOOKS permission.
     *
     * @param webhookId The ID of the webhook.
     * @param token The authentication token of the webhook.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook} as represented by the
     * supplied ID without the user field and with the token field. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> getWebhookByIdWithToken(final Snowflake webhookId, final String token) {
        return getRestClient().getWebhookService()
                .getWebhookWithToken(webhookId.asLong(), token)
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
        return Flux.from(gatewayResources.getStore().execute(ReadActions.getUsers()))
                .map(data -> new User(this, data));
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
     * Requests to retrieve the template represented by the supplied code.
     *
     * @param templateCode The code of the template.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildTemplate} as represented by the
     * supplied code. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildTemplate> getTemplateByCode(String templateCode) {
        return getRestClient().getTemplateService()
                .getTemplate(templateCode)
                .map(data -> new GuildTemplate(this, data));
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
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyGuildCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createGuild(GuildCreateSpec)} or {@link #createGuild(String, Region)} which offer an
     * immutable approach to build specs
     */
    @Deprecated
    public Mono<Guild> createGuild(final Consumer<? super LegacyGuildCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyGuildCreateSpec mutatedSpec = new LegacyGuildCreateSpec();
                    spec.accept(mutatedSpec);
                    return getRestClient().getGuildService().createGuild(mutatedSpec.asRequest());
                })
                .map(data -> new Guild(this, toGuildData(data)));
    }

    /**
     * Requests to create a guild. Properties specifying how to create the guild can be set via the {@code withXxx}
     * methods of the returned {@link GuildCreateMono}.
     *
     * @param name   the name of the guild to create
     * @param region the region of the guild to create
     * @return A {@link GuildCreateMono} where, upon successful completion, emits the created {@link Guild}. If an error
     * is received, it is emitted through the {@code GuildCreateMono}.
     */
    public GuildCreateMono createGuild(String name, Region region) {
        return GuildCreateMono.of(name, region, this);
    }

    /**
     * Requests to create a guild.
     *
     * @param spec an immutable object that specifies how to create the guild
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> createGuild(GuildCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> getRestClient().getGuildService().createGuild(spec.asRequest()))
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
     * Update the bot's {@link ClientPresence} (client status) for every shard in this shard group.
     *
     * @param clientPresence The updated client status.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> updatePresence(final ClientPresence clientPresence) {
        return gatewayClientGroup.multicast(GatewayPayload.statusUpdate(clientPresence.getStatusUpdate()));
    }

    /**
     * Update the bot's {@link Presence} (status) for the given shard index, provided it belongs in this shard group.
     *
     * @param clientPresence The updated client presence.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> updatePresence(final ClientPresence clientPresence, final int shardId) {
        return gatewayClientGroup.unicast(ShardGatewayPayload.statusUpdate(clientPresence.getStatusUpdate(), shardId));
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
     * Requests to retrieve an invite.
     *
     * @param inviteCode the code for the invite (e.g. "xdYkpp").
     * @param withCounts whether the invite should contain approximate member counts
     * @param withExpiration whether the invite should contain the expiration date
     * @param guildScheduledEventId the guild scheduled event to include with the invite, can be {@code null}
     * @return A {@link Mono} where, upon successful completion, emits the {@link Invite} as represented by the
     * supplied invite code. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Invite> getInvite(String inviteCode, boolean withCounts, boolean withExpiration,
                                  @Nullable Snowflake guildScheduledEventId) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("with_counts", withCounts);
        queryParams.put("with_expiration", withExpiration);
        if (guildScheduledEventId != null) {
            queryParams.put("guild_scheduled_event_id", guildScheduledEventId.asString());
        }
        return getRestClient().getInviteService()
                .getInvite(inviteCode, queryParams)
                .map(data -> new Invite(this, data));
    }

    /**
     * Requests to edit this client (i.e., modify the current bot user).
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyUserEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link User}. If an error is received,
     * it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(UserEditSpec)} or {@link #edit()} which offer an immutable approach to build specs
     */
    @Deprecated
    public Mono<User> edit(final Consumer<? super LegacyUserEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyUserEditSpec mutatedSpec = new LegacyUserEditSpec();
                    spec.accept(mutatedSpec);
                    return getRestClient().getUserService().modifyCurrentUser(mutatedSpec.asRequest());
                })
                .map(data -> new User(this, data));
    }

    /**
     * Requests to edit this client (i.e., modify the current bot user). Properties specifying how to edit this client
     * can be set via the {@code withXxx} methods of the returned {@link UserEditMono}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link User}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public UserEditMono edit() {
        return UserEditMono.of(this);
    }

    /**
     * Requests to edit this client (i.e., modify the current bot user).
     *
     * @param spec an immutable object that specifies how to edit this client
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link User}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<User> edit(UserEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> getRestClient().getUserService().modifyCurrentUser(spec.asRequest()))
                .map(data -> new User(this, data));
    }

    /**
     * Requests to retrieve a {@link StageInstance}.
     *
     * @param channelId The channel ID associated to the {@link StageInstance}.
     * @param retrievalStrategy The retreival strategy to use
     * @return A {@link Mono} where, upon successful completion, emits the {@link StageInstance} associated to the
     * supplied channel ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<StageInstance> getStageInstanceByChannelId(Snowflake channelId, EntityRetrievalStrategy retrievalStrategy) {
        Objects.requireNonNull(channelId);
        Objects.requireNonNull(retrievalStrategy);
        return withRetrievalStrategy(retrievalStrategy).getStageInstanceByChannelId(channelId);
    }

    /**
     * Requests to retrieve a {@link StageInstance}.
     *
     * @param channelId The channel ID associated to the {@link StageInstance}.
     * @return A {@link Mono} where, upon successful completion, emits the {@link StageInstance} associated to the
     * supplied channel ID. If an error is received, it is emitted through the {@code Mono}.
     */
    @Override
    public Mono<StageInstance> getStageInstanceByChannelId(Snowflake channelId) {
        Objects.requireNonNull(channelId);
        return entityRetriever.getStageInstanceByChannelId(channelId);
    }

    /**
     * Move the {@link Member} represented by this {@link GatewayDiscordClient} to the stage speakers.
     * Requires the {@link Member} to be connected to a {@link StageChannel}
     *
     * @return A {@link Mono} that upon subscription, will move the {@link Member} represented by this
     * {@link GatewayDiscordClient} to the stage speakers.
     */
    public Mono<Void> joinStageSpeakers(Snowflake guildId) {
        Objects.requireNonNull(guildId);
        return Mono.defer(() -> getRestClient().getGuildService()
                .modifySelfVoiceState(guildId.asLong(),
                        UpdateCurrentUserVoiceStateRequest.builder().suppress(false).build()));
    }

    /**
     * Move the {@link Member} represented by this {@link GatewayDiscordClient} to the stage audience.
     * Requires the {@link Member} to be connected to a {@link StageChannel}
     *
     * @return A {@link Mono} that upon subscription, will move the {@link Member} represented by this
     * {@link GatewayDiscordClient} to the stage audience.
     */
    public Mono<Void> joinStageAudience(Snowflake guildId) {
        Objects.requireNonNull(guildId);
        return Mono.defer(() -> getRestClient().getGuildService()
                .modifySelfVoiceState(guildId.asLong(),
                        UpdateCurrentUserVoiceStateRequest.builder().suppress(true).build()));
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
        return onDisconnect;
    }

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type. This {@link Flux} has to be subscribed to
     * in order to start processing. See {@link Event} class for the list of possible event classes.
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
     * <a href="https://docs.discord4j.com/error-handling">Error Handling</a> docs page.
     *
     * @param eventClass the event class to obtain events from
     * @param <E> the type of the event class
     * @return a new {@link reactor.core.publisher.Flux} with the requested events
     */
    public <E extends Event> Flux<E> on(Class<E> eventClass) {
        return getEventDispatcher().on(eventClass)
                .contextWrite(ctx -> ctx.put(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(hashCode())));
    }

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type, to be processed through a given
     * {@link Function} upon subscription. Errors occurring within the mapper will be logged and discarded, preventing
     * the termination of the "infinite" event sequence. See {@link Event} class for the list of possible event classes.
     * <p>
     * There are multiple ways of using this event handling method, for example:
     * <pre>
     * client.on(MessageCreateEvent.class, event -&gt; {
     *         // myCodeThatMightThrow should return a Reactor type (Mono or Flux)
     *         return myCodeThatMightThrow(event);
     *     })
     *     .subscribe();
     *
     * client.on(MessageCreateEvent.class, event -&gt; {
     *         // myCodeThatMightThrow *can* be blocking, so wrap it in a Reactor type
     *         return Mono.fromRunnable(() -&gt; myCodeThatMightThrow(event));
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
        return getEventDispatcher().on(eventClass, mapper)
                .contextWrite(ctx -> ctx.put(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(hashCode())));
    }

    /**
     * Applies a given {@code adapter} to all events from this dispatcher. Errors occurring within the mapper will be
     * logged and discarded, preventing the termination of the "infinite" event sequence. This variant allows you to
     * have a single subscriber to this dispatcher, which is useful to collect all startup events.
     * <p>
     * A standard approach to this method is to subclass {@link ReactiveEventAdapter}, overriding the methods you want
     * to listen for:
     * <pre>
     * client.on(new ReactiveEventAdapter() {
     *
     *     public Publisher&lt;?&gt; onReady(ReadyEvent event) {
     *         return Mono.fromRunnable(() -&gt;
     *                 System.out.println("Connected as " + event.getSelf().getTag()));
     *     }
     *
     *     public Publisher&lt;?&gt; onMessageCreate(MessageCreateEvent event) {
     *         if (event.getMessage().getContent().equals("!ping")) {
     *             return event.getMessage().getChannel()
     *                     .flatMap(channel -&gt; channel.createMessage("Pong!"));
     *         }
     *         return Mono.empty();
     *     }
     *
     * }).subscribe(); // nothing happens until you subscribe
     * </pre>
     * <p>
     * Each method requires a {@link Publisher} return like {@link Mono} or {@link Flux} and all errors
     * will be logged and discarded. To use a synchronous implementation you can wrap your code with
     * {@link Mono#fromRunnable(Runnable)}.
     * <p>
     * Continuing the chain will require your own error handling strategy.
     * Check the docs for {@link #on(Class)} for more details.
     *
     * @param adapter an adapter meant to be subclassed with its appropriate methods overridden
     * @return a new {@link Flux} with the type resulting from the given event mapper
     */
    public Flux<Event> on(ReactiveEventAdapter adapter) {
        return getEventDispatcher().on(adapter)
                .contextWrite(ctx -> ctx.put(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(hashCode())));
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
        return Flux.fromIterable(userIds)
                .map(Snowflake::asString)
                .buffer(100)
                .concatMap(userIdBuffer -> requestMembers(RequestGuildMembers.builder()
                        .guildId(guildId.asString())
                        .userIds(userIdBuffer)
                        .limit(0)
                        .build()));
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
        Snowflake guildId = Snowflake.of(request.guildId());
        return requestMemberChunks(request)
                .flatMapIterable(chunk -> chunk.members().stream()
                        .map(data -> new Member(this, data, guildId.asLong()))
                        .collect(Collectors.toList()));
    }

    /**
     * Submit a {@link RequestGuildMembers} payload using the current Gateway connection and wait for its completion,
     * delivering raw {@link GuildMembersChunk} elements asynchronously through a {@link Flux}. This method performs a
     * check to validate whether the given guild's data can be obtained from this {@link GatewayDiscordClient}.
     * <p>
     * A timeout given by is used to fail this request if the operation is unable to complete due to disallowed or
     * disabled members intent. This is particularly relevant when requesting a complete member list. If the timeout is
     * triggered, a {@link TimeoutException} is forwarded through the {@link Flux}.
     *
     * @param request the member request to submit. Create one using {@link RequestGuildMembers#builder()}.
     * {@link Flux#timeout(Duration)}
     * @return a {@link Flux} of {@link GuildMembersChunk} for the given {@link Guild}. If an error occurs, it is
     * emitted through the {@link Flux}.
     */
    public Flux<GuildMembersChunk> requestMemberChunks(RequestGuildMembers request) {
        try {
            // client-side validation is required to avoid indefinitely waiting for a response
            ValidationUtil.validateRequestGuildMembers(request, Possible.of(gatewayResources.getIntents()));
        } catch (Throwable t) {
            return Flux.error(t);
        }
        Snowflake guildId = Snowflake.of(request.guildId());
        int shardId = gatewayClientGroup.computeShardIndex(guildId);
        String nonce = String.valueOf(System.nanoTime());
        Supplier<Flux<GuildMembersChunk>> incomingMembers = () -> gatewayClientGroup.find(shardId)
                .map(gatewayClient -> gatewayClient.dispatch()
                        .ofType(GuildMembersChunk.class)
                        .takeUntilOther(onDisconnect)
                        .filter(chunk -> chunk.nonce().toOptional()
                                .map(s -> s.equals(nonce))
                                .orElse(false))
                        .takeUntil(chunk -> chunk.chunkIndex() + 1 == chunk.chunkCount()))
                .orElseThrow(() -> new IllegalStateException("Unable to find gateway client"));
        return Flux.deferContextual(ctx -> getGuildById(guildId)
                .then(gatewayClientGroup.unicast(ShardGatewayPayload.requestGuildMembers(
                        RequestGuildMembers.builder()
                                .from(request)
                                .nonce(nonce)
                                .build(), shardId))
                        .then(Mono.fromRunnable(() -> {
                            if (request.query().toOptional().map(String::isEmpty).orElse(false)
                                    && request.limit() == 0) {
                                completingChunkNonces.add(nonce);
                            }
                        })))
                .thenMany(Flux.defer(incomingMembers))
                .doOnComplete(() -> log.debug(format(ctx, "Member request completed: {}"), request)));
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
    public Mono<GuildSticker> getGuildStickerById(Snowflake guildId, Snowflake stickerId) {
        return entityRetriever.getGuildStickerById(guildId, stickerId);
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
    public Mono<Member> getSelfMember(Snowflake guildId) {
        return entityRetriever.getSelfMember(guildId);
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

    @Override
    public Flux<GuildSticker> getGuildStickers(Snowflake guildId) {
        return entityRetriever.getGuildStickers(guildId);
    }

    @Override
    public Mono<ThreadMember> getThreadMemberById(Snowflake threadId, Snowflake userId) {
        return entityRetriever.getThreadMemberById(threadId, userId);
    }

    @Override
    public Flux<ThreadMember> getThreadMembers(Snowflake threadId) {
        return entityRetriever.getThreadMembers(threadId);
    }

    @Override
    public Flux<AutoModRule> getGuildAutoModRules(Snowflake guildId) {
        return entityRetriever.getGuildAutoModRules(guildId);
    }

    @Override
    public Mono<ScheduledEvent> getScheduledEventById(Snowflake guildId, Snowflake eventId) {
        return entityRetriever.getScheduledEventById(guildId, eventId);
    }

    @Override
    public Flux<ScheduledEvent> getScheduledEvents(Snowflake guildId) {
        return entityRetriever.getScheduledEvents(guildId);
    }

    @Override
    public Flux<ScheduledEventUser> getScheduledEventUsers(Snowflake guildId, Snowflake eventId) {
        return entityRetriever.getScheduledEventUsers(guildId, eventId);
    }

    @Override
    public Mono<VoiceState> getVoiceStateById(Snowflake guildId, Snowflake userId) {
        return entityRetriever.getVoiceStateById(guildId, userId);
    }

    /**
     * Request to retrieve all the {@link SKU SKUs} for the current application.
     *
     * @return A {@link Flux} that emits the {@link SKU SKUs} for the application upon successful completion. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public Flux<SKU> getSKUs() {
        return getApplicationInfo().flatMapMany(applicationInfo -> {
            return getRestClient().getMonetizationService()
                .getAllSkus(applicationInfo.getId().asLong())
                .map(data -> new SKU(this, data));
        });
    }

    /**
     * Request to retrieve the {@link SKU SKU} for the application with the given ID.
     *
     * @param applicationId The ID of the application.
     * @return A {@link Mono} that emits the {@link SKU SKU} for the application with the given ID upon successful
     * completion. If an error is received, it is emitted through the {@code Mono}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public Flux<SKU> getSKUs(long applicationId) {
        return getRestClient().getMonetizationService()
            .getAllSkus(applicationId)
            .map(data -> new SKU(this, data));
    }

    /**
     * Request to retrieve all the {@link Entitlement} for the current application.
     * The request can be filtered using the "withXXX" methods of the returned {@link EntitlementListRequestFlux}.
     *
     * @return A {@link EntitlementListRequestFlux} that emits the {@link Entitlement} for the application with the given ID upon successful
     * completion. If an error is received, it is emitted through the {@code Mono}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public EntitlementListRequestFlux getEntitlements() {
        return EntitlementListRequestFlux.of(this, discordClient);
    }

    /**
     * Create a test entitlement for the given {@link SKU} and guild ID.
     *
     * @param skuId   The ID of the SKU.
     * @param guildId The ID of the guild.
     * @return A {@link CreateTestEntitlementMono} that emits the created {@link Entitlement} upon successful
     * completion. If an error is received, it is emitted through the {@code Mono}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public CreateTestEntitlementMono createTestEntitlementForGuild(Snowflake skuId, Snowflake guildId) {
        return CreateTestEntitlementMono.of(skuId, guildId, Entitlement.OwnerType.GUILD, this, discordClient);
    }

    /**
     * Create a test entitlement for the given {@link SKU} and user ID.
     *
     * @param skuId  The ID of the SKU.
     * @param userId The ID of the user.
     * @return A {@link CreateTestEntitlementMono} that emits the created {@link Entitlement} upon successful
     * completion. If an error is received, it is emitted through the {@code Mono}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public CreateTestEntitlementMono createTestEntitlementForUser(Snowflake skuId, Snowflake userId) {
        return CreateTestEntitlementMono.of(skuId, userId, Entitlement.OwnerType.USER, this, discordClient);
    }

    /**
     * Delete a test entitlement with the given ID.
     *
     * @param entitlementId The ID of the entitlement.
     * @return A {@link Mono} that completes upon successful deletion.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public Mono<Void> deleteTestEntitlement(Snowflake entitlementId) {
        return getApplicationInfo().flatMap(applicationInfo -> {
            return getRestClient().getMonetizationService()
                .deleteTestEntitlement(applicationInfo.getId().asLong(), entitlementId.asLong());
        });
    }

}
