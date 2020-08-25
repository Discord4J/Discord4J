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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.WebhookEditMono;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.WebhookData;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

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
     * Constructs a {@code Webhook} with an associated ServiceMediator and Discord data.
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
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildMessageChannel channel} this webhook is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildMessageChannel> getChannel() {
        return gateway.getChannelById(getChannelId()).cast(GuildMessageChannel.class);
    }

    /**
     * Requests to retrieve the channel this webhook is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildMessageChannel channel} this webhook is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildMessageChannel> getChannel(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy)
                .getChannelById(getChannelId())
                .cast(GuildMessageChannel.class);
    }

    /**
     * Gets the ID of the user this webhook was created by.
     *
     * @return The ID of the user this webhook was created by.
     */
    public Snowflake getCreatorId() {
        return Snowflake.of(data.user().get().id()); // TODO FIXME: really Possible?
    }

    /**
     * Requests to retrieve the user this webhook was created by, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User user} this webhook was created
     * by, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getCreator() {
        return gateway.getUserById(getCreatorId());
    }

    /**
     * Requests to retrieve the user this webhook was created by, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the creator
     * @return A {@link Mono} where, upon successful completion, emits the {@link User user} this webhook was created
     * by, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getCreator(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getUserById(getCreatorId());
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
     * Gets the secure token of this webhook.
     *
     * @return The secure token of this webhook.
     */
    public String getToken() {
        return data.token().get(); // TODO FIXME: really Possible?
    }

    /**
     * Requests to delete this webhook.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the webhook has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this webhook while optionally specifying a reason.
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
     * Requests to edit this webhook.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public WebhookEditMono edit() {
        return new WebhookEditMono(getClient(), getId().asLong());
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
