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
package discord4j.core.object.data.stored;

import discord4j.common.json.GuildEmojiResponse;
import discord4j.common.json.RoleResponse;
import discord4j.gateway.json.dispatch.GuildCreate;
import discord4j.gateway.json.dispatch.GuildUpdate;
import discord4j.rest.json.response.GuildResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;

public class BaseGuildBean implements Serializable {

    private static final long serialVersionUID = -133099254066154899L;

    private long id;
    private String name;
    @Nullable
    private String icon;
    @Nullable
    private String splash;
    @Nullable
    private String banner;
    private long ownerId;
    private String region;
    @Nullable
    private Long afkChannelId;
    private int afkTimeout;
    @Nullable
    private Long embedChannelId;
    private int premiumTier;
    private int premiumSubscriptionsCount;
    private String preferredLocale;
    private int verificationLevel;
    private int defaultMessageNotifications;
    private int explicitContentFilter;
    private long[] roles;
    private long[] emojis;
    private String[] features;
    private int mfaLevel;
    @Nullable
    private Long applicationId;
    @Nullable
    private Long widgetChannelId;
    @Nullable
    private Long systemChannelId;
    @Nullable
    private String vanityUrlCode;
    @Nullable
    private String description;

    public BaseGuildBean(final GuildCreate guildCreate) {
        id = guildCreate.getId();
        name = guildCreate.getName();
        icon = guildCreate.getIcon();
        splash = guildCreate.getSplash();
        banner = guildCreate.getBanner();
        ownerId = guildCreate.getOwnerId();
        region = guildCreate.getRegion();
        afkChannelId = guildCreate.getAfkChannelId();
        afkTimeout = guildCreate.getAfkTimeout();
        embedChannelId = guildCreate.getEmbedChannelId();
        verificationLevel = guildCreate.getVerificationLevel();
        premiumTier = guildCreate.getPremiumTier();
        preferredLocale = guildCreate.getPreferredLocale();

        defaultMessageNotifications = guildCreate.getDefaultMessageNotifications();
        explicitContentFilter = guildCreate.getExplicitContentFilter();

        roles = Arrays.stream(guildCreate.getRoles())
                .mapToLong(RoleResponse::getId)
                .toArray();

        emojis = Arrays.stream(guildCreate.getEmojis())
                .mapToLong(GuildEmojiResponse::getId)
                .toArray();

        features = guildCreate.getFeatures();
        mfaLevel = guildCreate.getMfaLevel();
        applicationId = guildCreate.getApplicationId();
        widgetChannelId = guildCreate.getWidgetChannelId();
        systemChannelId = guildCreate.getSystemChannelId();
        vanityUrlCode = guildCreate.getVanityUrlCode();
        description = guildCreate.getDescription();
    }

    public BaseGuildBean(final GuildUpdate guildUpdate) {
        id = guildUpdate.getId();
        name = guildUpdate.getName();
        icon = guildUpdate.getIcon();
        splash = guildUpdate.getSplash();
        banner = guildUpdate.getBanner();
        ownerId = guildUpdate.getOwnerId();
        region = guildUpdate.getRegion();
        afkChannelId = guildUpdate.getAfkChannelId();
        afkTimeout = guildUpdate.getAfkTimeout();
        embedChannelId = guildUpdate.getEmbedChannelId();
        verificationLevel = guildUpdate.getVerificationLevel();
        premiumTier = guildUpdate.getPremiumTier();
        preferredLocale = guildUpdate.getPreferredLocale();
        defaultMessageNotifications = guildUpdate.getDefaultMessageNotifications();
        explicitContentFilter = guildUpdate.getExplicitContentFilter();

        roles = Arrays.stream(guildUpdate.getRoles())
                .mapToLong(RoleResponse::getId)
                .toArray();

        emojis = Arrays.stream(guildUpdate.getEmojis())
                .mapToLong(GuildEmojiResponse::getId)
                .toArray();

        features = guildUpdate.getFeatures();
        mfaLevel = guildUpdate.getMfaLevel();
        applicationId = guildUpdate.getApplicationId();
        widgetChannelId = guildUpdate.getWidgetChannelId();
        systemChannelId = guildUpdate.getSystemChannelId();
        vanityUrlCode = guildUpdate.getVanityUrlCode();
        description = guildUpdate.getDescription();
    }

    public BaseGuildBean(final GuildResponse response) {
        id = response.getId();
        name = response.getName();
        icon = response.getIcon();
        splash = response.getSplash();
        banner = response.getBanner();
        ownerId = response.getOwnerId();
        region = response.getRegion();
        afkChannelId = response.getAfkChannelId();
        afkTimeout = response.getAfkTimeout();
        embedChannelId = response.getEmbedChannelId();
        verificationLevel = response.getVerificationLevel();
        premiumTier = response.getPremiumTier();
        preferredLocale = response.getPreferredLocale();
        defaultMessageNotifications = response.getDefaultMessageNotifications();
        explicitContentFilter = response.getExplicitContentFilter();

        roles = Arrays.stream(response.getRoles())
                .mapToLong(RoleResponse::getId)
                .toArray();

        emojis = Arrays.stream(response.getEmojis())
                .mapToLong(GuildEmojiResponse::getId)
                .toArray();

        features = response.getFeatures();
        mfaLevel = response.getMfaLevel();
        applicationId = response.getApplicationId();
        widgetChannelId = response.getWidgetChannelId();
        systemChannelId = response.getSystemChannelId();
        vanityUrlCode = response.getVanityUrlCode();
        description = response.getDescription();
    }

