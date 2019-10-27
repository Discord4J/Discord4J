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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.rest.entity.data;

import discord4j.common.json.GuildEmojiResponse;
import discord4j.common.json.RoleResponse;
import discord4j.rest.json.response.GuildResponse;

import java.util.Arrays;

public class GuildData {

    private final long id;
    private final String name;
    private final String icon;
    private final String splash;
    private final String banner;
    private final long ownerId;
    private final String region;
    private final Long afkChannelId;
    private final int afkTimeout;
    private final Long embedChannelId;
    private final int verificationLevel;
    private final int premiumTier;
    private final int defaultMessageNotifications;
    private final int explicitContentFilter;
    private final long[] roles;
    private final long[] emojis;
    private final String[] features;
    private final int mfaLevel;
    private final Long applicationId;
    private final Long widgetChannelId;
    private final Long systemChannelId;

    public GuildData(final GuildResponse response) {
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
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getSplash() {
        return splash;
    }

    public String getBanner() {
        return banner;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getRegion() {
        return region;
    }

    public Long getAfkChannelId() {
        return afkChannelId;
    }

    public int getAfkTimeout() {
        return afkTimeout;
    }

    public Long getEmbedChannelId() {
        return embedChannelId;
    }

    public int getVerificationLevel() {
        return verificationLevel;
    }

    public int getPremiumTier() {
        return premiumTier;
    }

    public int getDefaultMessageNotifications() {
        return defaultMessageNotifications;
    }

    public int getExplicitContentFilter() {
        return explicitContentFilter;
    }

    public long[] getRoles() {
        return roles;
    }

    public long[] getEmojis() {
        return emojis;
    }

    public String[] getFeatures() {
        return features;
    }

    public int getMfaLevel() {
        return mfaLevel;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getWidgetChannelId() {
        return widgetChannelId;
    }

    public Long getSystemChannelId() {
        return systemChannelId;
    }

    @Override
    public String toString() {
        return "GuildData{" +
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
                ", verificationLevel=" + verificationLevel +
                ", premiumTier=" + premiumTier +
                ", defaultMessageNotifications=" + defaultMessageNotifications +
                ", explicitContentFilter=" + explicitContentFilter +
                ", roles=" + Arrays.toString(roles) +
                ", emojis=" + Arrays.toString(emojis) +
                ", features=" + Arrays.toString(features) +
                ", mfaLevel=" + mfaLevel +
                ", applicationId=" + applicationId +
                ", widgetChannelId=" + widgetChannelId +
                ", systemChannelId=" + systemChannelId +
                '}';
    }
}
