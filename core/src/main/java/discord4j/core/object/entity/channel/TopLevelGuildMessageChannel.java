package discord4j.core.object.entity.channel;

import discord4j.core.object.entity.Webhook;
import discord4j.core.spec.WebhookCreateMono;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.core.spec.legacy.LegacyWebhookCreateSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A Discord message channel in a guild that isn't a thread.
 */
public interface TopLevelGuildMessageChannel extends CategorizableChannel, GuildMessageChannel {

    @Override
    Optional<String> getTopic();

    @Override
    Mono<Webhook> createWebhook(final Consumer<? super LegacyWebhookCreateSpec> spec);

    @Override
    default WebhookCreateMono createWebhook(String name) {
        return GuildMessageChannel.super.createWebhook(name);
    }

    @Override
    Mono<Webhook> createWebhook(WebhookCreateSpec spec);

    @Override
    Flux<Webhook> getWebhooks();
}
