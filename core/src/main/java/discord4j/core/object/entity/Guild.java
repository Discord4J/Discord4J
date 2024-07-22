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
import discord4j.common.store.action.read.ReadActions;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.*;
import discord4j.core.object.audit.AuditLogPart;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.entity.channel.*;
import discord4j.core.object.onboarding.Onboarding;
import discord4j.core.object.presence.Presence;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.*;
import discord4j.core.spec.legacy.*;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import discord4j.core.util.MentionUtil;
import discord4j.core.util.OrderUtil;
import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import discord4j.rest.util.PaginationUtil;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * A Discord guild.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild">Guild Resource</a>
 */
public final class Guild implements Entity {

    /** The default value for the maximum number of presences. **/
    private static final int DEFAULT_MAX_PRESENCES = 25000;

    /** The path for guild icon image URLs. */
    private static final String ICON_IMAGE_PATH = "icons/%s/%s";

    /** The path for guild splash image URLs. */
    private static final String SPLASH_IMAGE_PATH = "splashes/%s/%s";

    /** The path for guild discovery splash image URLs. */
    private static final String DISCOVERY_SPLASH_IMAGE_PATH = "discovery-splashes/%s/%s";

    /** The path for guild banner image URLs. */
    private static final String BANNER_IMAGE_PATH = "banners/%s/%s";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final GuildData data;

    /**
     * Constructs a {@code Guild} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Guild(final GatewayDiscordClient gateway, final GuildData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the data of the guild.
     *
     * @return The data of the guild.
     */
    public GuildData getData() {
        return data;
    }

    /**
     * Gets the guild name.
     *
     * @return The guild name.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the icon URL of the guild, if present.
     *
     * @param format The format for the URL.
     * @return The icon URL of the guild, if present.
     */
    public Optional<String> getIconUrl(final Image.Format format) {
        return data.icon()
                .map(icon -> ImageUtil.getUrl(String.format(ICON_IMAGE_PATH, getId().asString(), icon), format));
    }

    /**
     * Gets the icon of the guild.
     *
     * @param format The format in which to get the image.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image icon} of the guild. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getIcon(final Image.Format format) {
        return Mono.justOrEmpty(getIconUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the splash URL of the guild, if present.
     *
     * @param format The format for the URL.
     * @return The splash URL of the guild, if present.
     */
    public Optional<String> getSplashUrl(final Image.Format format) {
        return data.splash()
                .map(splash -> ImageUtil.getUrl(String.format(SPLASH_IMAGE_PATH, getId().asString(), splash), format));
    }

    /**
     * Gets the splash of the guild.
     *
     * @param format The format in which to get the image.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image splash} of the guild. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getSplash(final Image.Format format) {
        return Mono.justOrEmpty(getSplashUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the discovery splash URL of the guild, if present.
     *
     * @param format The format for the URL.
     * @return The discovery splash URL of the guild, if present.
     */
    public Optional<String> getDiscoverySplashUrl(final Image.Format format) {
        return data.discoverySplash()
                .map(splash -> ImageUtil.getUrl(String.format(DISCOVERY_SPLASH_IMAGE_PATH, getId().asString(),
                        splash), format));
    }

    /**
     * Gets the discovery splash of the guild.
     *
     * @param format The format in which to get the image.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image discovery splash} of the guild.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getDiscoverySplash(final Image.Format format) {
        return Mono.justOrEmpty(getDiscoverySplashUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the banner URL of the guild, if present.
     *
     * @param format The format for the URL.
     * @return The banner URL of the guild, if present.
     */
    public Optional<String> getBannerUrl(final Image.Format format) {
        return data.banner()
                .map(splash -> ImageUtil.getUrl(String.format(BANNER_IMAGE_PATH, getId().asString(), splash), format));
    }

    /**
     * Gets the banner of the guild.
     *
     * @param format The format in which to get the image.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image banner} of the guild. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getBanner(final Image.Format format) {
        return Mono.justOrEmpty(getBannerUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the ID of the owner of the guild.
     *
     * @return The ID of the owner of the guild.
     */
    public Snowflake getOwnerId() {
        return Snowflake.of(data.ownerId());
    }

    /**
     * Requests to retrieve the owner of the guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member owner} of the guild. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getOwner() {
        return gateway.getMemberById(getId(), getOwnerId());
    }

    /**
     * Gets the voice region ID for the guild.
     *
     * @return The voice region ID for the guild.
     * @deprecated Voice region are now specific to voice channels. Use {@code VoiceChannel#getRtcRegion} instead.
     */
    @Deprecated
    public Region.Id getRegionId() {
        return Region.Id.of(Possible.flatOpt(data.region()).orElse(null));
    }

    /**
     * Requests to retrieve the voice region for the guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits the voice {@link Region region} for the guild. If
     * an error is received, it is emitted through the {@code Mono}.
     * @deprecated Voice regions are now specific to voice channels. Use {@code VoiceChannel#getRtcRegion} instead.
     */
    @Deprecated
    public Mono<Region> getRegion() {
        return getRegions().filter(response -> response.getId().equals(getRegionId().getValue())).single();
    }

