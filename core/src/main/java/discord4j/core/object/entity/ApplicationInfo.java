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
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ApplicationInstallParams;
import discord4j.core.object.ApplicationIntegrationTypeConfiguration;
import discord4j.core.object.ApplicationRoleConnectionMetadata;
import discord4j.core.object.command.ApplicationIntegrationType;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.ApplicationEditMono;
import discord4j.core.spec.ApplicationEditSpec;
import discord4j.core.spec.ApplicationEmojiCreateMono;
import discord4j.core.spec.ApplicationEmojiCreateSpec;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import discord4j.discordjson.json.ApplicationEmojiDataList;
import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.discordjson.json.ApplicationRoleConnectionMetadataData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the Current (typically) Application Information.
 *
 * @see <a href="https://discord.com/developers/docs/resources/application">Application Resource</a>
 */
public final class ApplicationInfo implements Entity {

    /** The path for application icon image URLs. */
    private static final String ICON_IMAGE_PATH = "app-icons/%s/%s";

    /** The path for the store URL. */
    private static final String STORE_URL_SCHEME = "https://discord.com/application-directory/%s/store";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationInfoData data;

    /**
     * Constructs a {@code ApplicationInfo} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationInfo(final GatewayDiscordClient gateway, final ApplicationInfoData data) {
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
     * Gets the data of the app.
     *
     * @return The data of the app.
     */
    public ApplicationInfoData getData() {
        return data;
    }

    /**
     * Gets the name of the app.
     *
     * @return The name of the app.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the icon URL of the application, if present.
     *
     * @param format The format for the URL.
     * @return The icon URL of the application, if present.
     */
    public Optional<String> getIconUrl(final Image.Format format) {
        return data.icon()
                .map(icon -> ImageUtil.getUrl(String.format(ICON_IMAGE_PATH, getId().asString(), icon), format));
    }

    /**
     * Gets the icon of the application.
     *
     * @param format The format in which to get the icon.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image icon} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getIcon(final Image.Format format) {
        return Mono.justOrEmpty(getIconUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the cover image URL of the application, if present.
     *
     * @param format The format for the URL.
     * @return The icon URL of the application, if present.
     */
    public Optional<String> getCoverImageUrl(final Image.Format format) {
        return data.coverImage().toOptional()
            .map(icon -> ImageUtil.getUrl(String.format(ICON_IMAGE_PATH, getId().asString(), icon), format));
    }

    /**
     * Gets the cover image of the application.
     *
     * @param format The format in which to get the icon.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image cover image} of the application.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getCoverImage(final Image.Format format) {
        return Mono.justOrEmpty(getCoverImageUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the description of the app.
     *
     * @return The description of the app.
     */
    public String getDescription() {
        return data.description();
    }

    /**
     * Gets the list of RPC origin URLs, if RPC is enabled
     *
     * @return A {@link List} of RPC origin URLs, if RPC is enabled
     */
    public List<String> getRpcOrigins() {
        return data.rpcOrigins().toOptional().orElse(Collections.emptyList());
    }

    /**
     * Gets whether only the app owner can join the app's bot to guilds.
     *
     * @return {@code true} if only the app owner can join the app's bot to guilds, {@code false} otherwise.
     * @deprecated Use {@link #isBotPublic()} instead.
     */
    @Deprecated
    public boolean isPublic() {
        return data.botPublic();
    }

    /**
     * Gets whether only the app owner can join the app's bot to guilds.
     *
     * @return {@code true} if only the app owner can join the app's bot to guilds, {@code false} otherwise.
     */
    public boolean isBotPublic() {
        return data.botPublic();
    }

    /**
     * Gets whether the app's bot will only join upon completion of the full OAuth2 code grant flow.
     *
     * @return {@code true} if the app's bot will only join upon completion of the full OAuth2 code grant flow,
     * {@code false} otherwise.
     * @deprecated Use {@link #botRequiresCodeGrant()} instead.
     */
    @Deprecated
    public boolean requireCodeGrant() {
        return data.botRequireCodeGrant();
    }