    public BaseGuildBean(final BaseGuildBean toCopy) {
        id = toCopy.getId();
        name = toCopy.getName();
        icon = toCopy.getIcon();
        splash = toCopy.getSplash();
        banner = toCopy.getBanner();
        ownerId = toCopy.getOwnerId();
        region = toCopy.getRegion();
        afkChannelId = toCopy.getAfkChannelId();
        afkTimeout = toCopy.getAfkTimeout();
        embedChannelId = toCopy.getEmbedChannelId();
        verificationLevel = toCopy.getVerificationLevel();
        premiumTier = toCopy.getPremiumTier();
        preferredLocale = toCopy.getPreferredLocale();
        defaultMessageNotifications = toCopy.getDefaultMessageNotifications();
        explicitContentFilter = toCopy.getExplicitContentFilter();

        roles = Arrays.copyOf(toCopy.getRoles(), toCopy.getRoles().length);
        emojis = Arrays.copyOf(toCopy.getEmojis(), toCopy.getEmojis().length);

        features = toCopy.getFeatures();
        mfaLevel = toCopy.getMfaLevel();
        applicationId = toCopy.getApplicationId();
        widgetChannelId = toCopy.getWidgetChannelId();
        systemChannelId = toCopy.getSystemChannelId();
        vanityUrlCode = toCopy.getVanityUrlCode();
        description = toCopy.getDescription();
    }

    public BaseGuildBean() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Nullable
    public String getIcon() {
        return icon;
    }

    public void setIcon(@Nullable final String icon) {
        this.icon = icon;
    }

    @Nullable
    public String getSplash() {
        return splash;
    }

    @Nullable
    public String getBanner() {
        return banner;
    }

    public void setSplash(@Nullable final String splash) {
        this.splash = splash;
    }

    public void setBanner(@Nullable final String banner) {
        this.banner = banner;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final long ownerId) {
        this.ownerId = ownerId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    @Nullable
    public Long getAfkChannelId() {
        return afkChannelId;
    }

    public void setAfkChannelId(@Nullable final Long afkChannelId) {
        this.afkChannelId = afkChannelId;
    }

    public int getAfkTimeout() {
        return afkTimeout;
    }

    public void setAfkTimeout(final int afkTimeout) {
        this.afkTimeout = afkTimeout;
    }

    @Nullable
    public Long getEmbedChannelId() {
        return embedChannelId;
    }

    public void setEmbedChannelId(final Long embedChannelId) {
        this.embedChannelId = embedChannelId;
    }

    public int getVerificationLevel() {
        return verificationLevel;
    }

    public void setVerificationLevel(final int verificationLevel) {
        this.verificationLevel = verificationLevel;
    }

    public int getPremiumTier() {
        return premiumTier;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }

    public void setPreferredLocale(final String preferredLocale) {
        this.preferredLocale = preferredLocale;
    }

    public int getDefaultMessageNotifications() {
        return defaultMessageNotifications;
    }

    public void setDefaultMessageNotifications(final int defaultMessageNotifications) {
        this.defaultMessageNotifications = defaultMessageNotifications;
    }

    public int getExplicitContentFilter() {
        return explicitContentFilter;
    }

    public void setExplicitContentFilter(final int explicitContentFilter) {
        this.explicitContentFilter = explicitContentFilter;
    }

    public long[] getRoles() {
        return roles;
    }

    public void setRoles(final long[] roles) {
        this.roles = roles;
    }

    public long[] getEmojis() {
        return emojis;
    }

    public void setEmojis(final long[] emojis) {
        this.emojis = emojis;
    }

    public String[] getFeatures() {
        return features;
    }

    public void setFeatures(final String[] features) {
        this.features = features;
    }

    public int getMfaLevel() {
        return mfaLevel;
    }

    public void setMfaLevel(final int mfaLevel) {
        this.mfaLevel = mfaLevel;
    }

    @Nullable
    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(@Nullable final Long applicationId) {
        this.applicationId = applicationId;
    }

    @Nullable
    public Long getWidgetChannelId() {
        return widgetChannelId;
    }

    public void setWidgetChannelId(@Nullable final Long widgetChannelId) {
        this.widgetChannelId = widgetChannelId;
    }

    @Nullable
    public Long getSystemChannelId() {
        return systemChannelId;
    }

    public void setSystemChannelId(@Nullable final Long systemChannelId) {
        this.systemChannelId = systemChannelId;
    }

    @Nullable
    public String getVanityUrlCode() {
        return vanityUrlCode;
    }

    public void setVanityUrlCode(@Nullable final String vanityUrlCode) {
        this.vanityUrlCode = vanityUrlCode;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "BaseGuildBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", splash='" + splash + '\'' +
                ", banner='" + banner + '\'' +
                ", ownerId=" + ownerId +
                ", region='" + region + '\'' +
                ", afkChannelId=" + afkChannelId +
                ", afkTimeout=" + afkTimeout +
                ", embedChannelId=" + embedChannelId +
                ", premiumTier=" + premiumTier +
                ", preferredLocale=" + preferredLocale +
                ", verificationLevel=" + verificationLevel +
                ", defaultMessageNotifications=" + defaultMessageNotifications +
                ", explicitContentFilter=" + explicitContentFilter +
                ", roles=" + Arrays.toString(roles) +
                ", emojis=" + Arrays.toString(emojis) +
                ", features=" + Arrays.toString(features) +
                ", mfaLevel=" + mfaLevel +
                ", applicationId=" + applicationId +
                ", widgetChannelId=" + widgetChannelId +
                ", systemChannelId=" + systemChannelId +
                ", vanityUrlCode=" + vanityUrlCode +
                ", description=" + description +
                '}';
    }
}
