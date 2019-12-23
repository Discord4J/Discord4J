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
package discord4j.gateway.json.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.GuildEmojiResponse;
import discord4j.common.json.RoleResponse;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

public class GuildUpdate implements Dispatch {

    @JsonProperty("widget_enabled")
    @Nullable
    private Boolean widgetEnabled;
    @JsonProperty("widget_channel_id")
    @Nullable
    @UnsignedJson
    private Long widgetChannelId;
    @JsonProperty("verification_level")
    private int verificationLevel;
    @JsonProperty("premium_tier")
    private int premiumTier;
    @JsonProperty("premium_subscription_count")
    private int premiumSubcriptionsCount;
    @JsonProperty("preferred_locale")
    private String preferredLocale;
    @JsonProperty("system_channel_id")
    @Nullable
    @UnsignedJson
    private Long systemChannelId;
    @JsonProperty("system_channel_flags")
    private int systemChannelFlags;
    @JsonProperty("rules_channel_id")
    @Nullable
    @UnsignedJson
    private Long rulesChannelId;
    private String splash;
    @JsonProperty("discovery_splash")
    @Nullable
    private String discoverySplash;
    private String banner;
    private RoleResponse[] roles;
    private String region;
    @JsonProperty("owner_id")
    @UnsignedJson
    private long ownerId;
    private String name;
    @JsonProperty("mfa_level")
    private int mfaLevel;
    @UnsignedJson
    private long id;
    private String icon;
    private String[] features;
    @JsonProperty("explicit_content_filter")
    private int explicitContentFilter;
    private GuildEmojiResponse[] emojis;
    @JsonProperty("embed_enabled")
    private boolean embedEnabled;
    @JsonProperty("embed_channel_id")
    @Nullable
    @UnsignedJson
    private Long embedChannelId;
    @JsonProperty("default_message_notifications")
    private int defaultMessageNotifications;
    @JsonProperty("application_id")
    @Nullable
    @UnsignedJson
    private Long applicationId;
    @JsonProperty("afk_timeout")
    private int afkTimeout;
    @JsonProperty("afk_channel_id")
    @Nullable
    @UnsignedJson
    private Long afkChannelId;
    @JsonProperty("guild_id")
    @UnsignedJson
    private long guildId;
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

    @Nullable
    public Boolean isWidgetEnabled() {
        return widgetEnabled;
    }

    @Nullable
    public Long getWidgetChannelId() {
        return widgetChannelId;
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

    @Nullable
    public Long getSystemChannelId() {
        return systemChannelId;
    }

    public int getSystemChannelFlags() {
        return systemChannelFlags;
    }

    @Nullable
    public Long getRulesChannelId() {
        return rulesChannelId;
    }

    public String getSplash() {
        return splash;
    }

    @Nullable
    public String getDiscoverySplash() {
        return discoverySplash;
    }

    public String getBanner() {
        return banner;
    }

    public RoleResponse[] getRoles() {
        return roles;
    }

    public String getRegion() {
        return region;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public int getMfaLevel() {
        return mfaLevel;
    }

    public long getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public String[] getFeatures() {
        return features;
    }

    public int getExplicitContentFilter() {
        return explicitContentFilter;
    }

    public GuildEmojiResponse[] getEmojis() {
        return emojis;
    }

    public boolean isEmbedEnabled() {
        return embedEnabled;
    }

    @Nullable
    public Long getEmbedChannelId() {
        return embedChannelId;
    }

    public int getDefaultMessageNotifications() {
        return defaultMessageNotifications;
    }

    @Nullable
    public Long getApplicationId() {
        return applicationId;
    }

    public int getAfkTimeout() {
        return afkTimeout;
    }

    @Nullable
    public Long getAfkChannelId() {
        return afkChannelId;
    }

    public long getGuildId() {
        return guildId;
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
        return "GuildUpdate{" +
                "widgetEnabled=" + widgetEnabled +
                ", widgetChannelId=" + widgetChannelId +
                ", verificationLevel=" + verificationLevel +
                ", premiumTier=" + premiumTier +
                ", premiumSubcriptionsCount=" + premiumSubcriptionsCount +
                ", preferredLocale='" + preferredLocale + '\'' +
                ", systemChannelId=" + systemChannelId +
                ", systemChannelFlags=" + systemChannelFlags +
                ", rulesChannelId=" + rulesChannelId +
                ", splash='" + splash + '\'' +
                ", discoverySplash='" + discoverySplash + '\'' +
                ", banner='" + banner + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", region='" + region + '\'' +
                ", ownerId=" + ownerId +
                ", name='" + name + '\'' +
                ", mfaLevel=" + mfaLevel +
                ", id=" + id +
                ", icon='" + icon + '\'' +
                ", features=" + Arrays.toString(features) +
                ", explicitContentFilter=" + explicitContentFilter +
                ", emojis=" + Arrays.toString(emojis) +
                ", embedEnabled=" + embedEnabled +
                ", embedChannelId=" + embedChannelId +
                ", defaultMessageNotifications=" + defaultMessageNotifications +
                ", applicationId=" + applicationId +
                ", afkTimeout=" + afkTimeout +
                ", afkChannelId=" + afkChannelId +
                ", guildId=" + guildId +
                ", vanityUrlCode='" + vanityUrlCode + '\'' +
                ", description='" + description + '\'' +
                ", maxPresences=" + maxPresences +
                ", maxMembers=" + maxMembers +
                '}';
    }
}
