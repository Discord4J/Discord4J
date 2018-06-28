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
import discord4j.core.object.data.stored.UserBean;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import discord4j.rest.json.request.DMCreateRequest;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

import static discord4j.core.object.util.Image.Format.*;

/**
 * A Discord user.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/user">Users Resource</a>
 */
public class User implements Entity {

    /** The path for default user avatar image URLs. */
    private static final String DEFAULT_IMAGE_PATH = "embed/avatars/%d";

    /** The path for user avatar image URLs. */
    private static final String AVATAR_IMAGE_PATH = "avatars/%s/%s";

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final UserBean data;

    /**
     * Constructs an {@code User} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public User(final ServiceMediator serviceMediator, final UserBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public final DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the user's username, not unique across the platform.
     *
     * @return The user's username, not unique across the platform.
     */
    public final String getUsername() {
        return data.getUsername();
    }

    /**
     * Gets the user's 4-digit discord-tag.
     *
     * @return The user's 4-digit discord-tag.
     */
    public final String getDiscriminator() {
        return data.getDiscriminator();
    }

    /**
     * Gets if the user's avatar is animated.
     *
     * @return {@code true} if the user's avatar is animated, {@code false} otherwise.
     */
    public final boolean hasAnimatedAvatar() {
        final String avatar = data.getAvatar();
        return (avatar != null) && avatar.startsWith("a_");
    }

    /**
     * Gets the user's avatar URL, if present and in a supported format.
     *
     * @param format The format for the URL. Supported format types are {@link Image.Format#GIF GIF} if
     * {@link #hasAnimatedAvatar() animated}, otherwise {@link Image.Format#PNG PNG} or {@link Image.Format#JPEG JPEG}.
     * @return The user's avatar URL, if present and in a supported format.
     */
    public final Optional<String> getAvatarUrl(final Image.Format format) {
        final boolean animated = hasAnimatedAvatar();
        return Optional.ofNullable(data.getAvatar())
                .filter(ignored -> !animated || (format == GIF))
                .filter(ignored -> (!animated && ((format == PNG) || (format == JPEG))) || animated)
                .map(avatar -> ImageUtil.getUrl(String.format(AVATAR_IMAGE_PATH, getId().asString(), avatar), format));
    }

    /**
     * Gets the default avatar URL for this user.
     *
     * @return The default avatar URL for this user.
     */
    public final String getDefaultAvatarUrl() {
        return ImageUtil.getUrl(String.format(DEFAULT_IMAGE_PATH, Integer.parseInt(getDiscriminator()) % 5), PNG);
    }

    /**
     * Gets whether the user is a bot.
     *
     * @return {@code true} if this user is a bot, {@code false} otherwise.
     */
    public boolean isBot() {
        return data.isBot();
    }

    /**
     * Gets the <i>raw</i> mention. This is the format utilized to directly mention another user (assuming the user
     * exists in context of the mention).
     *
     * @return The <i>raw</i> mention.
     */
    public final String getMention() {
        return "<@" + getId().asString() + ">";
    }

    @Override
    public final Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    /**
     * Requests to retrieve this user as a {@link Member}.
     *
     * @param guildId The ID of the guild to associate this user as a {@link Member}.
     * @return A {@link Mono} where, upon successful completion, emits this user as a {@link Member member}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> asMember(final Snowflake guildId) {
        return getClient().getMemberById(guildId, getId());
    }

    /**
     * Requests to retrieve the private channel (DM) to this user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link PrivateChannel private channel} to
     * this user. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<PrivateChannel> getPrivateChannel() {
        return serviceMediator.getRestClient().getUserService()
                .createDM(new DMCreateRequest(getId().asLong()))
                .map(EntityUtil::getChannelBean)
                .map(bean -> EntityUtil.getChannel(serviceMediator, bean))
                .cast(PrivateChannel.class);
    }

    /**
     * Gets the ServiceMediator associated to this object.
     *
     * @return The ServiceMediator associated to this object.
     */
    protected final ServiceMediator getServiceMediator() {
        return serviceMediator;
    }
}
