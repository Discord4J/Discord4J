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
import discord4j.core.object.data.stored.ChannelBean;
import discord4j.core.object.data.stored.UserBean;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import discord4j.rest.json.request.DMCreateRequest;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.EnumSet;
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
     * Gets the user's effective avatar URL.
     *
     * @return The user's effective avatar URL.
     * @implNote This method will first attempt to get the user's {@link #getAvatarUrl(Image.Format) avatar URL}. If the
     * avatar is {@link #hasAnimatedAvatar() animated}, a {@link Image.Format#GIF GIF} is returned; otherwise a
     * {@link Image.Format#PNG PNG} is returned. The {@link #getDefaultAvatarUrl() default avatar URL} is returned if no
     * avatar is set for this user.
     */
    public final String getAvatarUrl() {
        final boolean animated = hasAnimatedAvatar();
        return getAvatarUrl(animated ? GIF : PNG).orElse(getDefaultAvatarUrl());
    }

    /**
     * Gets the user's avatar. This is the avatar at the url given by {@link #getAvatarUrl(Image.Format)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image avatar} of the user. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getAvatar(final Image.Format format) {
        return Mono.justOrEmpty(getAvatarUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the user's effective avatar. This is the avatar at the url given by {@link #getAvatarUrl()}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image avatar} of the user. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Image> getAvatar() {
        return Image.ofUrl(getAvatarUrl());
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
                .map(ChannelBean::new)
                .map(bean -> EntityUtil.getChannel(serviceMediator, bean))
                .cast(PrivateChannel.class)
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Returns the public flags of this {@link User}, describing its features.
     *
     * @return A {@code EnumSet} with the public flags of this user.
     */
    public EnumSet<User.PublicFlag> getPublicFlags() {
        if (data.getPublicFlags() != 0) {
            return User.PublicFlag.of(data.getPublicFlags());
        }
        return EnumSet.of(PublicFlag.NONE);
    }

    /**
     * Gets the ServiceMediator associated to this object.
     *
     * @return The ServiceMediator associated to this object.
     */
    final ServiceMediator getServiceMediator() {
        return serviceMediator;
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public final int hashCode() {
        return EntityUtil.hashCode(this);
    }

    /** Describes public flags of a user. */
    public enum PublicFlag {
        NONE(-1),

        DISCORD_EMPLOYEE(0),

        DISCORD_PARTNER(1),

        HYPESQUAD_EVENTS(2),

        BUG_HUNTER_LEVEL_1(3),

        HOUSE_BRAVERY(6),

        HOUSE_BRILLIANCE(7),

        HOUSE_BALANCE(8),

        EARLY_SUPPORTER(9),

        TEAM_USER(10),

        SYSTEM(12),

        BUG_HUNTER_LEVEL_2(14),

        VERIFIED_BOT(16),

        VERIFIED_BOT_DEVELOPER(17);

        /** The underlying value as represented by Discord. */
        private final int value;

        /** The flag value as represented by Discord. */
        private final int flag;

        /**
         * Constructs a {@code User.PublicFlag}.
         */
        PublicFlag(final int value) {
            this.value = value;
            this.flag = 1 << value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord.
         */
        public int getFlag() {
            return flag;
        }

        /**
         * Gets the public flags of user. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<User.PublicFlag> of(final int value) {
            final EnumSet<User.PublicFlag> userPublicFlags = EnumSet.noneOf(User.PublicFlag.class);
            for (User.PublicFlag flag : User.PublicFlag.values()) {
                if(flag.equals(User.PublicFlag.NONE)) {
                    continue;
                }
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    userPublicFlags.add(flag);
                }
            }
            return userPublicFlags;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "data=" + data +
                '}';
    }
}
