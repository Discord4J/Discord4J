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
package discord4j.rest.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.GuildEmojiResponse;
import discord4j.common.json.RoleResponse;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

public class GuildResponse {

    @JsonProperty("mfa_level")
    private int mfaLevel;
    private GuildEmojiResponse[] emojis;
    @JsonProperty("application_id")
    @Nullable
    @UnsignedJson
    private Long applicationId;
    private String name;
    private RoleResponse[] roles;
    @JsonProperty("afk_timeout")
    private int afkTimeout;
    @JsonProperty("system_channel_id")
    @Nullable
    @UnsignedJson
    private Long systemChannelId;
    @JsonProperty("widget_channel_id")
    @Nullable
    @UnsignedJson
    private Long widgetChannelId;
    private String region;
    @JsonProperty("default_message_notifications")
    private int defaultMessageNotifications;
    @JsonProperty("embed_channel_id")
    @Nullable
    @UnsignedJson
    private Long embedChannelId;
    @JsonProperty("explicit_content_filter")
    private int explicitContentFilter;
    private String splash;
    private String banner;
    private String[] features;
    @JsonProperty("afk_channel_id")
    @Nullable
    @UnsignedJson
    private Long afkChannelId;
    @JsonProperty("widget_enabled")
    @Nullable
    private Boolean widgetEnabled;
    @JsonProperty("verification_level")
    private int verificationLevel;
    @JsonProperty("premium_tier")
    private int premiumTier;
    @JsonProperty("premium_subscription_count")
    private int premiumSubcriptionsCount;
    @JsonProperty("preferred_locale")
    private String preferredLocale;
    @JsonProperty("owner_id")
    @UnsignedJson
    private long ownerId;
    @JsonProperty("embed_enabled")
    private boolean embedEnabled;
    @UnsignedJson
    private long id;
    private String icon;
    @JsonProperty("vanity_url_code")
    @Nullable
    private String vanityUrlCode;
    @Nullable
    private String description;
    @JsonProperty("max_presences")
    @Nullable
    private Integer maxPresences;
    @JsonProperty("max_members")
    @Nullable
    private Integer maxMembers;

    public int getMfaLevel() {
        return mfaLevel;
    }

    public GuildEmojiResponse[] getEmojis() {
        return emojis;
    }

    @Nullable
    public Long getApplicationId() {
        return applicationId;
    }

    public String getName() {
        return name;
    }

    public RoleResponse[] getRoles() {
        return roles;
    }

    public int getAfkTimeout() {
        return afkTimeout;
    }

    @Nullable
    public Long getSystemChannelId() {
        return systemChannelId;
    }

    @Nullable
    public Long getWidgetChannelId() {
        return widgetChannelId;
    }

    public String getRegion() {
        return region;
    }

    public int getDefaultMessageNotifications() {
        return defaultMessageNotifications;
    }

    @Nullable
    public Long getEmbedChannelId() {
        return embedChannelId;
    }

    public int getExplicitContentFilter() {
        return explicitContentFilter;
    }

    public String getSplash() {
        return splash;
    }

    public String getBanner() {
        return banner;
    }

    public String[] getFeatures() {
        return features;
    }

    @Nullable
    public Long getAfkChannelId() {
        return afkChannelId;
    }

    @Nullable
    public Boolean isWidgetEnabled() {
        return widgetEnabled;
    }

    public int getPremiumTier() {
        return premiumTier;
    }

    public int getPremiumSubcriptionsCount() {
        return premiumSubcriptionsCount;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }

    public int getVerificationLevel() {
        return verificationLevel;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public boolean isEmbedEnabled() {
        return embedEnabled;
    }

    public long getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    @Nullable
    public String getVanityUrlCode() {
        return vanityUrlCode;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public Integer getMaxPresences() {
        return maxPresences;
    }

    @Nullable
    public Integer getMaxMembers() {
        return maxMembers;
    }

    @Override
    public String toString() {
        return "GuildResponse{" +
                "mfaLevel=" + mfaLevel +
                ", premiumTier=" + premiumTier +
                ", premiumSubcriptionsCount=" + premiumSubcriptionsCount +
                ", preferredLocale=" + preferredLocale +
                ", emojis=" + Arrays.toString(emojis) +
                ", applicationId=" + applicationId +
                ", name='" + name + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", afkTimeout=" + afkTimeout +
                ", systemChannelId=" + systemChannelId +
                ", widgetChannelId=" + widgetChannelId +
                ", region='" + region + '\'' +
                ", defaultMessageNotifications=" + defaultMessageNotifications +
                ", embedChannelId=" + embedChannelId +
                ", explicitContentFilter=" + explicitContentFilter +
                ", splash='" + splash + '\'' +
                ", banner='" + banner + '\'' +
                ", features=" + Arrays.toString(features) +
                ", afkChannelId=" + afkChannelId +
                ", widgetEnabled=" + widgetEnabled +
                ", verificationLevel=" + verificationLevel +
                ", ownerId=" + ownerId +
                ", embedEnabled=" + embedEnabled +
                ", id=" + id +
                ", icon='" + icon + '\'' +
                ", vanityUrlCode=" + vanityUrlCode +
                ", description=" + description +
                ", maxPresences=" + maxPresences +
                ", maxMembers=" + maxMembers +
                '}';
    }
}
