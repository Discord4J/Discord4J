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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.core.spec.legacy.LegacyInviteCreateSpec;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

class BaseCategorizableChannel extends BaseGuildChannel implements CategorizableChannel {

    BaseCategorizableChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    @Override
    public Optional<Snowflake> getCategoryId() {
        return Possible.flatOpt(getData().parentId())
            .map(Snowflake::of);
    }

    @Override
    public Mono<Category> getCategory() {
        return Mono.justOrEmpty(getCategoryId()).flatMap(getClient()::getChannelById).cast(Category.class);
    }

    @Override
    public Mono<Category> getCategory(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getCategoryId())
                .flatMap(id -> getClient().withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(Category.class);
    }

    @Override
    public Mono<ExtendedInvite> createInvite(Consumer<? super LegacyInviteCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyInviteCreateSpec mutatedSpec = new LegacyInviteCreateSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getChannelService()
                            .createChannelInvite(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new ExtendedInvite(getClient(), data));
    }

    @Override
    public Mono<ExtendedInvite> createInvite(InviteCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> getClient().getRestClient().getChannelService()
                        .createChannelInvite(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> new ExtendedInvite(getClient(), data));
    }

    @Override
    public Flux<ExtendedInvite> getInvites() {
        return getClient().getRestClient().getChannelService()
                .getChannelInvites(getId().asLong())
                .map(data -> new ExtendedInvite(getClient(), data));
    }
}
