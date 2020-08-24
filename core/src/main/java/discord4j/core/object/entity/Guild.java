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
import discord4j.core.object.Ban;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.Region;
import discord4j.core.object.VoiceState;
import discord4j.core.object.audit.AuditLogEntry;
import discord4j.core.object.entity.channel.*;
import discord4j.core.object.presence.Presence;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.*;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import discord4j.core.util.OrderUtil;
import discord4j.discordjson.json.AuditLogData;
import discord4j.discordjson.json.AuditLogEntryData;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.NicknameModifyData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import discord4j.rest.util.PaginationUtil;
import discord4j.store.api.util.LongLongTuple2;
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
     * Constructs an {@code Guild} with an associated ServiceMediator and Discord data.
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
     */
    public String getRegionId() {
        return data.region();
    }

    /**
     * Requests to retrieve the voice region for the guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits the voice {@link Region region} for the guild. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Region> getRegion() {
        return getRegions().filter(response -> response.getId().equals(getRegionId())).single();
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
     * Gets the ID of the embedded channel, if present.
     *
     * @return The ID of the embedded channel, if present.
     * @deprecated Use {@code Guild#getWidgetChannelId} instead. For removal in v3.2
     */
    @Deprecated
    public Optional<Snowflake> getEmbedChannelId() {
        return getWidgetChannelId();
    }

    /**
     * Requests to retrieve the embedded channel, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the embedded {@link GuildChannel channel}, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getEmbedChannel() {
        return Mono.justOrEmpty(getEmbedChannelId()).flatMap(gateway::getChannelById).cast(GuildChannel.class);
    }

    /**
     * Requests to retrieve the embedded channel, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the embedded channel
     * @return A {@link Mono} where, upon successful completion, emits the embedded {@link GuildChannel channel}, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getEmbedChannel(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getEmbedChannelId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(GuildChannel.class);
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
        return Possible.flatOpt(data.premiumSubscriptionCount())
                .map(OptionalInt::of)
                .orElse(OptionalInt.empty());
    }

    /**
     * Gets the preferred locale of a "PUBLIC" guild used in server discovery and notices from Discord; defaults to
     * "en-US".
     *
     * @return The preferred locale of a "PUBLIC" guild used in server discovery and notices from Discord; defaults
     * to "en-US".
     */
    public Locale getPreferredLocale() {
        return new Locale.Builder().setLanguageTag(data.preferredLocale().orElse("en-US")).build();
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
     * Gets the guild's emoji's IDs.
     *
     * @return The guild's emoji's IDs.
     */
    public Set<Snowflake> getEmojiIds() {
        return data.emojis().stream().map(Snowflake::of).collect(Collectors.toSet());
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
    public Mono<GuildEmoji> getGuildEmojiById(final Snowflake id) {
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
    public Mono<GuildEmoji> getGuildEmojiById(final Snowflake id, EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildEmojiById(getId(), id);
    }

    /**
     * Gets the enabled guild features.
     * <br>
     * You can see the available
     * <a href="https://discord.com/developers/docs/resources/guild#guild-object-guild-features">guild features</a>
     *
     * @return The enabled guild features.
     */
    public Set<String> getFeatures() {
        return new HashSet<>(data.features());
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
     * Gets the id of the channel where "PUBLIC" guilds display rules and/or guidelines, if present.
     *
     * @return The id of the channel where "PUBLIC" guilds display rules and/or guidelines, if present.
     */
    public Optional<Snowflake> getRulesChannelId() {
        return data.rulesChannelId().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel where "PUBLIC" guilds display rules and/or guidelines, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} where "PUBLIC"
     * guilds display rules and/or guidelines, if present. If an error is received, it is emitted through the {@code
     * Mono}.
     */
    public Mono<TextChannel> getRulesChannel() {
        return Mono.justOrEmpty(getRulesChannelId()).flatMap(gateway::getChannelById).cast(TextChannel.class);
    }

    /**
     * Requests to retrieve the channel where "PUBLIC" guilds display rules and/or guidelines, if present, using
     * the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the rules channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} where "PUBLIC"
     * guilds
     * display rules and/or guidelines, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getRulesChannel(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getRulesChannelId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(TextChannel.class);
    }

    /**
     * Gets the id of the channel where admins and moderators of "PUBLIC" guilds receive notices from Discord, if
     * present.
     *
     * @return The id of the channel where admins and moderators of "PUBLIC" guilds receive notices from Discord, if
     * present.
     */
    public Optional<Snowflake> getPublicUpdatesChannelId() {
        return data.publicUpdatesChannelId().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel where admins and moderators of "PUBLIC" guilds receive notices from Discord,
     * if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} where admins
     * and moderators of
     * "PUBLIC" guilds receive notices from Discord, if present. If an error is received, it is emitted through the
     * {@code Mono}.
     */
    public Mono<TextChannel> getPublicUpdatesChannel() {
        return Mono.justOrEmpty(getPublicUpdatesChannelId()).flatMap(gateway::getChannelById).cast(TextChannel.class);
    }

    /**
     * Requests to retrieve the channel where admins and moderators of "PUBLIC" guilds receive notices from Discord,
     * if present,
     * using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the rules channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} where admins
     * and moderators
     * of "PUBLIC" guilds receive notices from Discord, if present. If an error is received, it is emitted through
     * the {@code Mono}.
     */
    public Mono<TextChannel> getPublicUpdatesChannel(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getPublicUpdatesChannelId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(id))
                .cast(TextChannel.class);
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
     * Requests to retrieve the voice states of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<VoiceState> getVoiceStates() {
        return gateway.getGatewayResources().getStateView().getVoiceStateStore()
                .findInRange(LongLongTuple2.of(getId().asLong(), Long.MIN_VALUE),
                        LongLongTuple2.of(getId().asLong(), Long.MAX_VALUE))
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
        return gateway.getGatewayResources().getStateView().getPresenceStore()
                .findInRange(LongLongTuple2.of(getId().asLong(), Long.MIN_VALUE),
                        LongLongTuple2.of(getId().asLong(), Long.MAX_VALUE))
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
     * <p>
     * The properties of the category are configurable by the {@link GuildEditMono}.
     *
     * <pre>
     * {@code
     * guild.edit()
     *     .withVerificationLevel(VerificationLevel.VERY_HIGH)
     *     .withReason("Raid!")
     * }
     * </pre>
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public GuildEditMono edit() {
        return new GuildEditMono(gateway, getId().asLong(), data);
    }

    /**
     * Requests to create an emoji.
     * The properties of the category are configurable by the {@link GuildEmojiCreateMono}.
     *
     * <pre>
     * {@code
     * Image.ofUrl("https://cdn.discordapp.com/emojis/546687597246939136.png")
     *     .flatMap(image ->
     *         guild.createEmoji()
     *             .withName("d4j")
     *             .withImage(image))
     * }
     * </pre>
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public GuildEmojiCreateMono createEmoji() {
        return new GuildEmojiCreateMono(gateway, getId().asLong());
    }

    /**
     * Requests to create a role.
     * <p>
     * The properties of the role are configurable by the {@link RoleCreateMono}.
     *
     * <pre>
     * {@code
     * guild.createRole()
     *     .withName("My role")
     *     .withHoist(true)
     *     .withMentionable(false)
     * }
     * </pre>
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Role}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public RoleCreateMono createRole() {
        return new RoleCreateMono(gateway, getId().asLong());
    }

    /**
     * Requests to create a news channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link NewsChannelCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link NewsChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<NewsChannel> createNewsChannel(final Consumer<? super NewsChannelCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    NewsChannelCreateSpec mutatedSpec = new NewsChannelCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .createGuildChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(NewsChannel.class);
    }

    /**
     * Requests to create a category.
     * <p>
     * The properties of the category are configurable by the {@link CategoryCreateMono}.
     *
     * <pre>
     * {@code
     * guild.createCategory()
     *     .withName("My Category")
     *     .withPosition(5)
     * }
     * </pre>
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Category}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public CategoryCreateMono createCategory() {
        return new CategoryCreateMono(gateway, getId().asLong());
    }

    /**
     * Requests to create a text channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link TextChannelCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> createTextChannel(final Consumer<? super TextChannelCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    TextChannelCreateSpec mutatedSpec = new TextChannelCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .createGuildChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(TextChannel.class);
    }

    /**
     * Requests to create a voice channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link VoiceChannelCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> createVoiceChannel(final Consumer<? super VoiceChannelCreateSpec> spec) {
        return Mono.defer(
                () -> {
                    VoiceChannelCreateSpec mutatedSpec = new VoiceChannelCreateSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .createGuildChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(gateway, data))
                .cast(VoiceChannel.class);
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
     * <p>
     * The reason and number of days to delete the banned users' messages are configurable by the {@link BanMono}.
     *
     * <pre>
     * {@code
     * guild.ban(toBan)
     *     .withReason("rekt")
     *     .withDeleteMessageDays(5)
     * }
     * </pre>
     *
     * @param userId The ID of the user to ban.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was
     * banned. If an error is received, it is emitted through the {@code Mono}.
     */
    public BanMono ban(final Snowflake userId) {
        return new BanMono(getClient(), getId().asLong(), userId.asLong());
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
     * Requests to retrieve the number of users that will be pruned. Users are pruned if they have not been seen within
     * the past specified amount of days <i>and</i> are not assigned to any roles for this guild.
     *
     * @param days The number of days since an user must have been seen to avoid being kicked.
     * @return A {@link Mono} where, upon successful completion, emits the number of users that will be pruned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Integer> getPruneCount(final int days) {
        final Map<String, Object> queryParams = new HashMap<>(1);
        queryParams.put("days", days);

        return gateway.getRestClient().getGuildService()
                .getGuildPruneCount(getId().asLong(), queryParams)
                .flatMap(data -> Mono.justOrEmpty(data.pruned()));
    }

    /**
     * Requests to retrieve the number of users that will be pruned. Users are pruned if they have not been seen within
     * the past specified amount of days, with roles optionally included in the prune count if specified through
     * {@link GuildPruneCountMono#addRole(Snowflake)} or {@link GuildPruneCountMono#addRoles(Collection)}.
     *
     * @return A {@link GuildPruneCountMono} where, upon successful completion, emits the number of users that will be pruned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public GuildPruneCountMono getPruneCount() {
        return new GuildPruneCountMono(gateway, getId().asLong());
    }

    /**
     * Requests to prune users. Users are pruned if they have not been seen within the past specified amount of days
     * <i>and</i> are not assigned to any roles for this guild.
     *
     * @param days The number of days since an user must have been seen to avoid being kicked.
     * @return A {@link Mono} where, upon successful completion, emits the number of users who were pruned. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Integer> prune(final int days) {
        return prune(days, null);
    }

    /**
     * Requests to prune users while optionally specifying a reason. Users are pruned if they have not been seen within
     * the past specified amount of days <i>and</i> are not assigned to any roles for this guild.
     *
     * @param days The number of days since an user must have been seen to avoid being kicked.
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits the number of users who were pruned. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Integer> prune(final int days, @Nullable final String reason) {
        final Map<String, Object> queryParams = new HashMap<>(1);
        queryParams.put("days", days);

        return gateway.getRestClient().getGuildService()
                .beginGuildPrune(getId().asLong(), queryParams, reason)
                .flatMap(data -> Mono.justOrEmpty(data.pruned()));
    }

    /**
     * Requests to prune users while customizing parameters. Users are pruned if they have not been seen within
     * the past specified amount of days, with roles optionally included in the prune request if specified through
     * {@link GuildPruneSpec#addRole(Snowflake)} or {@link GuildPruneSpec#addRoles(Collection)}.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildPruneSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, may emit the number of users who were pruned if
     * {@link GuildPruneSpec#setComputePruneCount(boolean)} is {@code true} (default), otherwise it would emit an
     * empty {@code Mono}. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Integer> prune(final Consumer<? super GuildPruneSpec> spec) {
        return Mono.defer(
                () -> {
                    GuildPruneSpec mutatedSpec = new GuildPruneSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getGuildService()
                            .beginGuildPrune(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                            .flatMap(data -> Mono.justOrEmpty(data.pruned()));
                });
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
     * The entries to retrieve can be configured by the {@link AuditLogFlux}.
     *
     * <pre>
     * {@code
     * guild.getAuditLog()
     *     .withActionType(ActionType.MEMBER_BAN_ADD)
     *     .take(20)
     * }
     * </pre>
     *
     * @return A {@link Flux} that continually emits entries for this guild's audit log.
     */
    public AuditLogFlux getAuditLog() {
        return new AuditLogFlux(gateway, getId().asLong());
    }

    /**
     * Requests to retrieve the webhooks of the guild.
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
     * Requests to change the bot user's nickname in this guild.
     *
     * @param nickname The new nickname for the bot user in this guild, or {@code null} to remove it.
     * @return A {@link Mono} where, upon successful completion, emits the bot user's new nickname in this guild. If
     * the nickname was set to {@code null}, then this {@link Mono} will complete empty. If an error is received, it
     * is emitted through the {@code Mono}.
     */
    public Mono<String> changeSelfNickname(@Nullable final String nickname) {
        return gateway.getRestClient().getGuildService()
                .modifyOwnNickname(getId().asLong(), NicknameModifyData.builder()
                        .nick(Optional.ofNullable(nickname))
                        .build())
                .handle((data, sink) -> {
                    String nick = data.nick().orElse(null);
                    if (nick != null) {
                        sink.next(nick);
                    } else {
                        sink.complete();
                    }
                });
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

        /** (╯°□°）╯︵ ┻━┻ - Must be a member of the server for longer than 10 minutes. */
        HIGH(3),

        /** ┻━┻ミヽ(ಠ益ಠ)ﾉ彡┻━┻ - Must have a verified phone number. */
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

        /** Member join notifications are suppressed. */
        SUPPRESS_JOIN_NOTIFICATIONS(0),

        /** Server boost notifications are suppressed. */
        SUPPRESS_PREMIUM_SUBSCRIPTIONS(1);

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

    @Override
    public String toString() {
        return "Guild{" +
                "data=" + data +
                '}';
    }
}
