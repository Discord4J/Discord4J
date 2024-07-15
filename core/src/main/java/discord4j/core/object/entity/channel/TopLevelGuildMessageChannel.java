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
package discord4j.core.object.entity.channel;

import discord4j.core.object.entity.Webhook;
import discord4j.core.spec.WebhookCreateMono;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.core.spec.legacy.LegacyWebhookCreateSpec;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** A Discord message channel in a guild that isn't a thread. */
public interface TopLevelGuildMessageChannel extends CategorizableChannel, GuildMessageChannel {

    /**
     * Gets the channel topic, if present.
     *
     * @return The channel topic, if present.
     */
    default Optional<String> getTopic() {
        return Possible.flatOpt(getData().topic());
    }

    /**
     * Requests to create a webhook.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyWebhookCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createWebhook(WebhookCreateSpec)} or {@link #createWebhook(String)} which offer an
     * immutable approach to build specs
     */
    default Mono<Webhook> createWebhook(final Consumer<? super LegacyWebhookCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyWebhookCreateSpec mutatedSpec = new LegacyWebhookCreateSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getWebhookService()
                            .createWebhook(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new Webhook(getClient(), data));
    }

    /**
     * Requests to create a webhook. Properties specifying how to create the webhook can be set via the {@code withXxx}
     * methods of the returned {@link WebhookCreateMono}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    default WebhookCreateMono createWebhook(String name) {
        return WebhookCreateMono.of(name, this);
    }

    /**
     * Requests to create a webhook.
     *
     * @param spec an immutable object that specifies how to create the webhook
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    default Mono<Webhook> createWebhook(WebhookCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> getClient().getRestClient().getWebhookService()
                        .createWebhook(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> new Webhook(getClient(), data));
    }

    /**
     * Requests to retrieve the webhooks of the channel.
     *
     * @return A {@link Flux} that continually emits the {@link Webhook webhooks} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    default Flux<Webhook> getWebhooks() {
        return getClient().getRestClient().getWebhookService()
                .getChannelWebhooks(getId().asLong())
                .map(data -> new Webhook(getClient(), data));
    }

}
