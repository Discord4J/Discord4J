package discord4j.core.object.entity.channel;

import discord4j.core.object.entity.Webhook;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.core.spec.legacy.LegacyWebhookCreateSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

// TODO naming
public interface TextOrNewsChannel extends CategorizableChannel, GuildMessageChannel {

    /**
     * Gets the channel topic, if present.
     *
     * @return The channel topic, if present.
     */
    @Override
    Optional<String> getTopic();

    /**
     * Requests to create a webhook.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link WebhookCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Webhook}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    @Override
    Mono<Webhook> createWebhook(final Consumer<? super LegacyWebhookCreateSpec> spec);

    /**
     * Requests to retrieve the webhooks of the channel.
     *
     * @return A {@link Flux} that continually emits the {@link Webhook webhooks} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    @Override
    Flux<Webhook> getWebhooks();
}