    /**
     * Gets whether the app's bot will only join upon completion of the full OAuth2 code grant flow.
     *
     * @return {@code true} if the app's bot will only join upon completion of the full OAuth2 code grant flow,
     * {@code false} otherwise.
     */
    public boolean botRequiresCodeGrant() {
        return data.botRequireCodeGrant();
    }

    /**
     * Gets the url of the app's terms of service, if present.
     *
     * @return The url of the app's terms of service, if present.
     */
    public Optional<String> getTermsOfServiceUrl() {
        return data.termsOfServiceUrl().toOptional();
    }

    /**
     * Gets the url of the app's privacy policy, if present.
     *
     * @return The url of the app's privacy policy, if present.
     */
    public Optional<String> getPrivacyPolicyUrl() {
        return data.privacyPolicyUrl().toOptional();
    }

    /**
     * Gets the ID of the owner of the application.
     *
     * @return The ID of the owner of the application.
     */
    public Optional<Snowflake> getOwnerId() {
        return data.owner().toOptional().map(data -> Snowflake.of(data.id()));
    }

    /**
     * Requests to retrieve the owner of the application.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User owner} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getOwner() {
        return getOwnerId().map(gateway::getUserById).orElseGet(Mono::empty);
    }

    /**
     * Requests to retrieve the owner of the application, using the given retrieval strategy.
     *
     * @param retrievalStrategy The strategy to use to get the owner.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User owner} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getOwner(EntityRetrievalStrategy retrievalStrategy) {
        return getOwnerId().map(gateway.withRetrievalStrategy(retrievalStrategy)::getUserById).orElseGet(Mono::empty);
    }

    /**
     * Gets the members of the application team, if the application belongs to a team.
     *
     * @return The members of the application's team, if the application belongs to a team.
     */
    public Optional<ApplicationTeam> getTeam() {
        return data.team().map(data -> new ApplicationTeam(gateway, data));
    }

    /**
     * Returns the public flags of this {@link ApplicationInfo}.
     *
     * @return A {@code EnumSet} with the public flags of this application.
     */
    public EnumSet<Flag> getFlags() {
        int publicFlags = data.flags().toOptional().orElse(0);
        if (publicFlags != 0) {
            return Flag.of(publicFlags);
        }
        return EnumSet.noneOf(Flag.class);
    }

    /**
     * Get the store URL for the current application.
     *
     * @return the store url for the current application
     */
    public String getStoreUrl() {
        return String.format(ApplicationInfo.STORE_URL_SCHEME, getId().asString());
    }

    /**
     * Hex encoded key for verification in interactions and the GameSDK's GetTicket
     *
     * @return The verify key
     */
    public String getVerifyKey() {
        return data.verifyKey();
    }

    /**
     * Returns the bot's id associated with this application, if present
     *
     * @return An {@link Optional} containing the bot's id or empty if the bot is not present
     */
    public Optional<Snowflake> getBotId() {
        return data.bot().toOptional().map(bot -> Snowflake.of(bot.id()));
    }

    /**
     * Returns the bot's user associated with this application, if present
     *
     * @return A {@link Mono} where, upon successful completion, emits the bot's {@link User}
     * associated with this application. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getBot() {
        return data.bot().toOptional().map(bot -> gateway.getUserById(Snowflake.of(bot.id()))).orElseGet(Mono::empty);
    }

    /**
     * Returns the guild id associated with the app. For example, a developer support server.
     *
     * @return An {@link Optional} containing the guild id or empty if the guild is not present
     */
    public Optional<Snowflake> getGuildId() {
        return data.guildId().toOptional().map(Snowflake::of);
    }

    /**
     * Returns the guild associated with the app. For example, a developer support server.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} associated with the app.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return data.guildId().toOptional()
            .map(Snowflake::of)
            .map(gateway::getGuildById)
            .orElseGet(Mono::empty);
    }

    /**
     * Returns the primary SKU id of the app. If this app is a game sold on Discord, this field will be the id of
     * the "Game SKU" that is created, if exists
     *
     * @return An {@link Optional} containing the primary SKU id or empty if the primary SKU id is not present
     */
    public Optional<Snowflake> getPrimarySkuId() {
        return data.primarySkuId().toOptional().map(Snowflake::of);
    }

