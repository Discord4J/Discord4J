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

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.bean.GuildEmojiBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Discord guild emoji.
 * <p>
 * <a href="https://discordapp.com/developers/docs/resources/emoji#emoji-resource">Emoji Resource</a>
 */
public final class GuildEmoji implements Entity {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final GuildEmojiBean data;

    /** The ID of the guild this emoji is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code GuildEmoji} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this emoji is associated to.
     */
    public GuildEmoji(final ServiceMediator serviceMediator, final GuildEmojiBean data, final long guildId) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    /**
     * Gets the emoji name.
     *
     * @return The emoji name.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the IDs of the roles this emoji is whitelisted to.
     *
     * @return The IDs of the roles this emoji is whitelisted to.
     */
    public Set<Snowflake> getRoleIds() {
        return Arrays.stream(data.getRoles())
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the roles this emoji is whitelisted to.
     *
     * @return A {@link Flux} that continually emits the {@link Role roles} this emoji is whitelisted for. if an error
     * is received, it is emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles() {
        return Flux.fromIterable(getRoleIds()).flatMap(id -> getClient().getRoleById(getGuildId(), id));
    }

    /**
     * Requests to retrieve the user that created this emoji.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User user} that created this emoji. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets whether this emoji must be wrapped in colons.
     *
     * @return {@code true} if this emoji must be wrapped in colons, {@code false} otherwise.
     */
    public boolean requiresColons() {
        return data.isRequireColons();
    }

    /**
     * Gets whether this emoji is managed.
     *
     * @return {@code true} if this emoji is managed, {@code false} otherwise.
     */
    public boolean isManaged() {
        return data.isManaged();
    }

    /**
     * Gets whether this emoji is animated.
     *
     * @return {@code true} if this emoji is animated, {@code false} otherwise.
     */
    public boolean isAnimated() {
        return data.isAnimated();
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
        return getClient().getGuildById(getGuildId());
    }
}
