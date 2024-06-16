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
package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.*;
import discord4j.core.spec.legacy.LegacyWebhookEditSpec;
import discord4j.core.spec.legacy.LegacyWebhookEditWithTokenSpec;
import discord4j.core.spec.legacy.LegacyWebhookExecuteSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.WebhookData;
import discord4j.discordjson.json.WebhookPartialChannelData;
import discord4j.discordjson.json.WebhookPartialGuildData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A Discord webhook.
 *
 * @see <a href="https://discord.com/developers/docs/resources/webhook">Webhook Resource</a>
 */
public final class Webhook implements Entity {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final WebhookData data;

    /**
     * Constructs a {@code Webhook} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data    The raw data as represented by Discord, must be non-null.
     */
    public Webhook(final GatewayDiscordClient gateway, final WebhookData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the data of the webhook.
     *
     * @return The data of the webhook.
     */
    public WebhookData getData() {
        return data;
    }

    /**
     * Gets the type of the webhook.
     *
     * @return The type of the webhook.
     */
    public Type getType() {
        return Type.of(data.type());
    }

    /**
     * Gets the ID of the guild this webhook is associated to.
     *
     * @return The ID of the guild this webhook is associated to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(data.guildId().get().get()); // TODO FIXME: really Possible?
    }

    /**
     * Requests to retrieve the guild this webhook is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this webhook is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return gateway.getGuildById(getGuildId());
    }

    /**
     * Requests to retrieve the guild this webhook is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this webhook is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    /**
     * Gets the ID of the channel this webhook is associated to.
     *
     * @return The ID of the channel this webhook is associated to.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(data.channelId().get());
    }

    /**
     * Requests to retrieve the channel this webhook is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildMessageChannel channel} this
     * webhook is associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildMessageChannel> getChannel() {
        return gateway.getChannelById(getChannelId()).cast(GuildMessageChannel.class);
    }

    /**
     * Requests to retrieve the channel this webhook is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildMessageChannel channel} this
     * webhook is associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildMessageChannel> getChannel(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy)
                .getChannelById(getChannelId())
                .cast(GuildMessageChannel.class);
    }

    /**
     * Requests to retrieve the user this webhook was created by, if present. Returns no creator if the webhook was
     * retrieved using a token.
     *
     * @return An {@link Optional} with the {@link User user} this webhook was created by, if present.
     */
    public Optional<User> getCreator() {
        return data.user().toOptional().map(userData -> new User(gateway, userData));
    }

    /**
     * Gets the default name of the webhook.
     *
     * @return The default name of the webhook.
     */
    public Optional<String> getName() {
        return data.name();
    }

    /**
     * Gets the avatar of this webhook, if present.
     *
     * @return The avatar of this webhook, if present.
     */
    public Optional<String> getAvatar() {
        return data.avatar();
    }

    /**
     * Gets the secure token of this webhook. The token is present for {@link Type#INCOMING} webhooks.
     *
     * @return The secure token of this webhook.
     */
    public Optional<String> getToken() {
        return data.token().toOptional();
    }

    /**
     * Gets the bot/OAuth2 application ID that created this webhook.
     *
     * @return The bot/OAuth2 application ID that created this webhook.
     */
    public Optional<Snowflake> getApplicationId() {
        return data.applicationId().map(Snowflake::of);
    }

    /**
     * Gets the guild id of the channel that this webhook is following (returned for Channel Follower Webhooks), if
     * present.
     *
     * @return The guild id of the channel that this webhook is following (returned for Channel Follower Webhooks), if
     * present.
     */
    public Optional<Snowflake> getSourceGuildId() {
        return data.sourceGuild().toOptional().map(WebhookPartialGuildData::id).map(Snowflake::of);
    }

    /**
     * Gets the guild name of the channel that this webhook is following (returned for Channel Follower Webhooks), if
     * present.
     *
     * @return The guild name of the channel that this webhook is following (returned for Channel Follower Webhooks), if
     * present.
     */
    public Optional<String> getSourceGuildName() {
        return data.sourceGuild().toOptional().map(WebhookPartialGuildData::name);
    }

    /**
     * Gets the id of the channel that this webhook is following (returned for Channel Follower Webhooks), if present.
     *
     * @return The id of the channel that this webhook is following (returned for Channel Follower Webhooks), if
     * present.
     */
    public Optional<Snowflake> getSourceChannelId() {
        return data.sourceChannel().toOptional().map(WebhookPartialChannelData::id).map(Snowflake::of);
    }

    /**
     * Gets the name of the channel that this webhook is following (returned for Channel Follower Webhooks), if
     * present.
     *
     * @return The name of the channel that this webhook is following (returned for Channel Follower Webhooks), if
     * present.
     */
    public Optional<String> getSourceChannelName() {
        return data.sourceChannel().toOptional().map(WebhookPartialChannelData::name);
    }

    /**
     * Requests to delete this webhook. Requires the MANAGE_WEBHOOKS permission.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the webhook has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this webhook while optionally specifying a reason. Requires the MANAGE_WEBHOOKS permission.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the webhook has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return gateway.getRestClient().getWebhookService()
                .deleteWebhook(getId().asLong(), reason);
    }

    /**
     * Requests to delete this webhook.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the webhook has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> deleteWithToken() {
        return Mono.defer(() -> {
            if (!getToken().isPresent()) {
                throw new IllegalStateException("Missing token");
            }
            return gateway.getRestClient().getWebhookService()
                    .deleteWebhookWithToken(getId().asLong(), getToken().get());
        });
    }

    /**
     * Requests to edit this webhook. Requires the MANAGE_WEBHOOKS permission.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyWebhookEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(WebhookEditSpec)} or {@link #edit()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<Webhook> edit(final Consumer<? super LegacyWebhookEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyWebhookEditSpec mutatedSpec = new LegacyWebhookEditSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getWebhookService()
                            .modifyWebhook(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new Webhook(gateway, data));
    }

    /**
     * Requests to edit this webhook. Properties specifying how to edit this webhook can be set via the {@code withXxx}
     * methods of the returned {@link WebhookEditMono}. Requires the MANAGE_WEBHOOKS permission.
     *
     * @return A {@link WebhookEditMono} where, upon successful completion, emits the edited {@link Webhook}. If an
     * error is received, it is emitted through the {@code WebhookEditMono}.
     */
    public WebhookEditMono edit() {
        return WebhookEditMono.of(this);
    }

    /**
     * Requests to edit this webhook. Requires the MANAGE_WEBHOOKS permission.
     *
     * @param spec an immutable object that specifies how to edit this webhook
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> edit(WebhookEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getWebhookService()
                        .modifyWebhook(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> new Webhook(gateway, data));
    }

    /**
     * Requests to edit this webhook. Does not require the MANAGE_WEBHOOKS permission.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyWebhookEditWithTokenSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #editWithToken(WebhookEditWithTokenSpec)} or {@link #editWithToken()} which offer an
     * immutable approach to build specs
     */
    @Deprecated
    public Mono<Webhook> editWithToken(final Consumer<? super LegacyWebhookEditWithTokenSpec> spec) {
        return Mono.defer(
                () -> {
                    if (!getToken().isPresent()) {
                        throw new IllegalStateException("Can't edit webhook.");
                    }
                    LegacyWebhookEditWithTokenSpec mutatedSpec = new LegacyWebhookEditWithTokenSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getWebhookService()
                            .modifyWebhookWithToken(getId().asLong(), getToken().get(), mutatedSpec.asRequest());
                })
                .map(data -> new Webhook(gateway, data));
    }

    /**
     * Requests to edit this webhook. Properties specifying how to edit this webhook can be set via the {@code withXxx}
     * methods of the returned {@link WebhookEditWithTokenMono}. Does not require the MANAGE_WEBHOOKS permission.
     *
     * @return A {@link WebhookEditWithTokenMono} where, upon successful completion, emits the edited {@link Webhook}.
     * If an error is received, it is emitted through the {@code WebhookEditWithTokenMono}.
     */
    public WebhookEditWithTokenMono editWithToken() {
        return WebhookEditWithTokenMono.of(this);
    }

    /**
     * Requests to edit this webhook. Does not require the MANAGE_WEBHOOKS permission.
     *
     * @param spec an immutable object that specifies how to edit this webhook
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> editWithToken(WebhookEditWithTokenSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getWebhookService().modifyWebhookWithToken(getId().asLong(), getToken()
                        .orElseThrow(() -> new IllegalStateException("Can't edit webhook.")), spec.asRequest()))
                .map(data -> new Webhook(gateway, data));
    }

    /**
     * Executes this webhook without waiting for a confirmation that a message was created.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyWebhookExecuteSpec} to be operated on.
     * @return A {@link Mono} where, upon successful webhook execution, completes. If the message fails to save, an
     * error IS NOT emitted through the {@code Mono}.
     * @deprecated use {@link #execute(WebhookExecuteSpec)} or {@link #execute()} which offer an immutable approach to
     * build specs
     */
    @Deprecated
    public Mono<Void> execute(final Consumer<? super LegacyWebhookExecuteSpec> spec) {
        return execute(false, spec).then();
    }

    /**
     * Executes this webhook. Properties specifying how to execute this webhook, including whether to wait for
     * confirmation that the message was created, can be set via the {@code withXxx} methods of the returned {@link
     * WebhookExecuteMono}.
     *
     * @return A {@link WebhookExecuteMono} where, upon successful webhook execution, emits a Message if {@code
     * withWaitForMessage(true)}. If the message fails to save, an error is emitted through the {@code
     * WebhookExecuteMono} only if {@code withWaitForMessage(true)}.
     */
    public WebhookExecuteMono execute() {
        return WebhookExecuteMono.of(false,this);
    }

    /**
     * Executes this webhook without waiting for a confirmation that a message was created.
     *
     * @param spec an immutable object that specifies how to execute this webhook
     * @return A {@link Mono} where, upon successful webhook execution, completes. If the message fails to save, an
     * error IS NOT emitted through the {@code Mono}.
     */
    public Mono<Void> execute(WebhookExecuteSpec spec) {
        return execute(false, spec).then();
    }

    /**
     * Executes this webhook.
     *
     * @param wait True to specify to wait for server confirmation that the message was saved or there was an error
     *             saving the message.
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyWebhookExecuteSpec} to be operated on.
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message if {@code wait = true}. If the
     * message fails to save, an error is emitted through the {@code Mono} only if {@code wait = true}.
     * @deprecated use {@link #execute(boolean, WebhookExecuteSpec)} which offers an immutable approach to build specs
     */
    @Deprecated
    public Mono<Message> execute(boolean wait, final Consumer<? super LegacyWebhookExecuteSpec> spec) {
        return Mono.defer(
                () -> {
                    if (!getToken().isPresent()) {
                        throw new IllegalArgumentException("Can't execute webhook.");
                    }
                    LegacyWebhookExecuteSpec mutatedSpec = new LegacyWebhookExecuteSpec();
                    spec.accept(mutatedSpec);

                    if (mutatedSpec.getThreadId().isAbsent()) {
                        return gateway.getRestClient().getWebhookService()
                            .executeWebhook(getId().asLong(), getToken().get(), wait, mutatedSpec.asRequest())
                            .map(data -> new Message(gateway, data));
                    } else {
                        return gateway.getRestClient().getWebhookService()
                            .executeWebhook(getId().asLong(), getToken().get(), wait, mutatedSpec.getThreadId().get().asLong(), mutatedSpec.asRequest())
                            .map(data -> new Message(gateway, data));
                    }
                }
        );
    }

    /**
     * Executes this webhook.
     *
     * @param wait True to specify to wait for server confirmation that the message was saved or there was an error
     *             saving the message.
     * @param spec an immutable object that specifies how to execute this webhook
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message if {@code wait = true}. If the
     * message fails to save, an error is emitted through the {@code Mono} only if {@code wait = true}.
     */
    public Mono<Message> execute(boolean wait, WebhookExecuteSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> {
                    if (!getToken().isPresent()) {
                        throw new IllegalArgumentException("Can't execute webhook.");
                    }

                    if (spec.threadId().isAbsent()) {
                        return gateway.getRestClient().getWebhookService()
                            .executeWebhook(getId().asLong(), getToken().get(), wait, spec.asRequest())
                            .map(data -> new Message(gateway, data));
                    } else {
                        return gateway.getRestClient().getWebhookService()
                            .executeWebhook(getId().asLong(), getToken().get(), wait, spec.threadId().get().asLong(), spec.asRequest())
                            .map(data -> new Message(gateway, data));
                    }
                }
        );
    }

