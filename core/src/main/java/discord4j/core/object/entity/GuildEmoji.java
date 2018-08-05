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

import discord4j.common.json.GuildEmojiResponse;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.data.stored.GuildEmojiBean;
import discord4j.core.object.data.stored.UserBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.GuildEmojiEditSpec;
import discord4j.core.util.ImageUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static discord4j.core.object.util.Image.Format.GIF;
import static discord4j.core.object.util.Image.Format.PNG;

/**
 * A Discord guild emoji.
 * <p>
 * <a href="https://discordapp.com/developers/docs/resources/emoji#emoji-resource">Emoji Resource</a>
 */
public final class GuildEmoji implements Entity {

    /** The path for {@code GuildEmoji} image URLs. */
    private static final String EMOJI_IMAGE_PATH = "emojis/%s";

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
        return serviceMediator.getRestClient().getEmojiService()
                .getGuildEmoji(getGuildId().asLong(), getId().asLong())
                .map(GuildEmojiResponse::getUser)
                .map(UserBean::new)
                .map(bean -> new User(serviceMediator, bean));
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

    /**
     * Requests to edit this guild emoji.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildEmojiEditSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #edit(GuildEmojiEditSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> edit(final Consumer<GuildEmojiEditSpec> spec) {
        final GuildEmojiEditSpec mutatedSpec = new GuildEmojiEditSpec();
        spec.accept(mutatedSpec);
        return edit(mutatedSpec);
    }

    /**
     * Requests to edit this guild emoji.
     *
     * @param spec A configured {@link GuildEmojiEditSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> edit(final GuildEmojiEditSpec spec) {
        return serviceMediator.getRestClient().getEmojiService()
                .modifyGuildEmoji(getGuildId().asLong(), getId().asLong(), spec.asRequest())
                .map(GuildEmojiBean::new)
                .map(bean -> new GuildEmoji(serviceMediator, bean, getGuildId().asLong()));
    }

    /**
     * Requests to delete this emoji.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the emoji has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return serviceMediator.getRestClient().getEmojiService()
                .deleteGuildEmoji(getGuildId().asLong(), getId().asLong());
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
}
