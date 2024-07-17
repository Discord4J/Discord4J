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

import discord4j.common.annotations.Experimental;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.CreateTestEntitlementMono;
import discord4j.core.spec.EntitlementListRequestFlux;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import discord4j.core.util.MentionUtil;
import discord4j.discordjson.json.DMCreateRequest;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import discord4j.rest.util.Image;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import static discord4j.rest.util.Image.Format.GIF;
import static discord4j.rest.util.Image.Format.PNG;

/**
 * A Discord user.
 *
 * @see <a href="https://discord.com/developers/docs/resources/user">Users Resource</a>
 */
public class User implements Entity {

    /** The path for default user avatar image URLs. */
    private static final String DEFAULT_IMAGE_PATH = "embed/avatars/%d";

    /** The path for user avatar image URLs. */
    private static final String AVATAR_IMAGE_PATH = "avatars/%s/%s";

    /** The path for user banner image URLs. */
    private static final String BANNER_IMAGE_PATH = "banners/%s/%s";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final UserData data;

    /**
     * Constructs an {@code User} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public User(final GatewayDiscordClient gateway, final UserData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public final GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the user.
     *
     * @return The data of the user.
     */
    public UserData getUserData() {
        return data;
    }

    /**
     * Gets the user's global username, not enforced to be unique across the platform.
     * May be empty if the user has not set a global username.
     *
     * @return The user's global name
     */
    public final Optional<String> getGlobalName() {
        return data.globalName();
    }

    /**
     * Gets the user's username.
     * May or may not be unique across the platform (due to the system ongoing change)
     *
     * @return The user's username, not unique across the platform.
     */
    public final String getUsername() {
        return data.username();
    }

    /**
     * Gets the user's 4-digit discriminator
     * The discriminator is unique number to distinct one among all users with the same username.
     * The discriminator is randomly generated, but can be changed if the user has a nitro subscription.
     * Migrated users from the old system will have a discriminator of "0".
     * May become null after the system change is complete.
     *
     * @return The user's 4-digit discriminator, or "0" if the user is migrated to the new system.
     * @deprecated This method will be removed once the system change is complete.
     */
    @Deprecated
    @Nullable
    public final String getDiscriminator() {
        return data.discriminator();
    }

    /**
     * Returns whether the user is migrated to the new system or not.
     *
     * @return true if the user is migrated to the new system
     */
    private boolean isMigrated() {
        return getDiscriminator() == null || getDiscriminator().equals("0");
    }

    /**
     * Gets the user's username and discriminator separated by a # or its username if the user is using the new system
     *
     * @return {@link User#getUsername()}#{@link User#getDiscriminator()} if the user is not migrated, {@link User#getUsername()} otherwise.
     */
    public final String getTag() {
        if (isMigrated()) {
            return getUsername();
        }

        return getUsername() + "#" + getDiscriminator();
    }

    /**
     * Gets if the user's avatar is animated.
     *
     * @return {@code true} if the user's avatar is animated, {@code false} otherwise.
     */
    public final boolean hasAnimatedAvatar() {
        final String avatar = data.avatar().orElse(null);
        return (avatar != null) && avatar.startsWith("a_");
    }

    /**
     * Gets the user's avatar URL, if present.
     *
     * @param format The format for the URL.
     * @return The user's avatar URL, if present.
     */
    public final Optional<String> getAvatarUrl(final Image.Format format) {
        return data.avatar().map(avatar -> ImageUtil.getUrl(
                String.format(AVATAR_IMAGE_PATH, getId().asString(), avatar), format));
    }

    /**
     * Gets the user's effective avatar URL.
     *
     * @return The user's effective avatar URL.
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
        if (isMigrated()) {
            return ImageUtil.getUrl(String.format(DEFAULT_IMAGE_PATH, (getId().asLong() >> 22) % 6), PNG);
        }

        return ImageUtil.getUrl(String.format(DEFAULT_IMAGE_PATH, Integer.parseInt(getDiscriminator()) % 5), PNG);
    }

    /**
     * Gets if the user's banner is animated.
     *
     * @return {@code true} if the user's banner is animated, {@code false} otherwise.
     */
    public final boolean hasAnimatedBanner() {
        final String banner = Possible.flatOpt(data.banner()).orElse(null);
        return (banner != null) && banner.startsWith("a_");
    }

    /**
     * Gets the user's banner URL, if present.
     *
     * @param format The format for the URL.
     * @return The user's banner URL, if present.
     */
    public final Optional<String> getBannerUrl(final Image.Format format) {
        return Possible.flatOpt(data.banner()).map(banner -> ImageUtil.getUrl(
                String.format(BANNER_IMAGE_PATH, getId().asString(), banner), format));
    }

    /**
     * Gets the user's effective banner URL.
     *
     * @return The user's effective banner URL.
     */
    public final Optional<String> getBannerUrl() {
        final boolean animated = hasAnimatedBanner();
        return getBannerUrl(animated ? GIF : PNG);
    }

