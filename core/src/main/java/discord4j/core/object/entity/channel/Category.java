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

import discord4j.discordjson.json.ChannelData;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.CategoryEditSpec;
import discord4j.core.util.EntityUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/** A Discord category. */
public final class Category extends BaseGuildChannel {

    /**
     * Constructs an {@code Category} with an associated ServiceMediator and Discord data.
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
     * @param spec A {@link Consumer} that provides a "blank" {@link CategoryEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Category}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Category> edit(final Consumer<? super CategoryEditSpec> spec) {
        final CategoryEditSpec mutatedSpec = new CategoryEditSpec();
        spec.accept(mutatedSpec);

        return getClient().getRestClient().getChannelService()
                .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(Category.class);
    }

    @Override
    public String toString() {
        return "Category{} " + super.toString();
    }
}
