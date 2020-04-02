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
package discord4j.core.object.entity;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.rest.util.Image;
import discord4j.rest.util.Snowflake;
import discord4j.core.spec.GuildEmojiEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import discord4j.core.util.OrderUtil;
import discord4j.discordjson.json.EmojiData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static discord4j.rest.util.Image.Format.GIF;
import static discord4j.rest.util.Image.Format.PNG;

/**
 * A Discord guild emoji.
 * <p>
 * <a href="https://discordapp.com/developers/docs/resources/emoji#emoji-resource">Emoji Resource</a>
 */
// TODO FIXME so many gets
public final class GuildEmoji implements Entity {

    /** The path for {@code GuildEmoji} image URLs. */
    private static final String EMOJI_IMAGE_PATH = "emojis/%s";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final EmojiData data;

    /** The ID of the guild this emoji is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code GuildEmoji} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this emoji is associated to.
     */
    public GuildEmoji(final GatewayDiscordClient gateway, final EmojiData data, final long guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id().get()); // this is safe for guild emojis
    }

    /**
     * Gets the emoji name.
     *
     * @return The emoji name.
     */
    public String getName() {
        return data.name().get();
    }

    /**
     * Gets the IDs of the roles this emoji is whitelisted to.
     *
     * @return The IDs of the roles this emoji is whitelisted to.
     */
    public Set<Snowflake> getRoleIds() {
        return data.roles().get().stream()
                .map(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the roles this emoji is whitelisted to.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use {@link OrderUtil#orderRoles(Flux)}
     * to consistently order roles.
     *
     * @return A {@link Flux} that continually emits the {@link Role roles} this emoji is whitelisted for. if an error
     * is received, it is emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles() {
        return Flux.fromIterable(getRoleIds()).flatMap(id -> gateway.getRoleById(getGuildId(), id));
    }

    /**
     * Requests to retrieve the roles this emoji is whitelisted to, using the given retrieval strategy.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use {@link OrderUtil#orderRoles(Flux)}
     * to consistently order roles.
     *
     * @param retrievalStrategy the strategy to use to get the roles
     * @return A {@link Flux} that continually emits the {@link Role roles} this emoji is whitelisted for. if an error
     * is received, it is emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles(EntityRetrievalStrategy retrievalStrategy) {
        return Flux.fromIterable(getRoleIds())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getRoleById(getGuildId(), id));
    }

    /**
     * Requests to retrieve the user that created this emoji. This method will always hit the REST API.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User user} that created this emoji. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return gateway.getRestClient().getEmojiService()
                .getGuildEmoji(getGuildId().asLong(), getId().asLong())
                .map(data -> new User(gateway, data.user().get()));
    }

    /**
     * Gets whether this emoji must be wrapped in colons.
     *
     * @return {@code true} if this emoji must be wrapped in colons, {@code false} otherwise.
     */
    public boolean requiresColons() {
        return data.requireColons().get();
    }

    /**
     * Gets whether this emoji is managed.
     *
     * @return {@code true} if this emoji is managed, {@code false} otherwise.
     */
    public boolean isManaged() {
        return data.managed().get();
    }

    /**
     * Gets whether this emoji is animated.
     *
     * @return {@code true} if this emoji is animated, {@code false} otherwise.
     */
    public boolean isAnimated() {
        return data.animated().get();
    }

    /**
     * Gets the ID of the guild this emoji is associated to.
     *
     * @return The ID of the guild this emoji is associated to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the guild this emoji is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this emoji is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return gateway.getGuildById(getGuildId());
    }

    /**
     * Requests to retrieve the guild this emoji is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this emoji is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    /**
     * Requests to edit this guild emoji.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildEmojiEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> edit(final Consumer<? super GuildEmojiEditSpec> spec) {
        final GuildEmojiEditSpec mutatedSpec = new GuildEmojiEditSpec();
        spec.accept(mutatedSpec);

        return gateway.getRestClient().getEmojiService()
                .modifyGuildEmoji(getGuildId().asLong(), getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                .map(data -> new GuildEmoji(gateway, data, getGuildId().asLong()));
    }

    /**
     * Requests to delete this emoji.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the emoji has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this emoji while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the emoji has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return gateway.getRestClient().getEmojiService()
                .deleteGuildEmoji(getGuildId().asLong(), getId().asLong(), reason);
    }

    /**
     * Gets the URL for this guild emoji.
     *
     * @return The URL for this guild emoji.
     */
    public String getImageUrl() {
        final String path = String.format(EMOJI_IMAGE_PATH, getId().asString());
        return isAnimated() ? ImageUtil.getUrl(path, GIF) : ImageUtil.getUrl(path, PNG);
    }

    /**
     * Gets the image for this guild emoji.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image image} of the emoji. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getImage() {
        return Image.ofUrl(getImageUrl());
    }

    /**
     * Gets the formatted version of this emoji (i.e., to display in the client).
     *
     * @return The formatted version of this emoji (i.e., to display in the client).
     */
    public String asFormat() {
        return '<' + (isAnimated() ? "a" : "") + ':' + getName() + ':' + getId().asString() + '>';
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "GuildEmoji{" +
                "data=" + data +
                ", guildId=" + guildId +
                '}';
    }
}