    /**
     * Gets the user's banner. This is the banner at the url given by {@link #getBannerUrl(Image.Format)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image banner} of the user. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getBanner(final Image.Format format) {
        return Mono.justOrEmpty(getBannerUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the user's effective banner. This is the banner at the url given by {@link #getBannerUrl()}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image banner} of the user. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Image> getBanner() {
        return Mono.justOrEmpty(getBannerUrl()).flatMap(Image::ofUrl);
    }

    /**
     * Gets the user's banner accent color, if present.
     *
     * @return The user's banner accent color, if present.
     */
    public final Optional<Color> getAccentColor() {
        return Possible.flatOpt(data.accentColor())
                .map(Color::of);
    }

    /**
     * Gets the user avatar decoration, if present.
     *
     * @return The user avatar decoration, if present.
     */
    public Optional<AvatarDecoration> getAvatarDecoration() {
        return Possible.flatOpt(data.avatarDecoration())
            .map(data -> new AvatarDecoration(this.getClient(), data));
    }

    /**
     * Gets whether the user is a bot.
     *
     * @return {@code true} if this user is a bot, {@code false} otherwise.
     */
    public boolean isBot() {
        return data.bot().toOptional().orElse(false);
    }

    /**
     * Gets the <i>raw</i> mention. This is the format utilized to directly mention another user (assuming the user
     * exists in context of the mention).
     *
     * @return The <i>raw</i> mention.
     */
    public final String getMention() {
        return MentionUtil.forUser(getId());
    }

    @Override
    public final Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Requests to retrieve this user as a {@link Member}.
     *
     * @param guildId The ID of the guild to associate this user as a {@link Member}.
     * @return A {@link Mono} where, upon successful completion, emits this user as a {@link Member member}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> asMember(final Snowflake guildId) {
        return gateway.getMemberById(guildId, getId());
    }

    /**
     * Requests to retrieve this user as a {@link Member}, using the given retrieval strategy.
     *
     * @param guildId The ID of the guild to associate this user as a {@link Member}.
     * @param retrievalStrategy the strategy to use to get the member
     * @return A {@link Mono} where, upon successful completion, emits this user as a {@link Member member}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> asMember(final Snowflake guildId, EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getMemberById(guildId, getId());
    }

    /**
     * Requests to retrieve the private channel (DM) to this user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link PrivateChannel private channel} to
     * this user. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<PrivateChannel> getPrivateChannel() {
        return gateway.getRestClient().getUserService()
                .createDM(DMCreateRequest.builder().recipientId(Snowflake.asString(getId().asLong())).build())
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(PrivateChannel.class);
    }

    /**
     * Returns the public flags of this {@link User}, describing its features.
     *
     * @return A {@code EnumSet} with the public flags of this user.
     */
    public EnumSet<Flag> getPublicFlags() {
        long publicFlags = data.publicFlags().toOptional().orElse(0L);
        if (publicFlags != 0) {
            return Flag.of(publicFlags);
        }
        return EnumSet.noneOf(Flag.class);
    }

    /**
     * Request the user's entitlements associated with the current application.
     * The request can be filtered using the "withXXX" methods of the returned {@link EntitlementListRequestFlux}.
     *
     * @return A {@link EntitlementListRequestFlux} which emits {@link discord4j.core.object.monetization.Entitlement} objects.
     * If an error is received, it is emitted through the {@link Flux}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public EntitlementListRequestFlux getEntitlements() {
        return gateway.getEntitlements().withUserId(getId());
    }

    /**
     * Request to create a test entitlement for the user with the provided SKU ID.
     *
     * @return A {@link CreateTestEntitlementMono} which emits the created {@link discord4j.core.object.monetization.Entitlement}.
     * If an error is received, it is emitted through the {@link Mono}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public CreateTestEntitlementMono createTestEntitlement(Snowflake skuId) {
        return gateway.createTestEntitlementForUser(skuId, getId());
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public final int hashCode() {
        return EntityUtil.hashCode(this);
    }

    /** Describes the flags of a user.
     * @see <a href="https://discord.com/developers/docs/resources/user#user-object-user-flags">Discord Docs - User Flags</a>
     **/
    public enum Flag {
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

        VERIFIED_BOT_DEVELOPER(17),

        DISCORD_CERTIFIED_MODERATOR(18),

        BOT_HTTP_INTERACTIONS(19),

        ACTIVE_DEVELOPER(22);

        /** The underlying value as represented by Discord. */
        private final int value;

        /** The flag value as represented by Discord. */
        private final int flag;

        /**
         * Constructs a {@code User.Flag}.
         */
        Flag(final int value) {
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
         * Gets the flags of user. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<Flag> of(final long value) {
            final EnumSet<Flag> userFlags = EnumSet.noneOf(Flag.class);
            for (Flag flag : Flag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    userFlags.add(flag);
                }
            }
            return userFlags;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "data=" + data +
                '}';
    }
}
