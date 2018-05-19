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
package discord4j.gateway.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;

import javax.annotation.Nullable;

public class VoiceStateResponse {

    @JsonProperty("guild_id")
    @UnsignedJson
    private long guildId;
    @JsonProperty("channel_id")
    @Nullable
    @UnsignedJson
    private Long channelId;
    @JsonProperty("user_id")
    @UnsignedJson
    private long userId;
    @JsonProperty("session_id")
    private String sessionId;
    private boolean deaf;
    private boolean mute;
    @JsonProperty("self_deaf")
    private boolean selfDeaf;
    @JsonProperty("self_video")
    private boolean selfVideo;
    @JsonProperty("self_mute")
    private boolean selfMute;
    private boolean suppress;

    public long getGuildId() {
        return guildId;
    }

    @Nullable
    public Long getChannelId() {
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

    public boolean isSelfVideo() {
        return selfVideo;
    }

    public boolean isSelfMute() {
        return selfMute;
    }

    public boolean isSuppress() {
        return suppress;
    }

    @Override
    public String toString() {
        return "VoiceStateResponse[" +
                "guildId=" + guildId +
                ", channelId=" + channelId +
                ", userId=" + userId +
                ", sessionId=" + sessionId +
                ", deaf=" + deaf +
                ", mute=" + mute +
                ", selfDeaf=" + selfDeaf +
                ", selfVideo=" + selfVideo +
                ", selfMute=" + selfMute +
                ", suppress=" + suppress +
                ']';
    }
}