    /**
     * Requests to retrieve the voice regions for the guild.
     *
     * @return A {@link Flux} that continually emits the guild's {@link Region voice regions}. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    public Flux<Region> getRegions() {
        return gateway.getRestClient().getGuildService()
                .getGuildVoiceRegions(getId().asLong())
                .map(data -> new Region(gateway, data));
    }

    /**
     * Gets the ID of the AFK channel, if present.
     *
     * @return The ID of the AFK channel, if present.
     */
    public Optional<Snowflake> getAfkChannelId() {
        return data.afkChannelId().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the AFK channel, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the AFK {@link VoiceChannel channel}, if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> getAfkChannel() {
        return Mono.justOrEmpty(getAfkChannelId()).flatMap(gateway::getChannelById).cast(VoiceChannel.class);
    }

    /**
     * Requests to retrieve the AFK channel, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the AFK channel
     * @return A {@link Mono} where, upon successful completion, emits the AFK {@link VoiceChannel channel}, if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> getAfkChannel(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getAfkChannelId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(VoiceChannel.class);
    }

    /**
     * Gets the AFK timeout in seconds.
     *
     * @return The AFK timeout in seconds.
     */
    public int getAfkTimeout() {
        return data.afkTimeout();
    }

    /**
     * Gets the Premium Tier (Server Boost level) for the guild.
     *
     * @return The Premium Tier (Server Boost level) for the guild.
     */
    public PremiumTier getPremiumTier() {
        return PremiumTier.of(data.premiumTier());
    }

    /**
     * Gets the number of boosts this server currently has, if present.
     *
     * @return The number of boosts this server currently has, if present.
     */
    public OptionalInt getPremiumSubscriptionCount() {
        return data.premiumSubscriptionCount().toOptional()
                .map(OptionalInt::of)
                .orElse(OptionalInt.empty());
    }

    /**
     * Gets the preferred locale of a Community guild used in server discovery and notices from Discord; defaults to
     * "en-US".
     *
     * @return The preferred locale of a Community guild used in server discovery and notices from Discord; defaults
     * to "en-US".
     */
    public Locale getPreferredLocale() {
        return new Locale.Builder().setLanguageTag(data.preferredLocale()).build();
    }

    /**
     * Gets the level of verification required for the guild.
     *
     * @return The level of verification required for the guild.
     */
    public VerificationLevel getVerificationLevel() {
        return VerificationLevel.of(data.verificationLevel());
    }

    /**
     * Gets the default message notification level..
     *
     * @return The default message notification level.
     */
    public NotificationLevel getNotificationLevel() {
        return NotificationLevel.of(data.defaultMessageNotifications());
    }

    /**
     * Gets the default explicit content filter level.
     *
     * @return The default explicit content filter level.
     */
    public ContentFilterLevel getContentFilterLevel() {
        return ContentFilterLevel.of(data.explicitContentFilter());
    }

    /**
     * Gets the guild's roles' IDs.
     *
     * @return The guild's roles' IDs.
     */
    public Set<Snowflake> getRoleIds() {
        return data.roles().stream().map(Snowflake::of).collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the guild's roles.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use {@link OrderUtil#orderRoles(Flux)}
     * to consistently order roles.
     *
     * @return A {@link Flux} that continually emits the guild's {@link Role roles}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles() {
        return gateway.getGuildRoles(getId());
    }

    /**
     * Requests to retrieve the guild's roles, using the given retrieval strategy.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use {@link OrderUtil#orderRoles(Flux)}
     * to consistently order roles.
     *
     * @param retrievalStrategy the strategy to use to get the roles
     * @return A {@link Flux} that continually emits the guild's {@link Role roles}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildRoles(getId());
    }

    /**
     * Requests to retrieve the role as represented by the supplied ID.
     *
     * @param id The ID of the role.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRoleById(final Snowflake id) {
        return gateway.getRoleById(getId(), id);
    }

    /**
     * Requests to retrieve the role as represented by the supplied ID, using the given retrieval strategy.
     *
     * @param id The ID of the role.
     * @param retrievalStrategy the strategy to use to get the role
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRoleById(final Snowflake id, EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getRoleById(getId(), id);
    }

    /**
     * Requests to retrieve the guild's @everyone {@link Role}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the @everyone {@link Role}, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getEveryoneRole() {
        return gateway.getRoleById(getId(), getId());
    }

    /**
     * Requests to retrieve the guild's @everyone {@link Role}, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the everyone role
     * @return A {@link Mono} where, upon successful completion, emits the @everyone {@link Role}, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getEveryoneRole(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getRoleById(getId(), getId());
    }

    /**
     * Gets the guild's sticker's IDs.
     *
     * @return The guild's sticker's IDs.
     */
    public Set<Snowflake> getStickerIds() {
        return data.stickers().toOptional().map(ids -> ids.stream().map(Snowflake::of).collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

    /**
     * Requests to retrieve the guild's stickers.
     *
     * @return A {@link Flux} that continually emits guild's {@link GuildSticker stickers}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<GuildSticker> getStickers() {
        return gateway.getGuildStickers(getId());
    }

    /**
     * Gets the guild's emoji's IDs.
     *
     * @return The guild's emoji's IDs.
     */
    public Set<Snowflake> getEmojiIds() {
        return data.emojis().stream().map(Snowflake::of).collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the guild's stickers, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the stickers
     * @return A {@link Flux} that continually emits guild's {@link GuildSticker stickers}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<GuildSticker> getStickers(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildStickers(getId());
    }

    /**
     * Requests to retrieve the guild sticker as represented by the supplied ID.
     *
     * @param id The ID of the guild sticker.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildSticker} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildSticker> getGuildStickerById(final Snowflake id) {
        return gateway.getGuildStickerById(getId(), id);
    }

    /**
     * Requests to retrieve the guild sticker as represented by the supplied ID, using the given retrieval strategy.
     *
     * @param id The ID of the guild sticker.
     * @param retrievalStrategy the strategy to use to get the guild sticker
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildSticker} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildSticker> getGuildStickerById(final Snowflake id, EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildStickerById(getId(), id);
    }

    /**
     * Requests to retrieve the guild's emojis.
     *
     * @return A {@link Flux} that continually emits guild's {@link GuildEmoji emojis}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<GuildEmoji> getEmojis() {
        return gateway.getGuildEmojis(getId());
    }

    /**
     * Requests to retrieve the guild's emojis, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the emojis
     * @return A {@link Flux} that continually emits guild's {@link GuildEmoji emojis}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<GuildEmoji> getEmojis(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildEmojis(getId());
    }

    /**
     * Requests to retrieve the guild emoji as represented by the supplied ID.
     *
     * @param id The ID of the guild emoji.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> getEmojiById(final Snowflake id) {
        return gateway.getGuildEmojiById(getId(), id);
    }

    /**
     * Requests to retrieve the guild emoji as represented by the supplied ID, using the given retrieval strategy.
     *
     * @param id The ID of the guild emoji.
     * @param retrievalStrategy the strategy to use to get the guild emoji
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> getEmojiById(final Snowflake id, EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildEmojiById(getId(), id);
    }

    /**
     * Gets the enabled guild features.
     * <br>
     * You can see the available
     * <a href="https://discord.com/developers/docs/resources/guild#guild-object-guild-features">guild features</a>
     *
     * @return The enabled guild features.
     * @deprecated Use {@code Guild#getGuildFeatures} instead
     */
    @Deprecated
    public Set<String> getFeatures() {
        return new HashSet<>(data.features());
    }

    /**
     * Gets the enabled features of this {@link Guild}.
     * If the EnumSet contains an UNKNOWN value, it means that one or more values are not implemented yet
     * or did not match the Discord Guild Features.
     * <br>
     * Raw data features are still available with {@link #getData} using {@link GuildData#features}.
     *
     * @return A {@code EnumSet} with the enabled guild features.
     */
    public EnumSet<GuildFeature> getGuildFeatures() {
        return data.features().stream()
            .map(GuildFeature::of)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(GuildFeature.class)));
    }

    /**
     * Gets the required MFA level for the guild.
     *
     * @return The required MFA level for the guild.
     */
    public MfaLevel getMfaLevel() {
        return MfaLevel.of(data.mfaLevel());
    }

    /**
     * Gets the application ID of the guild creator if it is bot-created.
     *
     * @return The application ID of the guild creator if it is bot-created.
     */
    public Optional<Snowflake> getApplicationId() {
        return data.applicationId().map(Snowflake::of);
    }

    /**
     * Gets whether or not the server widget is enabled.
     *
     * @return Whether or not the server widget is enabled.
     */
    public boolean isWidgetEnabled() {
        return data.widgetEnabled().toOptional().orElse(false);
    }

    /**
     * Gets the channel ID that the widget will generate an invite to, if present.
     *
     * @return The channel ID that the widget will generate an invite to, if present.
     */
    public Optional<Snowflake> getWidgetChannelId() {
        return Possible.flatOpt(data.widgetChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel for the server widget, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel channel} for the server
     * widget, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getWidgetChannel() {
        return Mono.justOrEmpty(getWidgetChannelId()).flatMap(gateway::getChannelById).cast(GuildChannel.class);
    }

    /**
     * Requests to retrieve the channel for the server widget, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the widget channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel channel} for the server
     * widget, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getWidgetChannel(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getWidgetChannelId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(GuildChannel.class);
    }

    /**
     * Gets the ID of the channel where guild notices such as welcome messages and boost events are posted, if present.
     *
     * @return The ID of the channel where guild notices such as welcome messages and boost events are posted, if
     * present.
     */
    public Optional<Snowflake> getSystemChannelId() {
        return data.systemChannelId().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel to which system messages are sent, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} to which system
     * messages are sent, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getSystemChannel() {
        return Mono.justOrEmpty(getSystemChannelId()).flatMap(gateway::getChannelById).cast(TextChannel.class);
    }

    /**
     * Requests to retrieve the channel to which system messages are sent, if present, using the given retrieval
     * strategy.
     *
     * @param retrievalStrategy the strategy to use to get the system channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} to which system
     * messages are sent, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getSystemChannel(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getSystemChannelId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(TextChannel.class);
    }

    /**
     * Returns the flags of the system {@link TextChannel channel}.
     *
     * @return A {@code EnumSet} with the flags of the system {@link TextChannel channel}.
     */
    public EnumSet<SystemChannelFlag> getSystemChannelFlags() {
        return SystemChannelFlag.of(data.systemChannelFlags().orElse(0));
    }

    /**
     * Gets the id of the channel where Community guilds display rules and/or guidelines, if present.
     *
     * @return The id of the channel where Community guilds display rules and/or guidelines, if present.
     */
    public Optional<Snowflake> getRulesChannelId() {
        return data.rulesChannelId().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel where Community guilds display rules and/or guidelines, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} where Community
     * guilds display rules and/or guidelines, if present. If an error is received, it is emitted through the {@code
     * Mono}.
     */
    public Mono<TextChannel> getRulesChannel() {
        return Mono.justOrEmpty(getRulesChannelId()).flatMap(gateway::getChannelById).cast(TextChannel.class);
    }

    /**
     * Requests to retrieve the channel where Community guilds display rules and/or guidelines, if present, using
     * the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the rules channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} where Community
     * guilds
     * display rules and/or guidelines, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getRulesChannel(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getRulesChannelId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(TextChannel.class);
    }

    /**
     * Gets the id of the channel where admins and moderators of Community guilds receive notices from Discord, if
     * present.
     *
     * @return The id of the channel where admins and moderators of Community guilds receive notices from Discord, if
     * present.
     */
    public Optional<Snowflake> getPublicUpdatesChannelId() {
        return data.publicUpdatesChannelId().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel where admins and moderators of Community guilds receive notices from Discord,
     * if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} where admins
     * and moderators of Community guilds receive notices from Discord, if present. If an error is received, it is
     * emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getPublicUpdatesChannel() {
        return Mono.justOrEmpty(getPublicUpdatesChannelId()).flatMap(gateway::getChannelById).cast(TextChannel.class);
    }

    /**
     * Requests to retrieve the channel where admins and moderators of Community guilds receive notices from Discord,
     * if present,
     * using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the rules channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} where admins
     * and moderators of Community guilds receive notices from Discord, if present. If an error is received, it is
     * emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getPublicUpdatesChannel(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getPublicUpdatesChannelId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(TextChannel.class);
    }

    /**
     * Gets the id of the channel where admins and moderators of Community guilds receive safety alerts from Discord, if
     * present.
     *
     * @return The id of the channel where admins and moderators of Community guilds receive safety alerts from Discord, if
     * present.
     */
    public Optional<Snowflake> getSafetyAlertsChannelId() {
        return data.safetyAlertsChannelId().map(Snowflake::of);
    }

    /**
     * Gets the maximum amount of users in a video channel, if present.
     *
     * @return The maximum amount of users in a video channel, if present.
     */
    public Optional<Integer> getMaxVideoChannelUsers() {
        return data.maxVideoChannelUsers().toOptional();
    }

    /**
     * Gets when this guild was joined at. If this {@link Guild} object was {@link EntityRetrievalStrategy retrieved}
     * using REST API, then calling this method will throw {@link DateTimeParseException}.
     *
     * @return When this guild was joined at.
     */
    public Instant getJoinTime() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.joinedAt(), Instant::from);
    }