    /**
     * Executes this webhook.
     *
     * @param wait True to specify to wait for server confirmation that the message was saved or there was an error
     *             saving the message.
     * @param threadId The ID of the thread to execute the webhook in. Overrides the thread ID in the spec.
     * @param spec an immutable object that specifies how to execute this webhook
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message if {@code wait = true}. If the
     * message fails to save, an error is emitted through the {@code Mono} only if {@code wait = true}.
     */
    public Mono<Message> execute(boolean wait, Snowflake threadId, WebhookExecuteSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
            () -> {
                if (!getToken().isPresent()) {
                    throw new IllegalArgumentException("Can't execute webhook.");
                }
                return gateway.getRestClient().getWebhookService()
                    .executeWebhook(getId().asLong(), getToken().get(), wait, threadId.asLong(), spec.asRequest())
                    .map(data -> new Message(gateway, data));
            }
        );
    }

    /**
     * Executes this webhook and waits for server confirmation for the message to save.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyWebhookExecuteSpec} to be operated on.
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message. If the message fails to save,
     * an error is emitted through the {@code Mono}.
     * @deprecated use {@link #executeAndWait(WebhookExecuteSpec)} which offers an immutable approach to build specs
     */
    @Deprecated
    public Mono<Message> executeAndWait(final Consumer<? super LegacyWebhookExecuteSpec> spec) {
        return execute(true, spec);
    }

    /**
     * Executes this webhook and waits for server confirmation for the message to save.
     *
     * @param spec an immutable object that specifies how to execute this webhook
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message. If the message fails to save,
     * an error is emitted through the {@code Mono}.
     */
    public Mono<Message> executeAndWait(WebhookExecuteSpec spec) {
        return execute(true, spec);
    }

    /**
     * Executes this webhook to get a message.
     *
     * @param messageId The ID of the message to get
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message. If the message get fails, an
     * error is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessage(Snowflake messageId) {
        Objects.requireNonNull(messageId);
        return Mono.defer(
            () -> {
                if (!getToken().isPresent()) {
                    throw new IllegalArgumentException("Can't get message with this webhook.");
                }
                return gateway.getRestClient().getWebhookService()
                    .getWebhookMessage(getId().asLong(), getToken().get(), messageId.asString())
                    .map(data -> new Message(gateway, data));
            }
        );
    }

    /**
     * Executes this webhook to get a message.
     *
     * @param messageId The ID of the message to get
     * @param threadId The ID of the thread to get the message from
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message. If the message get fails, an
     * error is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessage(Snowflake messageId, Snowflake threadId) {
        Objects.requireNonNull(messageId);
        Objects.requireNonNull(threadId);
        return Mono.defer(
            () -> {
                if (!getToken().isPresent()) {
                    throw new IllegalArgumentException("Can't get message with this webhook.");
                }
                return gateway.getRestClient().getWebhookService()
                    .getWebhookMessage(getId().asLong(), getToken().get(), messageId.asString(), threadId.asLong())
                    .map(data -> new Message(gateway, data));
            }
        );
    }

    /**
     * Executes this webhook to edit a message. Properties specifying how to execute this webhook, including the ID of
     * the message being edited, can be set via the {@code withXxx} methods of the returned {@link
     * WebhookMessageEditMono}.
     *
     * @param messageId The ID of the message to edit
     * @return A {@link WebhookMessageEditMono} where, upon successful webhook execution, emits a Message. If the
     * message edit fails, an error is emitted through the {@link WebhookMessageEditMono}.
     */
    public WebhookMessageEditMono editMessage(Snowflake messageId) {
        return WebhookMessageEditMono.of(messageId,this);
    }

    /**
     * Executes this webhook to edit a message.
     *
     * @param messageId The ID of the message to edit
     * @param spec an immutable object that specifies how to edit the message
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message. If the message edit fails, an
     * error is emitted through the {@code Mono}.
     */
    public Mono<Message> editMessage(Snowflake messageId, WebhookMessageEditSpec spec) {
        Objects.requireNonNull(messageId);
        Objects.requireNonNull(spec);
        return Mono.defer(
            () -> {
                if (!getToken().isPresent()) {
                    throw new IllegalArgumentException("Can't edit message with this webhook.");
                }

                if (spec.threadId().isAbsent()) {
                    return gateway.getRestClient().getWebhookService()
                        .modifyWebhookMessage(getId().asLong(), getToken().get(), messageId.asString(), spec.asRequest())
                        .map(data -> new Message(gateway, data));
                } else {
                    return gateway.getRestClient().getWebhookService()
                        .modifyWebhookMessage(getId().asLong(), getToken().get(), messageId.asString(), spec.threadId().get().asLong(), spec.asRequest())
                        .map(data -> new Message(gateway, data));
                }
            }
        );
    }

    /**
     * Executes this webhook to edit a message.
     *
     * @param messageId The ID of the message to edit
     * @param threadId The ID of the thread to edit the message in. Overrides the thread ID in the spec.
     * @param spec an immutable object that specifies how to edit the message
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message. If the message edit fails, an
     * error is emitted through the {@code Mono}.
     */
    public Mono<Message> editMessage(Snowflake messageId, Snowflake threadId, WebhookMessageEditSpec spec) {
        Objects.requireNonNull(messageId);
        Objects.requireNonNull(spec);
        return Mono.defer(
            () -> {
                if (!getToken().isPresent()) {
                    throw new IllegalArgumentException("Can't edit message with this webhook.");
                }
                return gateway.getRestClient().getWebhookService()
                    .modifyWebhookMessage(getId().asLong(), getToken().get(), messageId.asString(), threadId.asLong(), spec.asRequest())
                    .map(data -> new Message(gateway, data));
            }
        );
    }

    /**
     * Executes this webhook to delete a message.
     *
     * @param messageId The ID of the message to delete
     * @return A {@link Mono} where, upon successful webhook execution, emits nothing; indicating the message has been
     * deleted. If the message delete fails, an error is emitted through the {@code Mono}.
     */
    public Mono<Void> deleteMessage(Snowflake messageId) {
        Objects.requireNonNull(messageId);
        return Mono.defer(
            () -> {
                if (!getToken().isPresent()) {
                    throw new IllegalArgumentException("Can't delete message with this webhook.");
                }
                return gateway.getRestClient().getWebhookService()
                    .deleteWebhookMessage(getId().asLong(), getToken().get(), messageId.asString());
            }
        );
    }

    /**
     * Executes this webhook to delete a message.
     *
     * @param messageId The ID of the message to delete
     * @param threadId The ID of the thread to delete the message from
     * @return A {@link Mono} where, upon successful webhook execution, emits nothing; indicating the message has been
     * deleted. If the message delete fails, an error is emitted through the {@code Mono}.
     */
    public Mono<Void> deleteMessage(Snowflake messageId, Snowflake threadId) {
        Objects.requireNonNull(messageId);
        return Mono.defer(
            () -> {
                if (!getToken().isPresent()) {
                    throw new IllegalArgumentException("Can't delete message with this webhook.");
                }
                return gateway.getRestClient().getWebhookService()
                    .deleteWebhookMessage(getId().asLong(), getToken().get(), messageId.asString(), threadId.asLong());
            }
        );
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "Webhook{" +
                "data=" + data +
                '}';
    }

    /**
     * Represents the various types of webhooks.
     */
    public enum Type {
        /**
         * Unknown type.
         */
        UNKNOWN(-1),

        /**
         * Incoming Webhooks can post messages to channels with a generated token.
         */
        INCOMING(1),

        /**
         * Channel Follower Webhooks are internal webhooks used with Channel Following to post new messages into
         * channels.
         */
        CHANNEL_FOLLOWER(2);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code Webhook.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
            this.value = value;
        }

        /**
         * Gets the type of webhook. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of webhook.
         */
        public static Webhook.Type of(final int value) {
            switch (value) {
                case 1:
                    return INCOMING;
                case 2:
                    return CHANNEL_FOLLOWER;
                default:
                    return UNKNOWN;
            }
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }
    }
}