    /**
     * If this app is a game sold on Discord, this field will be the URL slug that links to the store page
     *
     * @return An {@link Optional} containing the slug or empty if the slug is not present
     */
    public Optional<String> getSlug() {
        return data.slug().toOptional();
    }

    /**
     * Returns the approximate count of guilds the app has been added to, if present
     *
     * @return An {@link Optional} containing the approximate count of guilds the app has been added to, if present
     */
    public Optional<Integer> getApproximateGuildCount() {
        return data.approximateGuildCount().toOptional();
    }

    /**
     * Returns the list of redirect URIs for the app
     *
     * @return A {@link List} of redirect URIs for the app
     */
    public List<String> getRedirectUris() {
        return data.redirectUris().toOptional().orElse(Collections.emptyList());
    }

    /**
     * Returns the interactions endpoint URL for the app if present
     *
     * @return An {@link Optional} containing the interactions endpoint URL for the app if present
     */
    public Optional<String> getInteractionsEndpointUrl() {
        return Possible.flatOpt(data.interactionsEndpointUrl());
    }

    /**
     * Returns the role connection verification URL for the app
     *
     * @return An {@link Optional} containing the role connection verification URL for the app
     */
    public Optional<String> getRoleConnectionsVerificationUrl() {
        return Possible.flatOpt(data.roleConnectionsVerificationUrl());
    }

    /**
     * Returns the of tags describing the content and functionality of the app
     *
     * @return A {@link List} of tags of the app
     */
    public List<String> getTags() {
        return data.tags().toOptional().orElse(Collections.emptyList());
    }

    /**
     * Returns the settings for the app's default in-app authorization link, if enabled
     *
     * @return An {@link Optional} containing the settings for the app's default in-app authorization link, if enabled
     */
    public Optional<ApplicationInstallParams> getInstallParams() {
        return data.installParams().toOptional()
            .map(ApplicationInstallParams::new);
    }

    /**
     * Returns the default scopes and permissions for each supported installation context.
     *
     * @return A map of {@link ApplicationIntegrationType} to {@link ApplicationIntegrationTypeConfiguration}
     */
    @Experimental // In preview
    public Map<ApplicationIntegrationType, ApplicationIntegrationTypeConfiguration> getIntegrationTypesConfig() {
        return data.integrationTypesConfig().toOptional()
            .map(map -> {
                Map<ApplicationIntegrationType, ApplicationIntegrationTypeConfiguration> integrationTypesConfig = new HashMap<>();
                map.forEach((key, value) -> integrationTypesConfig.put(ApplicationIntegrationType.of(key), new ApplicationIntegrationTypeConfiguration(value)));
                return integrationTypesConfig;
            })
            .orElse(Collections.emptyMap());
    }

    /**
     * Returns the default custom authorization URL for the app, if enabled
     *
     * @return An {@link Optional} containing the default custom authorization URL for the app, if enabled
     */
    public Optional<String> getCustomInstallUrl() {
        return data.customInstallUrl().toOptional();
    }