    /**
     * Gets whether this guild is considered large. If this {@link Guild} object was {@link EntityRetrievalStrategy
     * retrieved} using REST API, then calling this method will always return {@code false}.
     *
     * @return If present, {@code true} if the guild is considered large, {@code false} otherwise.
     */
    public boolean isLarge() {
        return data.large();
    }

    /**
     * Gets whether this guild is unavailable.
     *
     * @return If present, {@code true} if the guild is unavailable, {@code false} otherwise.
     */
    public boolean isUnavailable() {
        return data.unavailable().toOptional().orElse(false);
    }

    /**
     * Gets whether this guild is embeddable (e.g. widget).
     *
     * @return Whether this guild is embeddable (e.g. widget).
     * @deprecated Use {@code Guild#isWidgetEnabled} instead
     */
    @Deprecated
    public boolean isEmbedEnabled() {
        return isWidgetEnabled();
    }

    /**
     * Gets the total number of members in the guild. If this {@link Guild} object was
     * {@link EntityRetrievalStrategy retrieved} using REST API, then calling this method will always return the same
     * value.
     *
     * @return The total number of members in the guild.
     */
    public int getMemberCount() {
        return data.memberCount();
    }

    /**
     * Gets whether this guild is designated as NSFW.
     *
     * @return Whether this guild is designated as NSFW.
     * @deprecated Use {@code getNsfwLevel()} instead
     */
    @Deprecated
    public boolean isNsfw() {
        return data.nsfw().toOptional().orElse(false);
    }

    /**
     * Gets the guild NSFW level.
     *
     * @return The guild NSFW level.
     */
    public Guild.NsfwLevel getNsfwLevel() {
        return NsfwLevel.of(data.nsfwLevel());
    }

    /**
     * Requests to retrieve the voice states of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<VoiceState> getVoiceStates() {
        return Flux.from(gateway.getGatewayResources().getStore()
                .execute(ReadActions.getVoiceStatesInGuild(getId().asLong())))
                .map(data -> new VoiceState(gateway, data));
    }

    /**
     * Requests to retrieve the members of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link Member members} of the guild. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    public Flux<Member> getMembers() {
        return gateway.getGuildMembers(getId());
    }

    /**
     * Requests to retrieve the members of the guild, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the members
     * @return A {@link Flux} that continually emits the {@link Member members} of the guild. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    public Flux<Member> getMembers(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildMembers(getId());
    }

    /**
     * Return all {@link Member members} from this {@link Guild} using the current Gateway connection.
     *
     * @return a {@link Flux} of {@link Member} for the given {@link Guild}. If an error occurs, it is emitted through
     * the {@link Flux}.
     */
    public Flux<Member> requestMembers() {
        return gateway.requestMembers(getId());
    }

    /**
     * Return a set of {@link Member members} from this guild using the current Gateway connection.
     * This method performs a check to validate whether the given guild's data can be obtained from this
     * {@link GatewayDiscordClient}.
     *
     * @param userIds the {@link Snowflake} set of users to request
     * @return a {@link Flux} of {@link Member} for the given {@link Guild}. If an error occurs, it is emitted through
     * the {@link Flux}.
     */
    public Flux<Member> requestMembers(Set<Snowflake> userIds) {
        return gateway.requestMembers(getId(), userIds);
    }

    /**
     * Returns a list of {@link Member members} whose username or nickname starts with the provided username.
     *
     * @param username the string to match username(s) and nickname(s) against.
     * @param limit the max number of members to return.
     * @return a {@link Flux} of {@link Member} whose username or nickname starts with the provided username. If an
     * error occurs, it is emitted through the {@link Flux}.
     */
    public Flux<Member> searchMembers(String username, int limit) {
        Map<String, Object> queryParams = new HashMap<>(2);
        queryParams.put("query", username);
        queryParams.put("limit", limit);
        return gateway.getRestClient().getGuildService()
                .searchGuildMembers(data.id().asLong(), queryParams)
                .map(memberData -> new Member(gateway, memberData, data.id().asLong()));
    }

    /**
     * Requests to retrieve the member as represented by the supplied ID.
     *
     * @param id The ID of the member.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMemberById(final Snowflake id) {
        return gateway.getMemberById(getId(), id);
    }

    /**
     * Requests to retrieve the member as represented by the supplied ID, using the given retrieval strategy.
     *
     * @param id The ID of the member.
     * @param retrievalStrategy the strategy to use to get the member
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMemberById(final Snowflake id, EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getMemberById(getId(), id);
    }

    /**
     * Requests to retrieve the member as represented by the bot user's ID.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the bot
     * user's ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getSelfMember() {
        return this.getMemberById(gateway.getSelfId());
    }

    /**
     * Requests to retrieve the guild's channels.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use
     * {@link OrderUtil#orderGuildChannels(Flux)}
     * to consistently order channels.
     *
     * @return A {@link Flux} that continually emits the guild's {@link GuildChannel channels}. If an error is
     * received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<GuildChannel> getChannels() {
        return gateway.getGuildChannels(getId());
    }

    /**
     * Requests to retrieve the guild's channels, using the given retrieval strategy.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use
     * {@link OrderUtil#orderGuildChannels(Flux)}
     * to consistently order channels.
     *
     * @param retrievalStrategy the strategy to use to get the channels
     * @return A {@link Flux} that continually emits the guild's {@link GuildChannel channels}. If an error is
     * received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<GuildChannel> getChannels(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildChannels(getId());
    }

    /**
     * Requests to retrieve the channel as represented by the supplied ID.
     *
     * @param id The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getChannelById(final Snowflake id) {
        return gateway.getChannelById(id)
                .cast(GuildChannel.class)
                .filter(channel -> channel.getGuildId().equals(getId()));
    }

    /**
     * Requests to retrieve the channel as represented by the supplied ID, using the given retrieval strategy.
     *
     * @param id The ID of the channel.
     * @param retrievalStrategy the strategy to use to get the channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getChannelById(final Snowflake id, EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id)
                .cast(GuildChannel.class)
                .filter(channel -> channel.getGuildId().equals(getId()));
    }

    /**
     * Requests to retrieve the presences of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link Presence presences} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<Presence> getPresences() {
        return Flux.from(gateway.getGatewayResources().getStore()
                .execute(ReadActions.getPresencesInGuild(getId().asLong())))
                .map(Presence::new);
    }

    /**
     * Gets the vanity url code of the guild, if present.
     *
     * @return The vanity url code of the guild, if present.
     */
    public Optional<String> getVanityUrlCode() {
        return data.vanityUrlCode();
    }

    /**
     * Gets the description of the guild, if present.
     *
     * @return The description of the guild, if present.
     */
    public Optional<String> getDescription() {
        return data.description();
    }

    /**
     * Gets the maximum amount of presences of the guild.
     *
     * @return The maximum amount of presences for the guild.
     */
    public int getMaxPresences() {
        return Possible.flatOpt(data.maxPresences()).orElse(DEFAULT_MAX_PRESENCES);
    }

    /**
     * Gets the mention for the given {@link ResourceNavigation} channel.
     *
     * @param resourceNavigation The {@link ResourceNavigation} to get the mention for.
     * @return The mention for the given {@link ResourceNavigation} channel.
     */
    public String getResourceNavigationMention(ResourceNavigation resourceNavigation) {
        return resourceNavigation.getMention();
    }

    /**
     * Gets the maximum amount of members of the guild, if present.
     *
     * @return The maximum amount of members for the guild, if present.
     */
    public OptionalInt getMaxMembers() {
        return data.maxMembers().toOptional()
                .map(OptionalInt::of)
                .orElseGet(OptionalInt::empty);
    }

    /**
     * Requests to edit this guild.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyGuildEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(GuildEditSpec)} or {@link #edit()} which offer an immutable approach to build specs
     */
    @Deprecated
    public Mono<Guild> edit(final Consumer<? super LegacyGuildEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyGuildEditSpec mutatedSpec = new LegacyGuildEditSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .modifyGuild(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new Guild(gateway, GuildData.builder()
                        .from(this.data)
                        .from(data)
                        .build()));
    }

    /**
     * Requests to edit this guild. Properties specifying how to edit this guild can be set via the {@code withXxx}
     * methods of the returned {@link GuildEditMono}.
     *
     * @return A {@link GuildEditMono} where, upon successful completion, emits the edited {@link Guild}. If an error is
     * received, it is emitted through the {@code GuildEditMono}.
     */
    public GuildEditMono edit() {
        return GuildEditMono.of(this);
    }

