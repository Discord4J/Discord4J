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

import discord4j.common.json.GuildMemberResponse;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.Ban;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.Region;
import discord4j.core.object.VoiceState;
import discord4j.core.object.audit.AuditLogEntry;
import discord4j.core.object.data.*;
import discord4j.core.object.data.stored.*;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.*;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import discord4j.core.util.PaginationUtil;
import discord4j.rest.json.request.NicknameModifyRequest;
import discord4j.rest.json.response.AuditLogEntryResponse;
import discord4j.rest.json.response.AuditLogResponse;
import discord4j.rest.json.response.NicknameModifyResponse;
import discord4j.rest.json.response.PruneResponse;
import discord4j.store.api.util.LongLongTuple2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static discord4j.core.object.util.Image.Format.*;

/**
 * A Discord guild.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/guild">Guild Resource</a>
 */
public final class Guild implements Entity {

    /** The path for guild icon image URLs. */
    private static final String ICON_IMAGE_PATH = "icons/%s/%s";

    /** The path for guild splash image URLs. */
    private static final String SPLASH_IMAGE_PATH = "splashes/%s/%s";

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final BaseGuildBean data;

    /**
     * Constructs an {@code Guild} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Guild(final ServiceMediator serviceMediator, final BaseGuildBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    private Optional<GuildBean> getGatewayData() {
        return (data instanceof GuildBean) ? Optional.of((GuildBean) data) : Optional.empty();
    }

    /**
     * Gets the guild name.
     *
     * @return The guild name.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the icon URL of the guild, if present and in a supported format.
     *
     * @param format The format for the URL. Supported format types are {@link Image.Format#PNG PNG},
     * {@link Image.Format#JPEG JPEG}, and {@link Image.Format#WEB_P WebP}.
     * @return The icon URL of the guild, if present and in a supported format.
     */
    public Optional<String> getIconUrl(final Image.Format format) {
        return Optional.ofNullable(data.getIcon())
                .filter(ignored -> (format == PNG) || (format == JPEG) || (format == WEB_P))
                .map(icon -> ImageUtil.getUrl(String.format(ICON_IMAGE_PATH, getId().asString(), icon), format));
    }

    /**
     * Gets the splash URL of the guild, if present and in a supported format.
     *
     * @param format The format for the URL. Supported format types are {@link Image.Format#PNG PNG},
     * {@link Image.Format#JPEG JPEG}, and {@link Image.Format#WEB_P WebP}.
     * @return The splash URL of the guild, if present and in a supported format.
     */
    public Optional<String> getSplashUrl(final Image.Format format) {
        return Optional.ofNullable(data.getSplash())
                .filter(ignored -> (format == PNG) || (format == JPEG) || (format == WEB_P))
                .map(splash -> ImageUtil.getUrl(String.format(SPLASH_IMAGE_PATH, getId().asString(), splash), format));
    }

    /**
     * Gets the ID of the owner of the guild.
     *
     * @return The ID of the owner of the guild.
     */
    public Snowflake getOwnerId() {
        return Snowflake.of(data.getOwnerId());
    }

    /**
     * Requests to retrieve the owner of the guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member owner} of the guild. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getOwner() {
        return getClient().getMemberById(getId(), getOwnerId());
    }

    /**
     * Gets the voice region ID for the guild.
     *
     * @return The voice region ID for the guild.
     */
    public String getRegionId() {
        return data.getRegion();
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
        return serviceMediator.getRestClient().getGuildService()
                .getGuildVoiceRegions(getId().asLong())
                .map(RegionBean::new)
                .map(bean -> new Region(serviceMediator, bean));
    }

