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
package discord4j.common.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;

import javax.annotation.Nullable;
import java.util.Arrays;

public class GuildResponse {

    @UnsignedJson
    private long id;
    private String name;
    @Nullable
    private String icon;
    @Nullable
    private String splash;
    @JsonProperty("owner_id")
    @UnsignedJson
    private long ownerId;
    private String region;
    @JsonProperty("afk_channel_id")
    @Nullable
    @UnsignedJson
    private Long afkChannelId;
    @JsonProperty("afk_timeout")
    private int afkTimeout;
    @JsonProperty("embed_enabled")
    private boolean embedEnabled;
    @JsonProperty("embed_channel_id")
    @Nullable
    @UnsignedJson
    private Long embedChannelId;
    @JsonProperty("verification_level")
    private int verificationLevel;
    @JsonProperty("default_message_notifications")
    private int defaultMessageNotifications;
    @JsonProperty("explicit_content_filter")
    private int explciitContentFilter;
    private RoleResponse[] roles;
    private GuildEmojiResponse[] emojis;
    private String[] features;
    @JsonProperty("mfa_level")
    private int mfaLevel;
    @JsonProperty("application_id")
    @Nullable
    @UnsignedJson
    private Long applicationId;
    @JsonProperty("widget_enabled")
    private boolean widgetEnabled;
    @JsonProperty("widget_channel_id")
    @Nullable
    @UnsignedJson
    private Long widgetChannelId;
    @JsonProperty("joined_at")
    @Nullable
    private String joinedAt;
    @Nullable
    private Boolean large;
    @Nullable
    private Boolean unavailable;
    @JsonProperty("member_count")
    @Nullable
    private Integer memberCount;
    @JsonProperty("voice_states")
    @Nullable
    private VoiceStateResponse[] voiceStates;
    @Nullable
    private GuildMemberResponse[] members;
    @Nullable
    private ChannelResponse[] channels;
    @Nullable
    private PresenceResponse[] presences;
    @JsonProperty("system_channel_id")
    @Nullable
    @UnsignedJson
    private Long systemChannelId;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getIcon() {
        return icon;
    }

    @Nullable
    public String getSplash() {
        return splash;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getRegion() {
        return region;
    }

    @Nullable
    public Long getAfkChannelId() {
        return afkChannelId;
    }

    public int getAfkTimeout() {
        return afkTimeout;
    }

    public boolean isEmbedEnabled() {
        return embedEnabled;
    }

    @Nullable
    public Long getEmbedChannelId() {
        return embedChannelId;
    }

    public int getVerificationLevel() {
        return verificationLevel;
    }

    public int getDefaultMessageNotifications() {
        return defaultMessageNotifications;
    }

    public int getExplciitContentFilter() {
        return explciitContentFilter;
    }

    public RoleResponse[] getRoles() {
        return roles;
    }

    public GuildEmojiResponse[] getEmojis() {
        return emojis;
    }

    public String[] getFeatures() {
        return features;
    }

    public int getMfaLevel() {
        return mfaLevel;
    }

    @Nullable
    public Long getApplicationId() {
        return applicationId;
    }

    public boolean isWidgetEnabled() {
        return widgetEnabled;
    }

    @Nullable
    public Long getWidgetChannelId() {
        return widgetChannelId;
    }

    @Nullable
    public String getJoinedAt() {
        return joinedAt;
    }

    @Nullable
    public Boolean getLarge() {
        return large;
    }

    @Nullable
    public Boolean getUnavailable() {
        return unavailable;
    }

    @Nullable
    public Integer getMemberCount() {
        return memberCount;
    }

    @Nullable
    public VoiceStateResponse[] getVoiceStates() {
        return voiceStates;
    }

    @Nullable
    public GuildMemberResponse[] getMembers() {
        return members;
    }

    @Nullable
    public ChannelResponse[] getChannels() {
        return channels;
    }

    @Nullable
    public PresenceResponse[] getPresences() {
        return presences;
    }

    @Nullable
    public Long getSystemChannelId() {
        return systemChannelId;
    }

    @Override
    public String toString() {
        return "GuildResponse[" +
                "id=" + id +
                ", name=" + name +
                ", icon=" + icon +
                ", splash=" + splash +
                ", ownerId=" + ownerId +
                ", region=" + region +
                ", afkChannelId=" + afkChannelId +
                ", afkTimeout=" + afkTimeout +
                ", embedEnabled=" + embedEnabled +
                ", embedChannelId=" + embedChannelId +
                ", verificationLevel=" + verificationLevel +
                ", defaultMessageNotifications=" + defaultMessageNotifications +
                ", explciitContentFilter=" + explciitContentFilter +
                ", roles=" + Arrays.toString(roles) +
                ", emojis=" + Arrays.toString(emojis) +
                ", features=" + Arrays.toString(features) +
                ", mfaLevel=" + mfaLevel +
                ", applicationId=" + applicationId +
                ", widgetEnabled=" + widgetEnabled +
                ", widgetChannelId=" + widgetChannelId +
                ", joinedAt=" + joinedAt +
                ", large=" + large +
                ", unavailable=" + unavailable +
                ", memberCount=" + memberCount +
                ", voiceStates=" + Arrays.toString(voiceStates) +
                ", members=" + Arrays.toString(members) +
                ", channels=" + Arrays.toString(channels) +
                ", presences=" + Arrays.toString(presences) +
                ", systemChannelId=" + systemChannelId +
                ']';
    }
}
