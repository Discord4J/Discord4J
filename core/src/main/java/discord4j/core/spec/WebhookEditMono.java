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
package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Webhook;
import discord4j.discordjson.json.ImmutableWebhookModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Mono to modify a {@link Webhook} entity.
 *
 * @see <a href="https://discord.com/developers/docs/resources/webhook#modify-webhook">Modify Webhook</a>
 */
public class WebhookEditMono extends AuditableRequest<Webhook, ImmutableWebhookModifyRequest.Builder, WebhookEditMono> {

    private final GatewayDiscordClient gateway;
    private final long webhookId;

    public WebhookEditMono(Supplier<ImmutableWebhookModifyRequest.Builder> requestBuilder, @Nullable String reason,
                           GatewayDiscordClient gateway, long webhookId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.webhookId = webhookId;
    }

    public WebhookEditMono(GatewayDiscordClient gateway, long webhookId) {
        this(ImmutableWebhookModifyRequest::builder, null, gateway, webhookId);
    }

    @Override
    public WebhookEditMono withReason(String reason) {
        return new WebhookEditMono(requestBuilder, reason, gateway, webhookId);
    }

    @Override
    WebhookEditMono withBuilder(UnaryOperator<ImmutableWebhookModifyRequest.Builder> f) {
        return new WebhookEditMono(apply(f), reason, gateway, webhookId);
    }

    /**
     * Sets the name of the modified {@link Webhook}.
     *
     * @param name The webhook name.
     * @return This mono.
     */
    public WebhookEditMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    /**
     * Sets the image of the modified {@link Webhook}.
     *
     * @param avatar The webhook image.
     * @return This mono.
     */
    public WebhookEditMono withAvatar(@Nullable Image avatar) {
        return withBuilder(it -> it.avatar(avatar == null ? Possible.absent() : Possible.of(avatar.getDataUri())));
    }

    /**
     * Sets the channel ID of the modified {@link Webhook}.
     *
     * @param channelId the new channel id this webhook should be moved to
     * @return This mono.
     */
    public WebhookEditMono withChannel(@Nullable Snowflake channelId) {
        return withBuilder(it -> it.channelId(channelId == null ? Possible.absent() : Possible.of(channelId.asString())));
    }

    @Override
    Mono<Webhook> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getWebhookService()
            .modifyWebhook(webhookId, requestBuilder.get().build(), reason))
            .map(data -> new Webhook(gateway, data));
    }
}