    /**
     * Requests to edit this guild.
     *
     * @param spec an immutable object that specifies how to edit this guild
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> edit(GuildEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                        .modifyGuild(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> new Guild(gateway, GuildData.builder()
                        .from(this.data)
                        .from(data)
                        .build()));
    }

    /**
     * Requests to create an emoji.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyGuildEmojiCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createEmoji(GuildEmojiCreateSpec)} or {@link #createEmoji(String, Image)} which offer an
     * immutable approach to build specs
     */
    @Deprecated
    public Mono<GuildEmoji> createEmoji(final Consumer<? super LegacyGuildEmojiCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyGuildEmojiCreateSpec mutatedSpec = new LegacyGuildEmojiCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getEmojiService()
                            .createGuildEmoji(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new GuildEmoji(gateway, data, getId().asLong()));
    }

    /**
     * Requests to create an emoji. Properties specifying how to create an emoji can be set via the {@code withXxx}
     * methods of the returned {@link GuildEmojiCreateMono}.
     *
     * @param name the name of the emoji to create
     * @param image the image of the emoji to create
     * @return A {@link GuildEmojiCreateMono} where, upon successful completion, emits the created {@link GuildEmoji}.
     * If an error is received, it is emitted through the {@code GuildEmojiCreateMono}.
     */
    public GuildEmojiCreateMono createEmoji(String name, Image image) {
        return GuildEmojiCreateMono.of(name, image, this);
    }

