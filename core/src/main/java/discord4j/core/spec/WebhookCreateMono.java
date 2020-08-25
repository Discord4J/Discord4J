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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Webhook;
import discord4j.discordjson.json.ImmutableWebhookCreateRequest;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Mono used to create a {@link Webhook} entity.
 *
 * @see <a href="https://discord.com/developers/docs/resources/webhook#create-webhook">Create Webhook</a>
 */
public class WebhookCreateMono extends AuditableRequest<Webhook, ImmutableWebhookCreateRequest.Builder, WebhookCreateMono> {

    private final GatewayDiscordClient gateway;
    private final long channelId;

    public WebhookCreateMono(Supplier<ImmutableWebhookCreateRequest.Builder> requestBuilder, @Nullable String reason,
                             GatewayDiscordClient gateway, long channelId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.channelId = channelId;
    }

    public WebhookCreateMono(GatewayDiscordClient gateway, long channelId) {
        this(ImmutableWebhookCreateRequest::builder, null, gateway, channelId);
    }

    @Override
    public WebhookCreateMono withReason(String reason) {
        return new WebhookCreateMono(requestBuilder, reason, gateway, channelId);
    }

    @Override
    WebhookCreateMono withBuilder(UnaryOperator<ImmutableWebhookCreateRequest.Builder> f) {
        return new WebhookCreateMono(apply(f), reason, gateway, channelId);
    }

    /**
     * Sets the name of the created {@link Webhook}.
     *
     * @param name The webhook name.
     * @return This mono.
     */
    public WebhookCreateMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    /**
     * Sets the image of the created {@link Webhook}.
     *
     * @param avatar The webhook image.
     * @return This mono.
     */
    public WebhookCreateMono withAvatar(Image avatar) {
        return withBuilder(it -> it.avatar(avatar.getDataUri()));
    }

    @Override
    Mono<Webhook> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getWebhookService()
            .createWebhook(channelId, requestBuilder.get().build(), reason))
            .map(data -> new Webhook(gateway, data));
    }
}
