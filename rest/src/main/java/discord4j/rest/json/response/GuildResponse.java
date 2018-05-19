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

import javax.annotation.Nullable;
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
    private String[] features;
    @JsonProperty("afk_channel_id")
    @Nullable
    @UnsignedJson
    private Long afkChannelId;
    @JsonProperty("widget_enabled")
    private boolean widgetEnabled;
    @JsonProperty("verification_level")
    private int verificationLevel;
    @JsonProperty("owner_id")
    @UnsignedJson
    private long ownerId;
    @JsonProperty("embed_enabled")
    private boolean embedEnabled;
    @UnsignedJson
    private long id;
    private String icon;

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

    public String[] getFeatures() {
        return features;
    }

    @Nullable
    public Long getAfkChannelId() {
        return afkChannelId;
    }

    public boolean isWidgetEnabled() {
        return widgetEnabled;
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

    @Override
    public String toString() {
        return "GuildResponse{" +
                "mfaLevel=" + mfaLevel +
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
                ", features=" + Arrays.toString(features) +
                ", afkChannelId=" + afkChannelId +
                ", widgetEnabled=" + widgetEnabled +
                ", verificationLevel=" + verificationLevel +
                ", ownerId=" + ownerId +
                ", embedEnabled=" + embedEnabled +
                ", id=" + id +
                ", icon='" + icon + '\'' +
                '}';
    }
}