    /**
     * Gets the ID of the AFK channel, if present.
     *
     * @return The ID of the AFK channel, if present.
     */
    public Optional<Snowflake> getAfkChannelId() {
        return Optional.ofNullable(data.getAfkChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the AFK channel, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the AFK {@link VoiceChannel channel}, if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> getAfkChannel() {
        return Mono.justOrEmpty(getAfkChannelId()).flatMap(getClient()::getChannelById).cast(VoiceChannel.class);
    }

    /**
     * Gets the AFK timeout in seconds.
     *
     * @return The AFK timeout in seconds.
     */
    public int getAfkTimeout() {
        return data.getAfkTimeout();
    }

    /**
     * Gets the ID of the embedded channel, if present.
     *
     * @return The ID of the embedded channel, if present.
     */
    public Optional<Snowflake> getEmbedChannelId() {
        return Optional.ofNullable(data.getEmbedChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the embedded channel, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the embedded {@link GuildChannel channel}, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getEmbedChannel() {
        return Mono.justOrEmpty(getEmbedChannelId()).flatMap(getClient()::getChannelById).cast(GuildChannel.class);
    }

    /**
     * Gets the level of verification required for the guild.
     *
     * @return The level of verification required for the guild.
     */
    public VerificationLevel getVerificationLevel() {
        return VerificationLevel.of(data.getVerificationLevel());
    }

    /**
     * Gets the default message notification level.
     *
     * @return The default message notification level.
     */
    public NotificationLevel getNotificationLevel() {
        return NotificationLevel.of(data.getDefaultMessageNotifications());
    }

    /**
     * Gets the default explicit content filter level.
     *
     * @return The default explicit content filter level.
     */
    public ContentFilterLevel getContentFilterLevel() {
        return ContentFilterLevel.of(data.getExplicitContentFilter());
    }

    /**
     * Gets the guild's roles' IDs.
     *
     * @return The guild's roles' IDs.
     */
    public Set<Snowflake> getRoleIds() {
        return Arrays.stream(data.getRoles())
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the guild's roles.
     *
     * @return A {@link Flux} that continually emits the guild's {@link Role roles}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles() {
        return Flux.fromIterable(getRoleIds())
                .flatMap(id -> getClient().getRoleById(getId(), id))
                .sort(Comparator.comparing(Role::getRawPosition).thenComparing(Role::getId));
    }

    /**
     * Requests to retrieve the role as represented by the supplied ID.
     *
     * @param id The ID of the role.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRoleById(final Snowflake id) {
        return getClient().getRoleById(getId(), id);
    }

    /**
     * Requests to retrieve the guild's @everyone {@link Role}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the @everyone {@link Role}, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getEveryoneRole() {
        return getClient().getRoleById(getId(), getId());
    }

    /**
     * Gets the guild's emoji's IDs.
     *
     * @return The guild's emoji's IDs.
     */
    public Set<Snowflake> getEmojiIds() {
        return Arrays.stream(data.getEmojis())
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the guild's emojis.
     *
     * @return A {@link Flux} that continually emits guild's {@link GuildEmoji emojis}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<GuildEmoji> getEmojis() {
        return Flux.fromIterable(getEmojiIds()).flatMap(id -> getClient().getGuildEmojiById(getId(), id));
    }

    /**
     * Requests to retrieve the guild emoji as represented by the supplied ID.
     *
     * @param id The ID of the guild emoji.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> getGuildEmojiById(final Snowflake id) {
        return getClient().getGuildEmojiById(getId(), id);
    }

    /**
     * Gets the enabled guild features.
     *
     * @return The enabled guild features.
     */
    public Set<String> getFeatures() {
        return Arrays.stream(data.getFeatures()).collect(Collectors.toSet());
    }

    /**
     * Gets the required MFA level for the guild.
     *
     * @return The required MFA level for the guild.
     */
    public MfaLevel getMfaLevel() {
        return MfaLevel.of(data.getMfaLevel());
    }

    /**
     * Gets the application ID of the guild creator if it is bot-created.
     *
     * @return The application ID of the guild creator if it is bot-created.
     */
    public Optional<Snowflake> getApplicationId() {
        return Optional.ofNullable(data.getApplicationId()).map(Snowflake::of);
    }

    /**
     * Gets the channel ID for the server widget, if present.
     *
     * @return The channel ID for the server widget, if present.
     */
    public Optional<Snowflake> getWidgetChannelId() {
        return Optional.ofNullable(data.getWidgetChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel for the server widget, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel channel} for the server
     * widget, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getWidgetChannel() {
        return Mono.justOrEmpty(getWidgetChannelId()).flatMap(getClient()::getChannelById).cast(GuildChannel.class);
    }

    /**
     * Gets the ID of the channel to which system messages are sent, if present.
     *
     * @return The ID of the channel to which system messages are sent, if present.
     */
    public Optional<Snowflake> getSystemChannelId() {
        return Optional.ofNullable(data.getSystemChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel to which system messages are sent, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} to which system
     * messages are sent, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getSystemChannel() {
        return Mono.justOrEmpty(getSystemChannelId()).flatMap(getClient()::getChannelById).cast(TextChannel.class);
    }

    /**
     * Gets when this guild was joined at, if present.
     *
     * @return When this guild was joined at, if present.
     *
     * @implNote If the underlying {@link discord4j.core.DiscordClientBuilder#getStoreService() store} does not save
     * {@link GuildBean} instances <b>OR</b> the bot is currently not logged in then the returned {@code Optional} will
     * always be empty.
     */
    public Optional<Instant> getJoinTime() {
        return getGatewayData()
                .map(GuildBean::getJoinedAt)
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets whether this guild is considered large, if present.
     *
     * @return If present, {@code true} if the guild is considered large, {@code false} otherwise.
     *
     * @implNote If the underlying {@link discord4j.core.DiscordClientBuilder#getStoreService() store} does not save
     * {@link GuildBean} instances <b>OR</b> the bot is currently not logged in then the returned {@code Optional} will
     * always be empty.
     */
    public Optional<Boolean> isLarge() {
        return getGatewayData().map(GuildBean::getLarge);
    }

    /**
     * Gets the total number of members in the guild, if present.
     *
     * @return The total number of members in the guild, if present.
     *
     * @implNote If the underlying {@link discord4j.core.DiscordClientBuilder#getStoreService() store} does not save
     * {@link GuildBean} instances <b>OR</b> the bot is currently not logged in then the returned {@code Optional} will
     * always be empty.
     */
    public OptionalInt getMemberCount() {
        return getGatewayData()
                .map(guildBean -> OptionalInt.of(guildBean.getMemberCount()))
                .orElseGet(OptionalInt::empty);
    }

    /**
     * Requests to retrieve the voice states of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     *
     * @implNote If the underlying {@link discord4j.core.DiscordClientBuilder#getStoreService() store} does not save
     * {@link VoiceStateBean} instances <b>OR</b> the bot is currently not logged in then the returned {@code Flux} will
     * always be empty.
     */
    public Flux<VoiceState> getVoiceStates() {
        return serviceMediator.getStateHolder().getVoiceStateStore()
                .findInRange(LongLongTuple2.of(getId().asLong(), Long.MIN_VALUE),
                             LongLongTuple2.of(getId().asLong(), Long.MAX_VALUE))
                .map(bean -> new VoiceState(serviceMediator, bean));
    }

    /**
     * Requests to retrieve the members of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link Member members} of the guild. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    public Flux<Member> getMembers() {
        Function<Map<String, Object>, Flux<GuildMemberResponse>> doRequest = params ->
                serviceMediator.getRestClient().getGuildService().getGuildMembers(getId().asLong(), params);

        Flux<Member> requestMembers =
                PaginationUtil.paginateAfter(doRequest, response -> response.getUser().getId(), 0, 100)
                        .map(response -> Tuples.of(new MemberBean(response), new UserBean(response.getUser())))
                        .map(tuple -> new Member(serviceMediator, tuple.getT1(), tuple.getT2(), getId().asLong()));

        return Mono.justOrEmpty(getGatewayData())
                .map(GuildBean::getMembers)
                .map(Arrays::stream)
                .map(LongStream::boxed)
                .flatMapMany(Flux::fromStream)
                .map(Snowflake::of)
                .flatMap(memberId -> getClient().getMemberById(getId(), memberId))
                .switchIfEmpty(requestMembers);
    }

    /**
     * Requests to retrieve the member as represented by the supplied ID.
     *
     * @param id The ID of the member.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMemberById(final Snowflake id) {
        return getClient().getMemberById(getId(), id);
    }

    /**
     * Requests to retrieve the channels of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link GuildChannel channels} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<GuildChannel> getChannels() {
        return Mono.justOrEmpty(getGatewayData())
                .map(GuildBean::getChannels)
                .map(Arrays::stream)
                .map(LongStream::boxed)
                .flatMapMany(Flux::fromStream)
                .map(Snowflake::of)
                .flatMap(getClient()::getChannelById)
                .cast(GuildChannel.class)
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuildChannels(getId().asLong())
                        .map(EntityUtil::getChannelBean)
                        .map(bean -> EntityUtil.getChannel(serviceMediator, bean))
                        .cast(GuildChannel.class))
                .sort(Comparator.comparing(GuildChannel::getRawPosition).thenComparing(GuildChannel::getId));
    }

    /**
     * Requests to retrieve the channel as represented by the supplied ID.
     *
     * @param id The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getChannelById(final Snowflake id) {
        return getClient().getChannelById(id)
                .cast(GuildChannel.class)
                .filter(channel -> channel.getGuildId().equals(getId()));
    }

    /**
     * Requests to retrieve the presences of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link Presence presences} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     *
     * @implNote If the underlying {@link discord4j.core.DiscordClientBuilder#getStoreService() store} does not save
     * {@link PresenceBean} instances <b>OR</b> the bot is currently not logged in then the returned {@code Flux} will
     * always be empty.
     */
    public Flux<Presence> getPresences() {
        return serviceMediator.getStateHolder().getPresenceStore()
                .findInRange(LongLongTuple2.of(getId().asLong(), Long.MIN_VALUE),
                             LongLongTuple2.of(getId().asLong(), Long.MAX_VALUE))
                .map(Presence::new);
    }

    /**
     * Requests to edit this guild.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildEditSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #edit(GuildEditSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> edit(final Consumer<GuildEditSpec> spec) {
        final GuildEditSpec mutatedSpec = new GuildEditSpec();
        spec.accept(mutatedSpec);
        return edit(mutatedSpec);
    }

    /**
     * Requests to edit this guild.
     *
     * @param spec A configured {@link GuildEditSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> edit(final GuildEditSpec spec) {
        return serviceMediator.getRestClient().getGuildService()
                .modifyGuild(getId().asLong(), spec.asRequest())
                .map(BaseGuildBean::new)
                .map(bean -> new Guild(serviceMediator, bean));
    }

    /**
     * Requests to create an emoji.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildEmojiCreateSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #createEmoji(GuildEmojiCreateSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> createEmoji(final Consumer<GuildEmojiCreateSpec> spec) {
        final GuildEmojiCreateSpec mutatedSpec = new GuildEmojiCreateSpec();
        spec.accept(mutatedSpec);
        return createEmoji(mutatedSpec);
    }

    /**
     * Requests to create an emoji.
     *
     * @param spec A configured {@link GuildEmojiCreateSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link GuildEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> createEmoji(final GuildEmojiCreateSpec spec) {
        return serviceMediator.getRestClient().getEmojiService()
                .createGuildEmoji(getId().asLong(), spec.asRequest())
                .map(GuildEmojiBean::new)
                .map(bean -> new GuildEmoji(serviceMediator, bean, getId().asLong()));
    }

    /**
     * Requests to create a role.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link RoleCreateSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #createRole(RoleCreateSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Role}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> createRole(final Consumer<RoleCreateSpec> spec) {
        final RoleCreateSpec mutatedSpec = new RoleCreateSpec();
        spec.accept(mutatedSpec);
        return createRole(mutatedSpec);
    }

    /**
     * Requests to create a role.
     *
     * @param spec A configured {@link RoleCreateSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Role}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> createRole(final RoleCreateSpec spec) {
        return serviceMediator.getRestClient().getGuildService()
                .createGuildRole(getId().asLong(), spec.asRequest())
                .map(RoleBean::new)
                .map(bean -> new Role(serviceMediator, bean, getId().asLong()));
    }

    /**
     * Requests to create a category.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link CategoryCreateSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #createCategory(CategoryCreateSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Category}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Category> createCategory(final Consumer<CategoryCreateSpec> spec) {
        final CategoryCreateSpec mutatedSpec = new CategoryCreateSpec();
        spec.accept(mutatedSpec);
        return createCategory(mutatedSpec);
    }

    /**
     * Requests to create a category.
     *
     * @param spec A configured {@link CategoryCreateSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Category}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Category> createCategory(final CategoryCreateSpec spec) {
        return serviceMediator.getRestClient().getGuildService()
                .createGuildChannel(getId().asLong(), spec.asRequest())
                .map(EntityUtil::getChannelBean)
                .map(bean -> EntityUtil.getChannel(serviceMediator, bean))
                .cast(Category.class);
    }

    /**
     * Requests to create a text channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link TextChannelCreateSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #createTextChannel(TextChannelCreateSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> createTextChannel(final Consumer<TextChannelCreateSpec> spec) {
        final TextChannelCreateSpec mutatedSpec = new TextChannelCreateSpec();
        spec.accept(mutatedSpec);
        return createTextChannel(mutatedSpec);
    }

    /**
     * Requests to create a text channel.
     *
     * @param spec A configured {@link TextChannelCreateSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> createTextChannel(final TextChannelCreateSpec spec) {
        return serviceMediator.getRestClient().getGuildService()
                .createGuildChannel(getId().asLong(), spec.asRequest())
                .map(EntityUtil::getChannelBean)
                .map(bean -> EntityUtil.getChannel(serviceMediator, bean))
                .cast(TextChannel.class);
    }

    /**
     * Requests to create a voice channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link VoiceChannelCreateSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #createVoiceChannel(VoiceChannelCreateSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> createVoiceChannel(final Consumer<VoiceChannelCreateSpec> spec) {
        final VoiceChannelCreateSpec mutatedSpec = new VoiceChannelCreateSpec();
        spec.accept(mutatedSpec);
        return createVoiceChannel(mutatedSpec);
    }

    /**
     * Requests to create a voice channel.
     *
     * @param spec A configured {@link VoiceChannelCreateSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> createVoiceChannel(final VoiceChannelCreateSpec spec) {
        return serviceMediator.getRestClient().getGuildService()
                .createGuildChannel(getId().asLong(), spec.asRequest())
                .map(EntityUtil::getChannelBean)
                .map(bean -> EntityUtil.getChannel(serviceMediator, bean))
                .cast(VoiceChannel.class);
    }

    /**
     * Requests to delete this guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the guild has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return serviceMediator.getRestClient().getGuildService().deleteGuild(getId().asLong());
    }

    /**
     * Requests to kick the specified user from this guild.
     *
     * @param userId The ID of the user to kick from this guild.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was kicked
     * from this guild. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> kick(final Snowflake userId) {
        return serviceMediator.getRestClient().getGuildService().removeGuildMember(getId().asLong(), userId.asLong());
    }

    /**
     * Requests to retrieve all the bans for this guild.
     *
     * @return A {@link Flux} that continually emits the {@link Ban bans} for this guild. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<Ban> getBans() {
        return serviceMediator.getRestClient().getGuildService()
                .getGuildBans(getId().asLong())
                .map(BanBean::new)
                .map(bean -> new Ban(serviceMediator, bean));
    }

    /**
     * Requests to retrieve the ban for the specified user for this guild.
     *
     * @param userId The ID of the user to retrieve the ban for this guild.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Ban ban} for the specified user for
     * this guild. If an error is received, it is meitted through the {@code Mono}.
     */
    public Mono<Ban> getBan(final Snowflake userId) {
        return serviceMediator.getRestClient().getGuildService()
                .getGuildBan(getId().asLong(), userId.asLong())
                .map(BanBean::new)
                .map(bean -> new Ban(serviceMediator, bean));
    }

    /**
     * Requests to ban the specified user.
     *
     * @param userId The ID of the user to ban.
     * @param spec A {@link Consumer} that provides a "blank" {@link BanQuerySpec} to be operated on. If some properties
     * need to be retrieved via blocking operations (such as retrieval from a database), then it is recommended to build
     * the spec externally and call {@link #ban(Snowflake, BanQuerySpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was
     * banned. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> ban(final Snowflake userId, final Consumer<BanQuerySpec> spec) {
        final BanQuerySpec mutatedSpec = new BanQuerySpec();
        spec.accept(mutatedSpec);
        return ban(userId, mutatedSpec);
    }

    /**
     * Requests to ban the specified user.
     *
     * @param userId The ID of the user to ban.
     * @param spec A configured {@link BanQuerySpec} to perform the request on.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was
     * banned. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> ban(final Snowflake userId, final BanQuerySpec spec) {
        return serviceMediator.getRestClient().getGuildService()
                .createGuildBan(getId().asLong(), userId.asLong(), spec.asRequest());
    }

    /**
     * Requests to unban the specified user.
     *
     * @param userId The ID of the user to unban.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the specified user was
     * unbanned. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> unban(final Snowflake userId) {
        return serviceMediator.getRestClient().getGuildService().removeGuildBan(getId().asLong(), userId.asLong());
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

        return serviceMediator.getRestClient().getGuildService()
                .getGuildPruneCount(getId().asLong(), queryParams)
                .map(PruneResponse::getPruned);
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
        final Map<String, Object> queryParams = new HashMap<>(1);
        queryParams.put("days", days);

        return serviceMediator.getRestClient().getGuildService()
                .beginGuildPrune(getId().asLong(), queryParams)
                .map(PruneResponse::getPruned);
    }

    /**
     * Requests to leave this guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this guild has been left. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> leave() {
        return serviceMediator.getRestClient().getUserService().leaveGuild(getId().asLong());
    }

    /**
     * Requests to retrieve the audit log for this guild.
     *
     * @return A {@link Flux} that continually emits entries for this guild's audit log. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<AuditLogEntry> getAuditLog() {
        return getAuditLog(new AuditLogQuerySpec());
    }

    /**
     * Requests to retrieve the audit log for this guild.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link AuditLogQuerySpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #getAuditLog(AuditLogQuerySpec)}.
     *
     * @return A {@link Flux} that continually emits entries for this guild's audit log. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<AuditLogEntry> getAuditLog(final Consumer<AuditLogQuerySpec> spec) {
        final AuditLogQuerySpec mutatedSpec = new AuditLogQuerySpec();
        spec.accept(mutatedSpec);
        return getAuditLog(mutatedSpec);
    }

    /**
     * Requests to retrieve the audit log for this guild.
     *
     * @param spec A configured {@link AuditLogQuerySpec} to perform the request on.
     * @return A {@link Flux} that continually emits entries for this guild's audit log. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<AuditLogEntry> getAuditLog(final AuditLogQuerySpec spec) {
        Function<Map<String, Object>, Flux<AuditLogResponse>> makeRequest = params -> {
            params.putAll(spec.asRequest());
            return serviceMediator.getRestClient().getAuditLogService().getAuditLog(getId().asLong(), params).flux();
        };

        ToLongFunction<AuditLogResponse> getLastEntryId = response -> {
            AuditLogEntryResponse[] entries = response.getAuditLogEntries();
            if (entries.length == 0) {
                return Long.MAX_VALUE;
            } else {
                return entries[entries.length - 1].getId();
            }
        };

        return PaginationUtil.paginateBefore(makeRequest, getLastEntryId, Long.MAX_VALUE, 100)
                .flatMap(log -> Flux.fromArray(log.getAuditLogEntries())
                        .map(AuditLogEntryBean::new)
                        .map(bean -> new AuditLogEntry(serviceMediator, bean)));
    }

    /**
     * Requests to retrieve the webhooks of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link Webhook webhooks} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<Webhook> getWebhooks() {
        return serviceMediator.getRestClient().getWebhookService()
                .getGuildWebhooks(getId().asLong())
                .map(WebhookBean::new)
                .map(bean -> new Webhook(serviceMediator, bean));
    }

    /**
     * Requests to retrieve the invites of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link ExtendedInvite invites} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<ExtendedInvite> getInvites() {
        return serviceMediator.getRestClient().getGuildService()
                .getGuildInvites(getId().asLong())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(serviceMediator, bean));
    }

    /**
     * Requests to change the bot user's nickname in this guild.
     *
     * @param nickname The new nickname for the bot user in this guild, or {@code null} to remove it.
     * @return A {@link Mono} where, upon successful completion, emits the bot user's new nickname in this guild. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<String> changeSelfNickname(@Nullable final String nickname) {
        return serviceMediator.getRestClient().getGuildService()
                .modifyOwnNickname(getId().asLong(), new NicknameModifyRequest(nickname))
                .map(NicknameModifyResponse::getNick);
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
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }

    /**
     * Prevent potentially dangerous administrative actions for users without two-factor authentication enabled. This
     * setting can only be changed by the server owner if they have 2FA enabled on their account.
     */
    public enum MfaLevel {

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
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }

    /**
     * Determines whether {@link Member Members} who have not explicitly set their notification settings receive a
     * notification for every message sent in the server or not.
     */
    public enum NotificationLevel {

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
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }

    /**
     * {@link Member Members} of the server must meet the following criteria before they can send messages in text
     * channels or initiate a direct message conversation. If a member has an assigned role this does not apply.
     */
    public enum VerificationLevel {

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
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }

    @Override
    public String toString() {
        return "Guild{" +
                "data=" + data +
                '}';
    }
}
