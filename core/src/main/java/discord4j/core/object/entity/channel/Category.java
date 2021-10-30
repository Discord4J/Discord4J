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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.CategoryEditMono;
import discord4j.core.spec.CategoryEditSpec;
import discord4j.core.spec.legacy.LegacyCategoryEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;

/** A Discord category. */
public final class Category extends BaseTopLevelGuildChannel {

    /**
     * Constructs an {@code Category} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Category(final GatewayDiscordClient gateway, final ChannelData data) {
        super(gateway, data);
    }

    /**
     * Requests to retrieve the channels residing in this category.
     *
     * @return A {@link Flux} that continually emits the {@link CategorizableChannel channels} residing in this category. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<CategorizableChannel> getChannels() {
        return getGuild().flatMapMany(Guild::getChannels)
                .ofType(CategorizableChannel.class)
                .filter(channel -> channel.getCategoryId().map(getId()::equals).orElse(false));
    }

    /**
     * Requests to retrieve the channels residing in this category, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the channels
     * @return A {@link Flux} that continually emits the {@link CategorizableChannel channels} residing in this category. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<CategorizableChannel> getChannels(EntityRetrievalStrategy retrievalStrategy) {
        return getGuild(retrievalStrategy)
                .flatMapMany(guild -> guild.getChannels(retrievalStrategy))
                .ofType(CategorizableChannel.class)
                .filter(channel -> channel.getCategoryId().map(getId()::equals).orElse(false));
    }

    /**
     * Requests to edit this category.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyCategoryEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Category}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(CategoryEditSpec)}  or {@link #edit()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<Category> edit(final Consumer<? super LegacyCategoryEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyCategoryEditSpec mutatedSpec = new LegacyCategoryEditSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getChannelService()
                            .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(Category.class);
    }

    /**
     * Requests to edit this category. Properties specifying how to edit this category can be set via the {@code
     * withXxx} methods of the returned {@link CategoryEditMono}.
     *
     * @return A {@link CategoryEditMono} where, upon successful completion, emits the edited {@link Category}. If an
     * error is received, it is emitted through the {@code CategoryEditMono}.
     */
    public CategoryEditMono edit() {
        return CategoryEditMono.of(this);
    }

    /**
     * Requests to edit this category.
     *
     * @param spec an immutable object specifying how to edit this category
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Category}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Category> edit(CategoryEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> getClient().getRestClient().getChannelService()
                        .modifyChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(Category.class);
    }

    @Override
    public String toString() {
        return "Category{} " + super.toString();
    }
}
