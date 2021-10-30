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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

/** A Discord message channel in a guild that isn't a thread. */
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
