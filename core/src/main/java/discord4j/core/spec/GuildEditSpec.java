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
package discord4j.core.spec;

import discord4j.core.object.Region;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.GuildModifyRequest;
import discord4j.discordjson.json.ImmutableGuildModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * A spec used to selectively modify properties from a {@link Guild}.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild">Modify Guild</a>
 */
public class GuildEditSpec implements AuditSpec<GuildModifyRequest> {

    private final ImmutableGuildModifyRequest.Builder requestBuilder = GuildModifyRequest.builder();
    @Nullable
    private String reason;

    /**
     * Sets the modified {@link Guild} name.
     *
     * @param name the guild name
     * @return this spec
     */
    public GuildEditSpec setName(String name) {
        requestBuilder.name(name);
        return this;
    }

    /**
     * Sets the voice region for the modified {@link Guild}, automatic if null.
     *
     * @param regionId The voice region for the guild, automatic if null.
     * @return This spec.
     */
    public GuildEditSpec setRegion(@Nullable Region.Id regionId) {
        requestBuilder.region(Possible.of(Optional.ofNullable(regionId).map(Region.Id::getValue)));
        return this;
    }

    /**
     * Sets the voice region for the modified {@link Guild}, automatic if null.
     *
     * @param regionId The voice region for the guild, automatic if null.
     * @return This spec.
     */
    public GuildEditSpec setRegion(@Nullable String regionId) {
        requestBuilder.region(Possible.of(Optional.ofNullable(regionId)));
        return this;
    }

    /**
     * Sets the verification level required before a member can send messages in the modified {@link Guild}.
     *
     * @param verificationLevel The verification level for the guild.
     * @return This spec.
     */
    public GuildEditSpec setVerificationLevel(Guild.VerificationLevel verificationLevel) {
        requestBuilder.verificationLevel(verificationLevel.getValue());
        return this;
    }

    /**
     * Sets the default message notification level for the modified {@link Guild}.
     *
     * @param notificationsLevel The default notification level for the guild.
     * @return This spec.
     */
    public GuildEditSpec setDefaultMessageNotificationsLevel(Guild.NotificationLevel notificationsLevel) {
        requestBuilder.defaultMessageNotifications(notificationsLevel.getValue());
        return this;
    }

    /**
     * Sets the explicit content filter level for the modified {@link Guild}.
     *
     * @param level The explicit content filter level for the guild.
     * @return This spec.
     */
    public GuildEditSpec setExplicitContentFilter(Guild.ContentFilterLevel level) {
        requestBuilder.explicitContentFilter(level.getValue());
        return this;
    }

    /**
     * Sets the {@link Snowflake} identifier for the channel designated as AFK channel in this {@link Guild}.
     *
     * @param afkChannelId The identifier for the AFK channel.
     * @return This spec.
     */
    public GuildEditSpec setAfkChannelId(@Nullable Snowflake afkChannelId) {
        requestBuilder.afkChannelId(Possible.of(Optional.ofNullable(afkChannelId).map(Snowflake::asString)));
        return this;
    }

    /**
     * Sets the AFK timeout, in seconds, for this {@link Guild}.
     *
     * @param afkTimeout The AFK timeout, in seconds.
     * @return This spec.
     */
    public GuildEditSpec setAfkTimeout(int afkTimeout) {
        requestBuilder.afkTimeout(afkTimeout);
        return this;
    }

    /**
     * Sets the image icon to display for the modified {@link Guild}.
     *
     * @param icon The icon for the guild.
     * @return This spec.
     */
    public GuildEditSpec setIcon(@Nullable Image icon) {
        requestBuilder.icon(Possible.of(Optional.ofNullable(icon).map(Image::getDataUri)));
        return this;
    }

    /**
     * Sets the new owner ID for this {@link Guild}. Used to transfer guild ownership if this client is the owner.
     *
     * @param ownerId The identifier for the new guild owner.
     * @return This spec.
     */
    public GuildEditSpec setOwnerId(Snowflake ownerId) {
        requestBuilder.ownerId(ownerId.asString());
        return this;
    }

