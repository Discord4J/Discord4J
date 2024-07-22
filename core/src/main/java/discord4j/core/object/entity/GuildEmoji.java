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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.GuildEmojiEditMono;
import discord4j.core.spec.GuildEmojiEditSpec;
import discord4j.core.spec.legacy.LegacyGuildEmojiEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.OrderUtil;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.UserData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A Discord guild emoji.
 * <p>
 * <a href="https://discord.com/developers/docs/resources/emoji#emoji-resource">Emoji Resource</a>
 */
public final class GuildEmoji extends Emoji {

    /** The ID of the guild this emoji is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code GuildEmoji} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this emoji is associated to.
     */
    public GuildEmoji(final GatewayDiscordClient gateway, final EmojiData data, final long guildId) {
        super(gateway, data);
        this.guildId = guildId;
    }

    /**
     * Gets the IDs of the roles this emoji is whitelisted to.
     *
     * @return The IDs of the roles this emoji is whitelisted to.
     */
    public Set<Snowflake> getRoleIds() {
        return data.roles().toOptional()
                .map(roles -> roles.stream()
                        .map(Snowflake::of)
                        .collect(Collectors.toSet()))
                .orElseThrow(IllegalStateException::new); // this should be safe for guild emojis
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
    @Override
    public Mono<User> getUser() {
        UserData user = data.user().toOptional()
                .orElseThrow(IllegalStateException::new); // this should be safe for guild emojis

        return gateway.getRestClient().getEmojiService()
                .getGuildEmoji(getGuildId().asLong(), getId().asLong())
                .map(data -> new User(gateway, user));
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
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyGuildEmojiEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(GuildEmojiEditSpec)} or {@link #edit()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<GuildEmoji> edit(final Consumer<? super LegacyGuildEmojiEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyGuildEmojiEditSpec mutatedSpec = new LegacyGuildEmojiEditSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getEmojiService()
                            .modifyGuildEmoji(getGuildId().asLong(), getId().asLong(), mutatedSpec.asRequest(),
                                    mutatedSpec.getReason());
                })
                .map(data -> new GuildEmoji(gateway, data, getGuildId().asLong()));
    }

    /**
     * Requests to edit this guild emoji. Properties specifying how to edit this emoji can be set via the {@code
     * withXxx} methods of the returned {@link GuildEmojiEditMono}.
     *
     * @return A {@link GuildEmojiEditMono} where, upon successful completion, emits the edited {@link GuildEmoji}. If
     * an error is received, it is emitted through the {@code GuildEmojiEditMono}.
     */
    public GuildEmojiEditMono edit() {
        return GuildEmojiEditMono.of(this);
    }

    /**
     * Requests to edit this guild emoji.
     *
     * @param spec an immutable object that specifies how to edit this emoji
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> edit(GuildEmojiEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getEmojiService()
                        .modifyGuildEmoji(getGuildId().asLong(), getId().asLong(), spec.asRequest(),
                                spec.reason()))
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