    /**
     * Requests to create an emoji.
     *
     * @param spec an immutable object that specifies how to create the emoji
     * @return A {@link Mono} where, upon successful completion, emits the created {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> createEmoji(GuildEmojiCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getEmojiService()
                        .createGuildEmoji(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> new GuildEmoji(gateway, data, getId().asLong()));
    }

    /**
     * Requests to create a sticker.
     *
     * @param spec an immutable object that specifies how to create the sticker
     * @return A {@link Mono} where, upon successful completion, emits the created {@link GuildSticker}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildSticker> createSticker(GuildStickerCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getStickerService()
                    .createGuildSticker(getId().asLong(), spec.asRequest(), spec.reason()))
            .map(data -> new GuildSticker(gateway, data, getId().asLong()));
    }

    /**
     * Requests to create a template based on this guild.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyGuildTemplateCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon subscription, emits the created {@link GuildTemplate} on success. If an error
     * is received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createTemplate(GuildTemplateCreateSpec)} or {@link #createTemplate(String)} which offer
     * an immutable approach to build specs
     */
    @Deprecated
    public Mono<GuildTemplate> createTemplate(final Consumer<? super LegacyGuildTemplateCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyGuildTemplateCreateSpec mutatedSpec = new LegacyGuildTemplateCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getTemplateService()
                            .createTemplate(getId().asLong(), mutatedSpec.asRequest());
                })
                .map(data -> new GuildTemplate(gateway, data));
    }

    /**
     * Requests to create a template based on this guild. A description for this template can be set via the {@link
     * GuildTemplateCreateMono#withDescriptionOrNull(String)} method of the returned {@link GuildTemplateCreateMono}.
     *
     * @param name the name of the template to create
     * @return A {@link Mono} where, upon subscription, emits the created {@link GuildTemplate} on success. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public GuildTemplateCreateMono createTemplate(String name) {
        return GuildTemplateCreateMono.of(name, this);
    }

    /**
     * Requests to create a template based on this guild.
     *
     * @param spec an immutable object that specifies how to create a template for this guild
     * @return A {@link Mono} where, upon subscription, emits the created {@link GuildTemplate} on success. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildTemplate> createTemplate(GuildTemplateCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getTemplateService().createTemplate(getId().asLong(), spec.asRequest()))
                .map(data -> new GuildTemplate(gateway, data));
    }

    /**
     * Requests to create a role.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyRoleCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Role}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createRole(RoleCreateSpec)} or {@link #createRole()} which offer an immutable approach to
     * build specs
     */
    @Deprecated
    public Mono<Role> createRole(final Consumer<? super LegacyRoleCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyRoleCreateSpec mutatedSpec = new LegacyRoleCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .createGuildRole(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new Role(gateway, data, getId().asLong()));
    }

    /**
     * Requests to create a role. Properties specifying how to create the role can be set via the {@code withXxx}
     * methods of the returned {@link RoleCreateMono}.
     *
     * @return A {@link RoleCreateMono} where, upon successful completion, emits the created {@link Role}. If an error
     * is received, it is emitted through the {@code RoleCreateMono}.
     */
    public RoleCreateMono createRole() {
        return RoleCreateMono.of(this);
    }

    /**
     * Requests to create a role.
     *
     * @param spec an immutable object that specifies how to create the role
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Role}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> createRole(RoleCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                        .createGuildRole(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> new Role(gateway, data, getId().asLong()));
    }

    /**
     * Requests to create a news channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyNewsChannelCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link NewsChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createNewsChannel(NewsChannelCreateSpec)} or {@link #createNewsChannel(String)} which
     * offer an immutable approach to build specs
     */
    @Deprecated
    public Mono<NewsChannel> createNewsChannel(final Consumer<? super LegacyNewsChannelCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyNewsChannelCreateSpec mutatedSpec = new LegacyNewsChannelCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .createGuildChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(NewsChannel.class);
    }

    /**
     * Requests to create a news channel. Properties specifying how to create the news channel can be set via the {@code
     * withXxx} methods of the returned {@link NewsChannelCreateMono}.
     *
     * @param name the name of the news channel to create
     * @return A {@link NewsChannelCreateMono} where, upon successful completion, emits the created {@link NewsChannel}.
     * If an error is received, it is emitted through the {@code NewsChannelCreateMono}.
     */
    public NewsChannelCreateMono createNewsChannel(String name) {
        return NewsChannelCreateMono.of(name, this);
    }

    /**
     * Requests to create a news channel.
     *
     * @param spec an immutable object that specifies how to create the news channel
     * @return A {@link Mono} where, upon successful completion, emits the created {@link NewsChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<NewsChannel> createNewsChannel(NewsChannelCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                        .createGuildChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(NewsChannel.class);
    }

    /**
     * Requests to create a category.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyCategoryCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Category}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createCategory(CategoryCreateSpec)} or {@link #createCategory(String)} which offer an
     * immutable approach to build specs
     */
    @Deprecated
    public Mono<Category> createCategory(final Consumer<? super LegacyCategoryCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyCategoryCreateSpec mutatedSpec = new LegacyCategoryCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .createGuildChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(Category.class);
    }

    /**
     * Requests to create a category. Properties specifying how to create the category can be set via the {@code
     * withXxx} methods of the returned {@link CategoryCreateMono}.
     *
     * @param name the name of the category to create
     * @return A {@link CategoryCreateMono} where, upon successful completion, emits the created {@link Category}. If an
     * error is  received, it is emitted through the {@code CategoryCreateMono}.
     */
    public CategoryCreateMono createCategory(String name) {
        return CategoryCreateMono.of(name, this);
    }

    /**
     * Requests to create a category.
     *
     * @param spec an immutable object that specifies how to create the category
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Category}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Category> createCategory(CategoryCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                        .createGuildChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(Category.class);
    }

    /**
     * Requests to create a text channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyTextChannelCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createTextChannel(TextChannelCreateSpec)} or {@link #createTextChannel(String)} which
     * offer an immutable approach to build specs
     */
    @Deprecated
    public Mono<TextChannel> createTextChannel(final Consumer<? super LegacyTextChannelCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyTextChannelCreateSpec mutatedSpec = new LegacyTextChannelCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .createGuildChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(TextChannel.class);
    }

    /**
     * Requests to create a text channel. Properties specifying how to create the text channel can be set via the {@code
     * withXxx} methods of the returned {@link TextChannelCreateMono}.
     *
     * @param name the name of the text channel to create
     * @return A {@link TextChannelCreateMono} where, upon successful completion, emits the created {@link TextChannel}.
     * If an error is received, it is emitted through the {@code TextChannelCreateMono}.
     */
    public TextChannelCreateMono createTextChannel(String name) {
        return TextChannelCreateMono.of(name, this);
    }

    /**
     * Requests to create a text channel.
     *
     * @param spec an immutable object that specifies how to create the text channel
     * @return A {@link Mono} where, upon successful completion, emits the created {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> createTextChannel(TextChannelCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                        .createGuildChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(TextChannel.class);
    }

    /**
     * Requests to create a voice channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyVoiceChannelCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #createVoiceChannel(VoiceChannelCreateSpec)} or {@link #createVoiceChannel(String)} which
     * offer an immutable approach to build specs
     */
    @Deprecated
    public Mono<VoiceChannel> createVoiceChannel(final Consumer<? super LegacyVoiceChannelCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyVoiceChannelCreateSpec mutatedSpec = new LegacyVoiceChannelCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .createGuildChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(VoiceChannel.class);
    }

    /**
     * Requests to create a voice channel. Properties specifying how to create the voice channel can be set via the
     * {@code withXxx} methods of the returned {@link VoiceChannelCreateMono}.
     *
     * @param name the name of the voice channel to create
     * @return A {@link VoiceChannelCreateMono} where, upon successful completion, emits the created {@link
     * VoiceChannel}. If an error is received, it is emitted through the {@code VoiceChannelCreateMono}.
     */
    public VoiceChannelCreateMono createVoiceChannel(String name) {
        return VoiceChannelCreateMono.of(name, this);
    }

    /**
     * Requests to create a voice channel.
     *
     * @param spec an immutable object that specifies how to create the voice channel
     * @return A {@link Mono} where, upon successful completion, emits the created {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> createVoiceChannel(VoiceChannelCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                        .createGuildChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(VoiceChannel.class);
    }


    /**
     * Requests to create a stage channel.
     *
     * @param spec an immutable object that specifies how to create the stage channel
     * @return A {@link Mono} where, upon successful completion, emits the created {@link StageChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> createStageChannel(StageChannelCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                        () -> gateway.getRestClient().getGuildService()
                                .createGuildChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(VoiceChannel.class);
    }

    /**
     * Requests to create a forum channel.
     *
     * @param spec an immutable object that specifies how to create the forum channel
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ForumChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<ForumChannel> createForumChannel(ForumChannelCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                    .createGuildChannel(getId().asLong(), spec.asRequest(), spec.reason()))
            .map(data -> EntityUtil.getChannel(gateway, data))
            .cast(ForumChannel.class);
    }

    /**
     * Requests to create an automod rule. Properties specifying how to create the rule can be set via the
     * {@code withXxx} methods of the returned {@link AutoModRuleCreateMono}.
     *
     * @param name new name to set
     * @param eventType type of event to set
     * @param triggerType type of trigger to set
     * @return A {@link AutoModRuleCreateMono} where, upon successful completion, emits the created {@link
     * AutoModRule}. If an error is received, it is emitted through the {@code AutoModRuleCreateMono}.
     */
    public AutoModRuleCreateMono createAutoModRule(String name, AutoModRule.EventType eventType, AutoModRule.TriggerType triggerType) {
        return AutoModRuleCreateMono.of(name, eventType.getValue(), triggerType.getValue(), this);
    }

    /**
     * Requests to create an AutoMod Rule.
     *
     * @param spec an immutable object that specifies how to create the emoji
     * @return A {@link Mono} where, upon successful completion, emits the created {@link AutoModRule}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<AutoModRule> createAutoModRule(AutoModRuleCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                        () -> gateway.getRestClient().getAutoModService()
                                .createAutoModRule(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> new AutoModRule(gateway, data));
    }

    /**
     * Requests to delete this guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the guild has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return gateway.getRestClient().getGuildService()
                .deleteGuild(getId().asLong());
    }

    /**
     * Requests to kick the specified user from this guild.
     *
     * @param userId The ID of the user to kick from this guild.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was kicked
     * from this guild. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> kick(final Snowflake userId) {
        return kick(userId, null);
    }

    /**
     * Requests to kick the specified user from this guild while optionally specifying a reason.
     *
     * @param userId The ID of the user to kick from this guild.
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was kicked
     * from this guild. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> kick(final Snowflake userId, @Nullable final String reason) {
        return gateway.getRestClient().getGuildService()
                .removeGuildMember(getId().asLong(), userId.asLong(), reason);
    }

    /**
     * Requests to retrieve all the bans for this guild.
     *
     * @return A {@link Flux} that continually emits the {@link Ban bans} for this guild. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<Ban> getBans() {
        return gateway.getRestClient().getGuildService()
                .getGuildBans(getId().asLong())
                .map(data -> new Ban(gateway, data));
    }

    /**
     * Requests to retrieve the ban for the specified user for this guild.
     *
     * @param userId The ID of the user to retrieve the ban for this guild.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Ban ban} for the specified user for
     * this guild. If an error is received, it is meitted through the {@code Mono}.
     */
    public Mono<Ban> getBan(final Snowflake userId) {
        return gateway.getRestClient().getGuildService()
                .getGuildBan(getId().asLong(), userId.asLong())
                .map(data -> new Ban(gateway, data));
    }

    /**
     * Requests to ban the specified user.
     *
     * @param userId The ID of the user to ban.
     * @param spec   A {@link Consumer} that provides a "blank" {@link LegacyBanQuerySpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was
     * banned. If an error is received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #ban(Snowflake, BanQuerySpec)} or {@link #ban(Snowflake)} which offer an immutable
     * approach to build specs
     */
    @Deprecated
    public Mono<Void> ban(final Snowflake userId, final Consumer<? super LegacyBanQuerySpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyBanQuerySpec mutatedSpec = new LegacyBanQuerySpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .createGuildBan(getId().asLong(), userId.asLong(), mutatedSpec.asRequest(),
                                    mutatedSpec.getReason());
                });
    }

    /**
     * Requests to ban the specified user. Properties specifying how to ban the user can be set via the {@code withXxx}
     * methods of the returned {@link GuildBanQueryMono}.
     *
     * @param userId The ID of the user to ban.
     * @return A {@link GuildBanQueryMono} where, upon successful completion, emits nothing; indicating the specified
     * user was banned. If an error is received, it is emitted through the {@code GuildBanQueryMono}.
     */
    public GuildBanQueryMono ban(final Snowflake userId) {
        return GuildBanQueryMono.of(userId, this);
    }

    /**
     * Requests to ban the specified user.
     *
     * @param userId The ID of the user to ban.
     * @param spec   an immutable object that specifies how to ban the user
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was
     * banned. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> ban(final Snowflake userId, BanQuerySpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                        .createGuildBan(getId().asLong(), userId.asLong(), spec.asRequest(), spec.reason()));
    }

    /**
     * Requests to unban the specified user.
     *
     * @param userId The ID of the user to unban.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was
     * unbanned. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> unban(final Snowflake userId) {
        return unban(userId, null);
    }

    /**
     * Requests to unban the specified user while optionally specifying a reason.
     *
     * @param userId The ID of the user to unban.
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was
     * unbanned. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> unban(final Snowflake userId, @Nullable final String reason) {
        return gateway.getRestClient().getGuildService()
                .removeGuildBan(getId().asLong(), userId.asLong(), reason);
    }

    /**
     * Requests to ban the specified users. Properties specifying how to ban the user can be set via the {@code withXxx}
     * methods of the returned {@link BulkBanRequestMono}.
     *
     * @param userIds The list of IDs of the users to ban.
     * @return A {@link BulkBanRequestMono} where, upon successful completion, emits an {@link BulkBan} with the results.
     *      * If an error is received, it is emitted through the {@code Mono}.
     */
    public BulkBanRequestMono bulkBan(List<Snowflake> userIds) {
        return BulkBanRequestMono.of(this).withUserIds(userIds);
    }

    /**
     * Request a Bulk Ban to a specific list of users.
     * <br>
     * <b>Things considered error for this request</b>
     * <ul>
     *   <li>The list of users is over 200</li>
     *   <li>None of the list of users can be banned</li>
     * </ul>
     *
     * @param spec an immutable object that specifies how to bulk ban in the guild
     * @return A {@link Mono} where, upon successful completion, emits an {@link BulkBan} with the results.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<BulkBan> bulkBan(BulkBanRequestSpec spec) {
        Objects.requireNonNull(spec);
        return gateway.getRestClient().getGuildService()
            .bulkGuildBan(getId().asLong(), spec.asRequest(), spec.reason())
            .map(data -> new BulkBan(gateway, data));
    }

    /**
     * Requests to retrieve the number of users that will be pruned. Users are pruned if they have not been seen within
     * the past specified amount of days, with roles optionally included in the prune count if specified through {@link
     * LegacyGuildPruneCountSpec#addRole(Snowflake)} or {@link LegacyGuildPruneCountSpec#addRoles(Collection)}.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildPruneCountSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the number of users that will be pruned. If an
     * error is received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #getPruneCount(GuildPruneCountSpec)} or {@link #getPruneCount(int)} which offer an
     * immutable approach to build specs
     */
    @Deprecated
    public Mono<Integer> getPruneCount(final Consumer<? super LegacyGuildPruneCountSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyGuildPruneCountSpec mutatedSpec = new LegacyGuildPruneCountSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .getGuildPruneCount(getId().asLong(), mutatedSpec.asRequest())
                            .flatMap(data -> Mono.justOrEmpty(data.pruned()));
                });
    }

    /**
     * Requests to retrieve the number of users that will be pruned. Users are pruned if they have not been seen within
     * the past specified amount of days. Included roles can be set via the
     * {@link GuildPruneCountMono#withRoles(Snowflake...)}
     * method of the returned {@link GuildPruneCountMono}.
     *
     * @param days The number of days since an user must have been seen to avoid being kicked.
     * @return A {@link GuildPruneCountMono} where, upon successful completion, emits the number of users that will be
     * pruned. If an error is received, it is emitted through the {@code GuildPruneCountMono}.
     */
    public GuildPruneCountMono getPruneCount(final int days) {
        return GuildPruneCountMono.of(days, this);
    }

    /**
     * Requests to retrieve the number of users that will be pruned. Users are pruned if they have not been seen within
     * the past specified amount of days, with roles optionally included in the prune count if specified through {@link
     * GuildPruneCountSpec#roles()}.
     *
     * @param spec an immutable object that specifies how to get prune count
     * @return A {@link Mono} where, upon successful completion, emits the number of users that will be pruned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Integer> getPruneCount(GuildPruneCountSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                        .getGuildPruneCount(getId().asLong(), spec.asRequest())
                        .flatMap(data -> Mono.justOrEmpty(data.pruned())));
    }

    /**
     * Requests to prune users while customizing parameters. Users are pruned if they have not been seen within the past
     * specified amount of days, with roles optionally included in the prune request if specified through {@link
     * LegacyGuildPruneSpec#addRole(Snowflake)} or {@link LegacyGuildPruneSpec#addRoles(Collection)}.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyGuildPruneSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, may emit the number of users who were pruned if {@link
     * LegacyGuildPruneSpec#setComputePruneCount(boolean)} is {@code true} (default), otherwise it would emit an empty
     * {@code Mono}. If an error is received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #prune(GuildPruneSpec)} or {@link #prune(int)} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<Integer> prune(final Consumer<? super LegacyGuildPruneSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyGuildPruneSpec mutatedSpec = new LegacyGuildPruneSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .beginGuildPrune(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                            .flatMap(data -> Mono.justOrEmpty(data.pruned()));
                });
    }

    /**
     * Requests to prune users. Users are pruned if they have not been seen within the past specified amount of days.
     * Included roles can be set via the {@link GuildPruneMono#withRoles(Snowflake...)} method of the returned {@link
     * GuildPruneMono}.
     *
     * @param days The number of days since an user must have been seen to avoid being kicked.
     * @return A {@link Mono} where, upon successful completion, may emit the number of users who were pruned if {@link
     * GuildPruneMono#withComputePruneCount(Boolean)} is {@code true} (default), otherwise it would emit an empty {@code
     * Mono}. If an error is received, it is emitted through the {@code Mono}.
     */
    public GuildPruneMono prune(final int days) {
        return GuildPruneMono.of(days, this);
    }

    /**
     * Requests to prune users while customizing parameters. Users are pruned if they have not been seen within
     * the past specified amount of days, with roles optionally included in the prune request if specified through
     * {@link GuildPruneSpec#roles()}.
     *
     * @param spec an immutable object that specifies how to prune users of this guild
     * @return A {@link Mono} where, upon successful completion, may emit the number of users who were pruned if
     * {@link GuildPruneSpec#computePruneCount()} is {@code true} (default), otherwise it would emit an
     * empty {@code Mono}. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Integer> prune(GuildPruneSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getGuildService()
                        .beginGuildPrune(getId().asLong(), spec.asRequest(), spec.reason())
                        .flatMap(data -> Mono.justOrEmpty(data.pruned())));
    }

    /**
     * Requests to leave this guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this guild has been left. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> leave() {
        return gateway.getRestClient().getUserService()
                .leaveGuild(getId().asLong());
    }

    /**
     * Requests to retrieve the audit log for this guild.
     * <p>
     * The audit log parts can be {@link AuditLogPart#combine(AuditLogPart) combined} for easier querying. For example,
     * <pre>
     * {@code
     * guild.getAuditLog()
     *     .take(10)
     *     .reduce(AuditLogPart::combine)
     * }
     * </pre>
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyAuditLogQuerySpec} to be operated on.
     * @return A {@link Flux} that continually parts of this guild's audit log. If an error is received, it is emitted
     * through the {@code Flux}.
     * @deprecated use {@link #getAuditLog(AuditLogQuerySpec)} or {@link #getAuditLog()} which offer an immutable
     * approach to build specs
     */
    @Deprecated
    public Flux<AuditLogPart> getAuditLog(final Consumer<? super LegacyAuditLogQuerySpec> spec) {
        final Function<Map<String, Object>, Flux<AuditLogData>> makeRequest = params -> {
            final LegacyAuditLogQuerySpec mutatedSpec = new LegacyAuditLogQuerySpec();
            spec.accept(mutatedSpec);
            params.putAll(mutatedSpec.asRequest());
            return gateway.getRestClient().getAuditLogService()
                    .getAuditLog(getId().asLong(), params)
                    .flux();
        };
        final ToLongFunction<AuditLogData> getLastEntryId = response -> {
            final List<AuditLogEntryData> entries = response.auditLogEntries();
            return (entries.isEmpty()) ? Long.MAX_VALUE :
                    Snowflake.asLong(entries.get(entries.size() - 1).id());
        };
        return PaginationUtil.paginateBefore(makeRequest, getLastEntryId, Long.MAX_VALUE, 100)
                .map(data -> new AuditLogPart(getId().asLong(), gateway, data));
    }

    /**
     * Requests to retrieve the audit log for this guild. Properties specifying how to query audit log can be set via
     * {@code withXxx} methods of the returned {@link AuditLogQueryFlux}.
     * <p>
     * The audit log parts can be {@link AuditLogPart#combine(AuditLogPart) combined} for easier querying. For example,
     * <pre>
     * {@code
     * guild.getAuditLog()
     *     .take(10)
     *     .reduce(AuditLogPart::combine)
     * }
     * </pre>
     *
     * @return A {@link AuditLogQueryFlux} that continually emits parts of this guild's audit log. If an error is
     * received, it is emitted
     * through the {@code Flux}.
     */
    public AuditLogQueryFlux getAuditLog() {
        return AuditLogQueryFlux.of(this);
    }

    /**
     * Requests to retrieve the audit log for this guild.
     * <p>
     * The audit log parts can be {@link AuditLogPart#combine(AuditLogPart) combined} for easier querying. For example,
     * <pre>
     * {@code
     * guild.getAuditLog()
     *     .take(10)
     *     .reduce(AuditLogPart::combine)
     * }
     * </pre>
     *
     * @param spec an immutable object that specifies how to query audit log
     * @return A {@link Flux} that continually emits parts of this guild's audit log. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<AuditLogPart> getAuditLog(AuditLogQuerySpec spec) {
        Objects.requireNonNull(spec);
        final Function<Map<String, Object>, Flux<AuditLogData>> makeRequest = params -> {
            params.putAll(spec.asRequest());
            return gateway.getRestClient().getAuditLogService()
                    .getAuditLog(getId().asLong(), params)
                    .flux();
        };

        final ToLongFunction<AuditLogData> getLastEntryId = response -> {
            final List<AuditLogEntryData> entries = response.auditLogEntries();
            return (entries.isEmpty()) ? Long.MAX_VALUE :
                    Snowflake.asLong(entries.get(entries.size() - 1).id());
        };

        return PaginationUtil.paginateBefore(makeRequest, getLastEntryId, Long.MAX_VALUE, 100)
                .map(data -> new AuditLogPart(getId().asLong(), gateway, data));
    }

    /**
     * Requests to retrieve the webhooks of the guild. Requires the MANAGE_WEBHOOKS permission.
     *
     * @return A {@link Flux} that continually emits the {@link Webhook webhooks} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<Webhook> getWebhooks() {
        return gateway.getRestClient().getWebhookService()
                .getGuildWebhooks(getId().asLong())
                .map(data -> new Webhook(gateway, data));
    }

    /**
     * Requests to retrieve the invites of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link ExtendedInvite invites} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<ExtendedInvite> getInvites() {
        return gateway.getRestClient().getGuildService()
                .getGuildInvites(getId().asLong())
                .map(data -> new ExtendedInvite(gateway, data));
    }

    /**
     * Requests to retrieve the templates of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link GuildTemplate templates} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<GuildTemplate> getTemplates() {
        return gateway.getRestClient().getTemplateService()
            .getTemplates(getId().asLong())
            .map(data -> new GuildTemplate(gateway, data));
    }

    /**
     * Requests to change the bot user's nickname in this guild.
     *
     * @param nickname The new nickname for the bot user in this guild, or {@code null} to remove it.
     * @return A {@link Mono} where, upon successful completion, emits the bot user's new nickname in this guild. If
     * the nickname was set to {@code null}, then this {@link Mono} will complete empty. If an error is received, it
     * is emitted through the {@code Mono}.
     */
    public Mono<String> changeSelfNickname(@Nullable final String nickname) {
        return gateway.getRestClient().getGuildService()
                .modifyCurrentMember(getId().asLong(), CurrentMemberModifyData.builder()
                        .nick(Optional.ofNullable(nickname))
                        .build())
                .handle((data, sink) -> {
                    String nick = Possible.flatOpt(data.nick()).orElse(null);
                    if (nick != null) {
                        sink.next(nick);
                    } else {
                        sink.complete();
                    }
                });
    }

    /**
     * Returns the current voice connection registered for this guild.
     *
     * @return A {@link Mono} of {@link VoiceConnection} for this guild if present, or empty otherwise.
     */
    public Mono<VoiceConnection> getVoiceConnection() {
        return gateway.getVoiceConnectionRegistry().getVoiceConnection(getId());
    }

    /**
     * Requests to retrieve the active threads of the guild.
     * <p>
     * The audit log parts can be {@link ThreadListPart#combine(ThreadListPart) combined} for easier querying. For example,
     * <pre>
     * {@code
     * guild.getActiveThreads()
     *     .take(10)
     *     .reduce(ThreadListPart::combine)
     * }
     * </pre>
     *
     * @return A {@link Flux} that continually emits the {@link ThreadListPart threads} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Mono<ThreadListPart> getActiveThreads() {
        return gateway.getRestClient().getGuildService()
                .listActiveGuildThreads(data.id().asLong())
                .map(data -> new ThreadListPart(gateway, data));
    }

    /**
     * Requests to retrieve the automod rules of the guild. Requires the MANAGE_GUILD permission.
     *
     * @return A {@link Flux} that continually emits the {@link AutoModRule automod rules} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<AutoModRule> getAutoModRules() {
        return gateway.getRestClient().getAutoModService()
                .getAutoModRules(getId().asLong())
                .map(data -> new AutoModRule(gateway, data));
    }

    /**
     * Requests to retrieve the automod rule of the guild using the ID. Requires the MANAGE_GUILD permission.
     *
     * @return A {@link Mono} of {@link AutoModRule} for this guild if present, or empty otherwise.
     */
    public Mono<AutoModRule> getAutoModRule(Snowflake autoModRuleId) {
        return gateway.getRestClient().getAutoModService()
            .getAutoModRule(getId().asLong(), autoModRuleId.asLong())
            .map(data -> new AutoModRule(gateway, data));
    }

    /**
     * Requests to retrieve the scheduled event using the provided ID.
     *
     * @param eventId the event ID
     * @param withUserCount Requests to fetch the enrolled user count to Discord or not
     * @return A {@link Mono} which, upon completion, emits an associated {@link ScheduledEvent} if found.
     */
    public Mono<ScheduledEvent> getScheduledEventById(Snowflake eventId, boolean withUserCount) {
        return gateway.getRestClient().getScheduledEventById(getId(), eventId).getData(withUserCount)
            .map(data -> new ScheduledEvent(gateway, data));
    }

    /**
     * Requests to retrieve all the scheduled events associated to this guild.
     *
     * @param withUserCount Requests to fetch the enrolled user count to Discord or not
     * @return A {@link Flux} which emits {@link ScheduledEvent} objects.
     */
    public Flux<ScheduledEvent> getScheduledEvents(boolean withUserCount) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("with_user_count", withUserCount);

        return gateway.getRestClient().getGuildService().getScheduledEvents(getId().asLong(), queryParams)
            .map(data -> new ScheduledEvent(gateway, data));
    }

    /**
     * Requests to create a guild scheduled event with the provided spec on this guild
     *
     * @param spec spec specifying {@link ScheduledEvent} parameters
     * @return A {@link Mono} which, upon completion, emits the created {@link ScheduledEvent} object. Any error, if occurs,
     * is emitted through the {@link Mono}.
     */
    public Mono<ScheduledEvent> createScheduledEvent(ScheduledEventCreateSpec spec) {
        return gateway.getRestClient().getGuildService().createScheduledEvent(getId().asLong(), spec.asRequest())
            .map(data -> new ScheduledEvent(gateway, data));
    }

    /**
     * Request the guild's entitlements associated with the current application.
     * The request can be filtered using the "withXXX" methods of the returned {@link EntitlementListRequestFlux}.
     *
     * @return A {@link EntitlementListRequestFlux} which emits {@link discord4j.core.object.monetization.Entitlement} objects.
     * If an error is received, it is emitted through the {@link Flux}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public EntitlementListRequestFlux getEntitlements() {
        return gateway.getEntitlements().withGuildId(getId());
    }

    /**
     * Request to create a test entitlement for the guild with the provided SKU ID.
     *
     * @return A {@link CreateTestEntitlementMono} which emits the created {@link discord4j.core.object.monetization.Entitlement}.
     * If an error is received, it is emitted through the {@link Mono}.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public CreateTestEntitlementMono createTestEntitlement(Snowflake skuId) {
        return gateway.createTestEntitlementForGuild(skuId, getId());
    }

    /**
     * Get the onboarding of the guild.
     *
     * @return A {@link Mono} which, upon completion, emits the {@link Onboarding} object. Any error, if occurs,
     * is emitted through the {@link Mono}.
     */
    public Mono<Onboarding> getOnboarding() {
        return this.gateway.getRestClient()
            .getGuildService()
            .getOnboarding(this.getId().asLong())
            .map(data -> new Onboarding(this.gateway, data));
    }

    /**
     * Request to edit the onboarding of the guild with the provided spec.
     *
     * @param spec spec specifying how to edit the onboarding
     * @return A {@link Mono} which, upon completion, emits the edited {@link Onboarding} object. Any error, if occurs,
     * is emitted through the {@link Mono}.
     */
    public Mono<Onboarding> modifyOnboarding(OnboardingEditSpec spec) {
        return this.gateway.getRestClient()
            .getGuildService()
            .modifyOnboarding(this.getId().asLong(), spec.asRequest(), spec.reason())
            .map(data -> new Onboarding(this.gateway, data));
    }

    /**
     * Requests to edit the onboarding of the guild. Properties specifying how to edit the onboarding can be set via the
     * {@code withXxx} methods of the returned {@link OnboardingEditMono}.
     *
     * @return A {@link Mono} which, upon completion, emits nothing. Any error, if occurs,
     * is emitted through the {@link Mono}.
     */
    public OnboardingEditMono modifyOnboarding() {
        return OnboardingEditMono.of(this);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    /** Automatically scan and delete messages sent in the server that contain explicit content. */
    public enum ContentFilterLevel {

        /** Unknown content filter level. */
        UNKNOWN(-1),

        /** Don't scan any messages. */
        DISABLED(0),

        /** Scan messages from members without a role. */
        MEMBERS_WITHOUT_ROLES(1),

        /** Scan messages sent by all members. */
        ALL_MEMBERS(2);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.ContentFilterLevel}.
         *
         * @param value The underlying value as represented by Discord.
         */
        ContentFilterLevel(final int value) {
            this.value = value;
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
         * Gets the content filter level of the guild. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The content filter level of the guild.
         */
        public static ContentFilterLevel of(final int value) {
            switch (value) {
                case 0: return DISABLED;
                case 1: return MEMBERS_WITHOUT_ROLES;
                case 2: return ALL_MEMBERS;
                default: return UNKNOWN;
            }
        }
    }

    /**
     * Prevent potentially dangerous administrative actions for users without two-factor authentication enabled. This
     * setting can only be changed by the server owner if they have 2FA enabled on their account.
     */
    public enum MfaLevel {

        /** Unknown MFA level. */
        UNKNOWN(-1),

        /** Disabled 2FA requirement. */
        NONE(0),

        /** Enabled 2FA requirement. */
        ELEVATED(1);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.MfaLevel}.
         *
         * @param value The underlying value as represented by Discord.
         */
        MfaLevel(final int value) {
            this.value = value;
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
         * Gets the multi-factor authentication level of the guild. It is guaranteed that invoking {@link #getValue()}
         * from the returned enum will equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The multi-factor authentication level of the guild.
         */
        public static MfaLevel of(final int value) {
            switch (value) {
                case 0: return NONE;
                case 1: return ELEVATED;
                default: return UNKNOWN;
            }
        }
    }

    /**
     * Determines whether {@link Member Members} who have not explicitly set their notification settings receive a
     * notification for every message sent in the server or not.
     */
    public enum NotificationLevel {

        /** Unknown notification level. */
        UNKNOWN(-1),

        /** Receive a notification for all messages. */
        ALL_MESSAGES(0),

        /** Receive a notification only for mentions. */
        ONLY_MENTIONS(1);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.NotificationLevel}.
         *
         * @param value The underlying value as represented by Discord.
         */
        NotificationLevel(final int value) {
            this.value = value;
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
         * Gets the notification level of the guild. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The notification level of the guild.
         */
        public static NotificationLevel of(final int value) {
            switch (value) {
                case 0: return ALL_MESSAGES;
                case 1: return ONLY_MENTIONS;
                default: return UNKNOWN;
            }
        }
    }

    /**
     * Represent the server Premium Tier (aka boost level) of the {@link Guild}.
     *
     * @see <a href="https://support.discord.com/hc/en/articles/360028038352">Server Boost info</a>
     * @see
     * <a href="https://discord.com/developers/docs/resources/guild#guild-object-premium-tier">Premium Tier docs</a>
     */
    public enum PremiumTier {

        /** Unknown Premium Tier. */
        UNKNOWN(-1),

        /** No Premium Tier. **/
        NONE(0),

        /** Premium Tier 1 (Boost Level 1). **/
        TIER_1(1),

        /** Premium Tier 2 (Boost Level 2). **/
        TIER_2(2),

        /** Premium Tier 3 (Boost Level 3). **/
        TIER_3(3);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.PremiumTier}.
         *
         * @param value The underlying value as represented by Discord.
         */
        PremiumTier(final int value) {
            this.value = value;
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
         * Gets the Premium Tier (aka boost level) of the Guild. It is guaranteed that invoking {@link #getValue()}
         * from the
         * returned enum will equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The Premium Tier (aka boost level) of the {Guild.
         */
        public static PremiumTier of(final int value) {
            switch (value) {
                case 0: return NONE;
                case 1: return TIER_1;
                case 2: return TIER_2;
                case 3: return TIER_3;
                default: return UNKNOWN;
            }
        }
    }

    /**
     * {@link Member Members} of the server must meet the following criteria before they can send messages in text
     * channels or initiate a direct message conversation. If a member has an assigned role this does not apply.
     */
    public enum VerificationLevel {

        /** Unknown verification level. */
        UNKNOWN(-1),

        /** Unrestricted. */
        NONE(0),

        /** Must have verified email on account. */
        LOW(1),

        /** Must be registered on Discord for longer than 5 minutes. */
        MEDIUM(2),

        /** Must be a member of the server for longer than 10 minutes. */
        HIGH(3),

        /** Must have a verified phone number. */
        VERY_HIGH(4);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.VerificationLevel}.
         *
         * @param value The underlying value as represented by Discord.
         */
        VerificationLevel(final int value) {
            this.value = value;
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
         * Gets the verification level of the guild. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The verification level of the guild.
         */
        public static VerificationLevel of(final int value) {
            switch (value) {
                case 0: return NONE;
                case 1: return LOW;
                case 2: return MEDIUM;
                case 3: return HIGH;
                case 4: return VERY_HIGH;
                default: return UNKNOWN;
            }
        }
    }

    /** Describes system channel flags. */
    public enum SystemChannelFlag {

        /** Suppress member join notifications. */
        SUPPRESS_JOIN_NOTIFICATIONS(0),

        /** Suppress server boost notifications. */
        SUPPRESS_PREMIUM_SUBSCRIPTIONS(1),

        /** Suppress server setup tips. */
        SUPPRESS_GUILD_REMINDER_NOTIFICATIONS(2),

        /** Hide member join sticker reply buttons. */
        SUPPRESS_JOIN_NOTIFICATION_REPLIES(3),

        /** Suppress role subscription purchase and renewal notifications. */
        SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATIONS(4),

        /** Hide role subscription sticker reply buttons. */
        SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATION_REPLIES(5);

        /** The underlying value as represented by Discord. */
        private final int value;

        /** The flag value as represented by Discord. */
        private final int flag;

        /**
         * Constructs a {@code Flag}.
         */
        SystemChannelFlag(final int value) {
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
         * Gets the flags of system channel. It is guaranteed that invoking {@link #getValue()} from the returned enum
         * will be equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<SystemChannelFlag> of(final int value) {
            final EnumSet<SystemChannelFlag> flags = EnumSet.noneOf(SystemChannelFlag.class);
            for (SystemChannelFlag flag : SystemChannelFlag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    flags.add(flag);
                }
            }
            return flags;
        }
    }

    public enum NsfwLevel {

        UNKNOWN(-1),

        DEFAULT(0),

        EXPLICIT(1),

        SAFE(2),

        AGE_RESTRICTED(3);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.NsfwLevel}.
         *
         * @param value The underlying value as represented by Discord.
         */
        NsfwLevel(final int value) {
            this.value = value;
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
         * Gets the NSFW level of the guild. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The NSFW level of the guild.
         */
        public static NsfwLevel of(final int value) {
            switch (value) {
                case 0: return DEFAULT;
                case 1: return EXPLICIT;
                case 2: return SAFE;
                case 3: return AGE_RESTRICTED;
                default: return UNKNOWN;
            }
        }
    }

    /**
     * Describes the features of a guild.
     * <br>
     * You can see the available
     * @see <a href="https://discord.com/developers/docs/resources/guild#guild-object-guild-features">Guild Features</a>
     */
    public enum GuildFeature {

        /* indicates that the value is not implemented yet or does not match any of the Discord Guild Features */
        UNKNOWN("UNKNOWN", false),
        /* guild has access to set an animated guild banner image */
        ANIMATED_BANNER("ANIMATED_BANNER", false),
        /* guild has access to set an animated guild icon */
        ANIMATED_ICON("ANIMATED_ICON", false),
        /* guild is using the old permissions configuration behavior */
        APPLICATION_COMMAND_PERMISSIONS_V2("APPLICATION_COMMAND_PERMISSIONS_V2", false),
        /* guild has set up auto moderation rules */
        AUTO_MODERATION("AUTO_MODERATION", false),
        /* guild has access to set a guild banner image */
        BANNER("BANNER", false),
        /* guild can enable welcome screen, Membership Screening, stage channels and discovery, and receives community updates */
        COMMUNITY("COMMUNITY", true),
        /* guild has enabled monetization */
        CREATOR_MONETIZABLE_PROVISIONAL("CREATOR_MONETIZABLE_PROVISIONAL", false),
        /* guild has enabled the role subscription promo page */
        CREATOR_STORE_PAGE("CREATOR_STORE_PAGE", false),
        /* guild has been set as a support server on the App Directory */
        DEVELOPER_SUPPORT_SERVER("DEVELOPER_SUPPORT_SERVER", false),
        /* guild is able to be discovered in the directory */
        DISCOVERABLE("DISCOVERABLE", true),
        /* guild is able to be featured in the directory */
        FEATURABLE("FEATURABLE", false),
        /* guild has paused invites, preventing new users from joining */
        INVITES_DISABLED("INVITES_DISABLED", true),
        /* guild has access to set an invite splash background */
        INVITE_SPLASH("INVITE_SPLASH", false),
        /* guild has enabled Membership Screening */
        MEMBER_VERIFICATION_GATE_ENABLED("MEMBER_VERIFICATION_GATE_ENABLED", false),
        /* guild has increased custom sticker slots */
        MORE_STICKERS("MORE_STICKERS", false),
        /* guild has access to create announcement channels */
        NEWS("NEWS", false),
        /* guild is partnered */
        PARTNERED("PARTNERED", false),
        /* guild can be previewed before joining via Membership Screening or the directory */
        PREVIEW_ENABLED("PREVIEW_ENABLED", false),
        /* guild has disabled alerts for join raids in the configured safety alerts channel */
        RAID_ALERTS_DISABLED("RAID_ALERTS_DISABLED", true),
        /* guild is able to set role icons */
        ROLE_ICONS("ROLE_ICONS", false),
        /* guild has role subscriptions that can be purchased */
        ROLE_SUBSCRIPTIONS_AVAILABLE_FOR_PURCHASE("ROLE_SUBSCRIPTIONS_AVAILABLE_FOR_PURCHASE", false),
        /* guild has enabled role subscriptions */
        ROLE_SUBSCRIPTIONS_ENABLED("ROLE_SUBSCRIPTIONS_ENABLED", false),
        /* guild has enabled ticketed events */
        TICKETED_EVENTS_ENABLED("TICKETED_EVENTS_ENABLED", false),
        /* guild has access to set a vanity URL */
        VANITY_URL("VANITY_URL", false),
        /* guild is verified */
        VERIFIED("VERIFIED", false),
        /* guild has access to set 384kbps bitrate in voice (previously VIP voice servers) */
        VIP_REGIONS("VIP_REGIONS", false),
        /* guild has enabled the welcome screen */
        WELCOME_SCREEN_ENABLED("WELCOME_SCREEN_ENABLED", false);

        private final String value;

        private final boolean mutable;

        GuildFeature(String value, boolean mutable) {
            this.value = value;
            this.mutable = mutable;
        }

        public String getValue() {
            return value;
        }

        /**
         * @see <a href="https://discord.com/developers/docs/resources/guild#guild-object-mutable-guild-features">Mutable Guild Features</a>
         * @return a boolean indicating if the guild feature is mutable or not
         */
        public boolean isMutable() {
            return mutable;
        }

        /**
         * Gets the enabled guild features.
         * For internal use, we set unknown values to UNKNOWN.
         * @param value The value as represented by Discord.
         * @return The {@link EnumSet} of enabled features.
         */
        public static GuildFeature of(final String value) {
            return Arrays.stream(GuildFeature.values())
                .filter(guildFeature -> guildFeature.getValue().equals(value))
                .findAny()
                .orElse(GuildFeature.UNKNOWN);
        }

    }

    /**
     * Describes guild navigation types.
     *
     * @see <a href="https://discord.com/developers/docs/reference#message-formatting-guild-navigation-types">Discord Docs</a>
     */
    public enum ResourceNavigation {
        /** Customize tab with the server's onboarding prompts */
        CUSTOMIZE("customize"),
        /** Browse Channels tab */
        BROWSE("browse"),
        /** Server Guide */
        GUIDE("guide"),
        ;

        /** The underlying value as represented by Discord. */
        private final String value;

        /**
         * Constructs an {@code Guild.ResourceNavigation}.
         *
         * @param value The underlying value as represented by Discord.
         */
        ResourceNavigation(final String value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public String getValue() {
            return value;
        }

        /**
         * Gets the <i>raw</i> mention. This is the format utilized to directly mention guild resource.
         *
         * @return The <i>raw</i> mention.
         */
        public String getMention() {
            return MentionUtil.forGuildResourceNavigation(this);
        }
    }

    @Override
    public String toString() {
        return "Guild{" +
                "data=" + data +
                '}';
    }
}