    /**
     * Request the {@link ApplicationRoleConnectionMetadata} for this application.
     *
     * @return A {@link Flux} where, upon successful completion, emits the {@link ApplicationRoleConnectionMetadata} for this application.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<ApplicationRoleConnectionMetadata> getRoleConnectionMetadata() {
        return this.gateway.rest()
            .getApplicationService()
            .getApplicationRoleConnectionMetadata(this.getId().asLong())
            .map(ApplicationRoleConnectionMetadata::new);
    }

    /**
     * Request to set the new {@link ApplicationRoleConnectionMetadata} for this application.
     *
     * @param metadata The new metadata to set.
     * @return A {@link Flux} where, upon successful completion, emits the new {@link ApplicationRoleConnectionMetadata}
     * for this application. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<ApplicationRoleConnectionMetadata> setRoleConnectionMetadata(List<ApplicationRoleConnectionMetadataData> metadata) {
        return this.gateway.rest()
            .getApplicationService()
            .modifyApplicationRoleConnectionMetadata(this.getId().asLong(), metadata)
            .map(ApplicationRoleConnectionMetadata::new);
    }

    /**
     * Requests to retrieve the application's emojis.
     *
     * @return A {@link Flux} that continually emits guild's {@link ApplicationEmoji emojis}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<ApplicationEmoji> getEmojis() {
        return gateway.rest()
            .getEmojiService()
            .getApplicationEmojis(this.getId().asLong())
            .flatMapIterable(ApplicationEmojiDataList::items) // the response include a items field with all the emojis
            .map(emojiData -> new ApplicationEmoji(gateway, emojiData, getId().asLong()));
    }

    /**
     * Requests to retrieve the guild emoji as represented by the supplied ID.
     *
     * @param id The ID of the guild emoji.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationEmoji> getEmojiById(final Snowflake id) {
        return gateway.rest()
            .getEmojiService()
            .getApplicationEmoji(getId().asLong(), id.asLong())
            .map(emojiData -> new ApplicationEmoji(gateway, emojiData, id.asLong()));
    }

    /**
     * Requests to create an emoji. Properties specifying how to create an emoji can be set via the {@code withXxx}
     * methods of the returned {@link ApplicationEmojiCreateMono}.
     *
     * @param name the name of the emoji to create
     * @param image the image of the emoji to create
     * @return A {@link ApplicationEmojiCreateMono} where, upon successful completion, emits the created {@link ApplicationEmoji}.
     * If an error is received, it is emitted through the {@code ApplicationEmojiCreateMono}.
     */
    public ApplicationEmojiCreateMono createEmoji(String name, Image image) {
        return ApplicationEmojiCreateMono.of(name, image, this);
    }

    /**
     * Requests to create an emoji.
     *
     * @param spec an immutable object that specifies how to create the emoji
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ApplicationEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationEmoji> createEmoji(ApplicationEmojiCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> gateway.getRestClient().getEmojiService()
                    .createApplicationEmoji(getId().asLong(), spec.asRequest()))
            .map(data -> new ApplicationEmoji(gateway, data, getId().asLong()));
    }

    /**
     * Requests to edit the current application. Properties specifying how to edit this application
     * can be set via the {@code withXxx} methods of the returned {@link ApplicationEditMono}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link ApplicationInfo}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public ApplicationEditMono edit() {
        return ApplicationEditMono.of(this);
    }

    /**
     * Requests to edit this application.
     *
     * @param spec an immutable object that specifies how to edit this application
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link ApplicationInfo}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationInfo> edit(ApplicationEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
            () -> gateway.getRestClient().getApplicationService().modifyCurrentApplicationInfo(spec.asRequest()))
            .map(data -> new ApplicationInfo(gateway, ApplicationInfoData.builder()
                .from(this.data)
                .from(data)
                .build()));
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    /**
     * Describes the flags of an application.
     *
     * @see
     * <a href="https://discord.com/developers/docs/resources/application#application-object-application-flags">Discord</a>
     */
    public enum Flag {
        APPLICATION_AUTO_MODERATION_RULE_CREATE_BADGE(6),
        GATEWAY_PRESENCE(12),
        GATEWAY_PRESENCE_LIMITED(13),
        GATEWAY_GUILD_MEMBERS(14),
        GATEWAY_GUILD_MEMBERS_LIMITED(15),
        VERIFICATION_PENDING_GUILD_LIMIT(16),
        EMBEDDED(17),
        GATEWAY_MESSAGE_CONTENT(18),
        GATEWAY_MESSAGE_CONTENT_LIMITED(19),
        APPLICATION_COMMAND_BADGE(23);

        /** The underlying value as represented by Discord. */
        private final int value;

        /** The flag value as represented by Discord. */
        private final int flag;

        /**
         * Constructs a {@code ApplicationInfo.Flag}.
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
         * Gets the flags of an application. It is guaranteed that invoking {@link #getValue()} from the returned enum
         * will be equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<Flag> of(final int value) {
            final EnumSet<Flag> flagSet = EnumSet.noneOf(Flag.class);
            for (Flag flag : Flag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    flagSet.add(flag);
                }
            }
            return flagSet;
        }
    }

    @Override
    public String toString() {
        return "ApplicationInfo{" +
                "data=" + data +
                '}';
    }
}
