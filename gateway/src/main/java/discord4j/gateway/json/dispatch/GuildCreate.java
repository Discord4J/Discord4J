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
import discord4j.common.json.GuildMemberResponse;
import discord4j.common.json.RoleResponse;
import discord4j.gateway.json.response.GameResponse;
import discord4j.gateway.json.response.GatewayChannelResponse;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

public class GuildCreate implements Dispatch {

    @JsonProperty("voice_states")
    private VoiceState[] voiceStates;
    @JsonProperty("verification_level")
    private int verificationLevel;
    private boolean unavailable;
    @JsonProperty("rules_channel_id")
    @Nullable
    @UnsignedJson
    private Long rulesChannelId;
    @JsonProperty("public_updates_channel_id")
    @Nullable
    @UnsignedJson
    private Long publicUpdatesChannelId;
    @JsonProperty("system_channel_id")
    @Nullable
    @UnsignedJson
    private Long systemChannelId;
    private String splash;
    private String banner;
    private RoleResponse[] roles;
    private String region;
    private Presence[] presences;
    @JsonProperty("owner_id")
    @UnsignedJson
    private long ownerId;
    private String name;
    @JsonProperty("mfa_level")
    private int mfaLevel;
    @JsonProperty("premium_tier")
    private int premiumTier;
    @JsonProperty("premium_subscription_count")
    @Nullable
    private Integer premiumSubcriptionsCount;
    @JsonProperty("preferred_locale")
    private String preferredLocale;
    private GuildMemberResponse[] members;
    @JsonProperty("member_count")
    private int memberCount;
    private boolean lazy;
    private boolean large;
    @JsonProperty("joined_at")
    private String joinedAt;
    @UnsignedJson
    private long id;
    private String icon;
    private String[] features;
    @JsonProperty("explicit_content_filter")
    private int explicitContentFilter;
    private GuildEmojiResponse[] emojis;
    @JsonProperty("default_message_notifications")
    private int defaultMessageNotifications;
    private GatewayChannelResponse[] channels;
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
    @JsonProperty("embed_channel_id")
    @Nullable
    @UnsignedJson
    private Long embedChannelId;
    @JsonProperty("widget_channel_id")
    @Nullable
    @UnsignedJson
    private Long widgetChannelId;
    @JsonProperty("widget_enabled")
    @Nullable
    private Boolean widgetEnabled;
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

    public VoiceState[] getVoiceStates() {
        return voiceStates;
    }

    public int getPremiumTier() {
        return premiumTier;
    }

    @Nullable
    public Integer getPremiumSubcriptionsCount() {
        return premiumSubcriptionsCount;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }

    public int getVerificationLevel() {
        return verificationLevel;
    }

    public boolean isUnavailable() {
        return unavailable;
    }

    @Nullable
    public Long getRulesChannelId() {
        return rulesChannelId;
    }

    @Nullable
    public Long getPublicUpdatesChannelId() {
        return publicUpdatesChannelId;
    }

    @Nullable
    public Long getSystemChannelId() {
        return systemChannelId;
    }

    public String getSplash() {
        return splash;
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

    public Presence[] getPresences() {
        return presences;
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

    public GuildMemberResponse[] getMembers() {
        return members;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public boolean isLazy() {
        return lazy;
    }

    public boolean isLarge() {
        return large;
    }

    public String getJoinedAt() {
        return joinedAt;
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

    public int getDefaultMessageNotifications() {
        return defaultMessageNotifications;
    }

    public GatewayChannelResponse[] getChannels() {
        return channels;
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

    @Nullable
    public Long getEmbedChannelId() {
        return embedChannelId;
    }

    @Nullable
    public Boolean isWidgetEnabled() {
        return widgetEnabled;
    }

    @Nullable
    public Long getWidgetChannelId() {
        return widgetChannelId;
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
        return "GuildCreate{" +
                "voiceStates=" + Arrays.toString(voiceStates) +
                ", verificationLevel=" + verificationLevel +
                ", premiumTier=" + premiumTier +
                ", premiumSubcriptionsCount=" + premiumSubcriptionsCount +
                ", preferredLocale=" + preferredLocale +
                ", unavailable=" + unavailable +
                ", systemChannelId=" + systemChannelId +
                ", splash='" + splash + '\'' +
                ", banner='" + banner + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", region='" + region + '\'' +
                ", presences=" + Arrays.toString(presences) +
                ", ownerId=" + ownerId +
                ", name='" + name + '\'' +
                ", mfaLevel=" + mfaLevel +
                ", members=" + Arrays.toString(members) +
                ", memberCount=" + memberCount +
                ", lazy=" + lazy +
                ", large=" + large +
                ", joinedAt='" + joinedAt + '\'' +
                ", id=" + id +
                ", icon='" + icon + '\'' +
                ", features=" + Arrays.toString(features) +
                ", explicitContentFilter=" + explicitContentFilter +
                ", emojis=" + Arrays.toString(emojis) +
                ", defaultMessageNotifications=" + defaultMessageNotifications +
                ", channels=" + Arrays.toString(channels) +
                ", applicationId=" + applicationId +
                ", afkTimeout=" + afkTimeout +
                ", afkChannelId=" + afkChannelId +
                ", embedChannelId=" + embedChannelId +
                ", widgetEnabled=" + widgetEnabled +
                ", widgetChannelId=" + widgetChannelId +
                ", vanityUrlCode=" + vanityUrlCode +
                ", description=" + description +
                ", maxPresences=" + maxPresences +
                ", maxMembers=" + maxMembers +
                '}';
    }

    public static class VoiceState {

        @JsonProperty("channel_id")
        @UnsignedJson
        private long channelId;
        @JsonProperty("user_id")
        @UnsignedJson
        private long userId;
        @JsonProperty("session_id")
        private String sessionId;
        private boolean deaf;
        private boolean mute;
        @JsonProperty("self_deaf")
        private boolean selfDeaf;
        @JsonProperty("self_mute")
        private boolean selfMute;
        @JsonProperty("self_stream")
        @Nullable
        private Boolean selfStream;
        private boolean suppress;

        public long getChannelId() {
            return channelId;
        }

        public long getUserId() {
            return userId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public boolean isDeaf() {
            return deaf;
        }

        public boolean isMute() {
            return mute;
        }

        public boolean isSelfDeaf() {
            return selfDeaf;
        }

        public boolean isSelfMute() {
            return selfMute;
        }

        @Nullable
        public Boolean isSelfStream() {
            return selfStream;
        }

        public boolean isSuppress() {
            return suppress;
        }

        @Override
        public String toString() {
            return "VoiceState{" +
                    "channelId=" + channelId +
                    ", userId=" + userId +
                    ", sessionId='" + sessionId + '\'' +
                    ", deaf=" + deaf +
                    ", mute=" + mute +
                    ", selfDeaf=" + selfDeaf +
                    ", selfMute=" + selfMute +
                    ", selfStream=" + selfStream +
                    ", suppress=" + suppress +
                    '}';
        }
    }

    public static class Presence {

        private User user;
        private String status;
        private GameResponse game;

        public static class User {
            @UnsignedJson
            private long id;

            public long getId() {
                return id;
            }

            @Override
            public String toString() {
                return "User{" +
                        "id=" + id +
                        '}';
            }
        }

        public User getUser() {
            return user;
        }

        public String getStatus() {
            return status;
        }

        public GameResponse getGame() {
            return game;
        }

        @Override
        public String toString() {
            return "Presence{" +
                    "user=" + user +
                    ", status='" + status + '\'' +
                    ", game=" + game +
                    '}';
        }
    }
}
