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

/**
 * A Discord message channel in a guild that isn't a thread.
 */
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
