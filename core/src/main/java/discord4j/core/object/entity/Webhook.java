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
import discord4j.core.spec.WebhookEditSpec;
import discord4j.core.spec.WebhookEditWithTokenSpec;
import discord4j.core.spec.WebhookExecuteSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.WebhookData;
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

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final WebhookData data;

    /**
     * Constructs a {@code Webhook} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
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

    public enum Type {
        UNKNOWN(-1),
        /**
         * Incoming Webhooks can post messages to channels with a generated token
         */
        INCOMING(1),
        /**
         * Channel Follower Webhooks are internal webhooks used with Channel Following
         * to post new messages into channels
         */
        CHANNEL_FOLLOWER(2);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Type fromValue(int value) {
            switch (value) {
                case 1:
                    return Type.INCOMING;
                case 2:
                    return Type.CHANNEL_FOLLOWER;
                default:
                    return Type.UNKNOWN;
            }
        }
    }

    /**
     * Gets the type of this webhook.
     *
     * @return The type of this webhook.
     */
    public Type getType() {
        return Type.fromValue(data.type());
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
     * Gets the ID of the guild this webhook is associated to.
     *
     * @return The ID of the guild this webhook is associated to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(data.guildId().get()); // TODO FIXME: really Possible?
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
        return Snowflake.of(data.channelId());
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
     * Requests to retrieve the user this webhook was created by, if present.
     * Returns no creator if the webhook was retrieved using a token.
     *
     * @return An {@link Optional} with the {@link User user} this webhook was created
     * by, if present.
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
     * @param spec A {@link Consumer} that provides a "blank" {@link WebhookEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> edit(final Consumer<? super WebhookEditSpec> spec) {
        return Mono.defer(
                () -> {
                    WebhookEditSpec mutatedSpec = new WebhookEditSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getWebhookService()
                            .modifyWebhook(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new Webhook(gateway, data));
    }

    /**
     * Requests to edit this webhook.
     * Does not require the MANAGE_WEBHOOKS permission.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link WebhookEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> editWithToken(final Consumer<? super WebhookEditWithTokenSpec> spec) {
        return Mono.defer(
                () -> {
                    if (!getToken().isPresent()) {
                        throw new IllegalStateException("Can't edit webhook.");
                    }
                    WebhookEditWithTokenSpec mutatedSpec = new WebhookEditWithTokenSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getWebhookService()
                            .modifyWebhookWithToken(getId().asLong(), getToken().get(), mutatedSpec.asRequest());
                })
                .map(data -> new Webhook(gateway, data));
    }

    /**
     * Executes this webhook without waiting for a confirmation that a message was created.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link WebhookExecuteSpec} to be operated on.
     * @return A {@link Mono} where, upon successful webhook execution, completes. If the message fails to save,
     * an error IS NOT emitted through the {@code Mono}.
     */
    public Mono<Void> execute(final Consumer<? super WebhookExecuteSpec> spec) {
        return execute(false, spec).cast(Void.class);
    }

    /**
     * Executes this webhook.
     *
     * @param wait True to specify to wait for server confirmation that the message was saved or
     * there was an error saving the message.
     * @param spec A {@link java.util.function.Consumer} that provides a "blank"
     * {@link discord4j.core.spec.WebhookExecuteSpec} to be operated on.
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message if {@code wait = true}.
     * If the message fails to save, an error is emitted through the {@code Mono} only if {@code wait = true}.
     */
    public Mono<Message> execute(boolean wait, final Consumer<? super WebhookExecuteSpec> spec) {
        return Mono.defer(
                () -> {
                    if (!getToken().isPresent()) {
                        throw new IllegalArgumentException("Can't execute webhook.");
                    }
                    WebhookExecuteSpec mutatedSpec = new WebhookExecuteSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getWebhookService()
                            .executeWebhook(getId().asLong(), getToken().get(), wait, mutatedSpec.asRequest())
                            .map(data -> new Message(gateway, data));
                }
        );
    }

    /**
     * Executes this webhook and waits for server confirmation for the message to save.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link WebhookExecuteSpec} to be operated on.
     * @return A {@link Mono} where, upon successful webhook execution, emits a Message.
     * If the message fails to save, an error is emitted through the {@code Mono}.
     */
    public Mono<Message> executeAndWait(final Consumer<? super WebhookExecuteSpec> spec) {
        return execute(true, spec);
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
}
