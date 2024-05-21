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
import discord4j.core.object.ExtendedInvite;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.InviteCreateMono;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.core.spec.legacy.LegacyInviteCreateSpec;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

/** A Discord channel which can be categorized into a {@link Category}. These channels can also have invites. */
public interface CategorizableChannel extends TopLevelGuildChannel {

    /**
     * Gets the amount of seconds a user has to wait before sending another message (0-21600).
     * <p>
     * Bots, as well as users with the permission {@code manage_messages} or {@code manage_channel}, are unaffected.
     *
     * @return The amount of seconds a user has to wait before sending another message (0-21600).
     */
    default int getRateLimitPerUser() {
        return getData().rateLimitPerUser().toOptional()
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the ID of the category for this channel, if present.
     *
     * @return The ID of the category for this channel, if present.
     */
    default Optional<Snowflake> getCategoryId() {
        return Possible.flatOpt(getData().parentId())
                .map(Snowflake::of);
    }

    /**
     * Requests to retrieve the category for this channel, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Category category} this channel, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Category> getCategory() {
        return Mono.justOrEmpty(getCategoryId())
                .flatMap(getClient()::getChannelById)
                .cast(Category.class);
    }

    /**
     * Requests to retrieve the category for this channel, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the category
     * @return A {@link Mono} where, upon successful completion, emits the {@link Category category} this channel, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Category> getCategory(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getCategoryId())
                .flatMap(id -> getClient().withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(Category.class);
    }

    /**
     * Requests to create an invite.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyInviteCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ExtendedInvite}. If an error
     * is received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createInvite(InviteCreateSpec)} or {@link #createInvite()} which offer an immutable
     * approach to build specs
     */
    @Deprecated
    default Mono<ExtendedInvite> createInvite(final Consumer<? super LegacyInviteCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyInviteCreateSpec mutatedSpec = new LegacyInviteCreateSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getChannelService()
                            .createChannelInvite(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new ExtendedInvite(getClient(), data));
    }

    /**
     * Requests to create an invite. Properties specifying how to create the invite can be set via the {@code withXxx }
     * methods of the returned {@link InviteCreateMono}.
     *
     * @return A {@link InviteCreateMono} where, upon successful completion, emits the created {@link ExtendedInvite}.
     * If an error is received, it is emitted through the {@code InviteCreateMono}.
     */
    default InviteCreateMono createInvite() {
        return InviteCreateMono.of(this);
    }

    /**
     * Requests to create an invite.
     *
     * @param spec an immutable object that specifies how to create the invite
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ExtendedInvite}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    default Mono<ExtendedInvite> createInvite(InviteCreateSpec spec) {
        return Mono.defer(
                () -> getClient().getRestClient().getChannelService()
                        .createChannelInvite(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> new ExtendedInvite(getClient(), data));
    }

    /**
     * Requests to retrieve this channel's invites.
     *
     * @return A {@link Flux} that continually emits this channel's {@link ExtendedInvite invites}. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    default Flux<ExtendedInvite> getInvites() {
        return getClient().getRestClient().getChannelService()
                .getChannelInvites(getId().asLong())
                .map(data -> new ExtendedInvite(getClient(), data));
    }
}
