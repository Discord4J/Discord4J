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
package discord4j.core.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Webhook;
import discord4j.core.object.entity.channel.NewsChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.FollowedChannelData;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A news channel that has been followed.
 */
public class FollowedChannel implements DiscordObject {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final FollowedChannelData data;

    public FollowedChannel(final GatewayDiscordClient gateway, final FollowedChannelData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public final GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the channel that has been followed.
     *
     * @return The data of the channel that has been followed.
     */
    public FollowedChannelData getData() {
        return data;
    }

    /**
     * Returns the ID of the news channel that has been followed.
     *
     * @return the news channel ID
     */
    public Snowflake getNewsChannelId() {
        return Snowflake.of(data.channelId());
    }

    /**
     * Requests to retrieve the news channel that has been followed.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link NewsChannel news channel} that has
     * been followed. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<NewsChannel> getNewsChannel() {
        return gateway.getChannelById(Snowflake.of(data.channelId()))
                .cast(NewsChannel.class);
    }

    /**
     * Requests to retrieve the news channel that has been followed, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the news channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link NewsChannel news channel} that has
     * been followed. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<NewsChannel> getNewsChannel(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy)
                .getChannelById(Snowflake.of(data.channelId()))
                .cast(NewsChannel.class);
    }

    /**
     * Returns the ID of the webhook created as the result of following the news channel.
     *
     * @return the webhook ID
     */
    public Snowflake getWebhookId() {
        return Snowflake.of(data.webhookId());
    }

    /**
     * Requests to retrieve the webhook that has been created when following the news channel. Requires
     * 'MANAGE_WEBHOOKS' permission.
     *
     * <p>
     * Note that the returned webhook cannot be executed, but can be deleted.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook webhook} that has been created
     * when following the news channel. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> getWebhook() {
        return gateway.getWebhookById(Snowflake.of(data.webhookId()));
    }
}