    /**
     * Sets the image for the guild splash (when the server has the INVITE_SPLASH feature).
     *
     * @param splash The image for the guild splash.
     * @return This spec.
     */
    public GuildEditSpec setSplash(@Nullable Image splash) {
        requestBuilder.splash(Possible.of(Optional.ofNullable(splash).map(Image::getDataUri)));
        return this;
    }

    /**
     * Sets the image for the guild discovery splash (when the server has the DISCOVERABLE feature).
     *
     * @param discoverySplash The image for the guild discovery splash.
     * @return This spec.
     */
    public GuildEditSpec setDiscoverySplash(@Nullable Image discoverySplash) {
        requestBuilder.splash(Possible.of(Optional.ofNullable(discoverySplash).map(Image::getDataUri)));
        return this;
    }

    /**
     * Sets the image for the guild banner (when the server has the BANNER feature).
     *
     * @param banner The image for the guild banner.
     * @return This spec.
     */
    public GuildEditSpec setBanner(@Nullable Image banner) {
        requestBuilder.banner(Possible.of(Optional.ofNullable(banner).map(Image::getDataUri)));
        return this;
    }

    /**
     * Sets the id of the channel where guild notices such as welcome messages and boost events are posted.
     *
     * @param systemChannelId The id of the channel where guild notices such as welcome messages and boost events
     *                        are posted.
     * @return This spec.
     */
    public GuildEditSpec setSystemChannelId(@Nullable Snowflake systemChannelId) {
        requestBuilder.systemChannelId(Possible.of(Optional.ofNullable(systemChannelId).map(Snowflake::asString)));
        return this;
    }

    /**
     * Sets the system channel flags.
     *
     * @param flag The system channel flags.
     * @return This spec.
     */
    public GuildEditSpec setSystemChannelFlags(Guild.SystemChannelFlag flag) {
        requestBuilder.systemChannelFlags(flag.getValue());
        return this;
    }

    /**
     * Sets the id of the channel where Community guilds display rules and/or guidelines.
     *
     * @param rulesChannelId The id of the channel where Community guilds display rules and/or guidelines.
     * @return This spec.
     */
    public GuildEditSpec setRulesChannelId(@Nullable Snowflake rulesChannelId) {
        requestBuilder.rulesChannelId(Possible.of(Optional.ofNullable(rulesChannelId).map(Snowflake::asString)));
        return this;
    }

    /**
     * Sets the id of the channel where admins and moderators of Community guilds receive notices from Discord.
     *
     * @param publicUpdatesChannelId The id of the channel where admins and moderators of Community guilds receive
     *                               notices from Discord.
     * @return This spec.
     */
    public GuildEditSpec setPublicUpdatesChannelId(@Nullable Snowflake publicUpdatesChannelId) {
        requestBuilder.publicUpdatesChannelId(Possible.of(Optional.ofNullable(publicUpdatesChannelId)
            .map(Snowflake::asString)));
        return this;
    }

    /**
     * Sets the preferred locale of a Community guild used in server discovery and notices from Discord;
     * defaults to "en-US".
     *
     * @param preferredLocale The preferred locale of a Community guild used in server discovery and notices from Discord.
     * @return This spec.
     */
    public GuildEditSpec setPreferredLocale(@Nullable Locale preferredLocale) {
        requestBuilder.preferredLocale(Possible.of(Optional.ofNullable(preferredLocale).map(Locale::toLanguageTag)));
        return this;
    }

    /**
     * Sets the enabled guild features.
     * <br>
     * You can see the available
     * <a href="https://discord.com/developers/docs/resources/guild#guild-object-guild-features">guild features</a>
     *
     * @param features Enabled guild features.
     * @return This spec.
     */
    public GuildEditSpec setFeatures(Set<String> features) {
        requestBuilder.features(features);
        return this;
    }

    /**
     * Sets the description for the guild, if the guild is discoverable.
     *
     * @param description The description for the guild.
     * @return This spec.
     */
    public GuildEditSpec setDescription(@Nullable String description) {
        requestBuilder.description(description);
        return this;
    }

    @Override
    public GuildEditSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public GuildModifyRequest asRequest() {
        return requestBuilder.build();
    }
}
