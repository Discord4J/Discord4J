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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import discord4j.gateway.json.dispatch.GuildCreate;
import discord4j.gateway.json.response.VoiceStateResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public final class VoiceStateBean implements Serializable {

    private static final long serialVersionUID = 7194969399964938104L;

    private long guildId;
    @Nullable
    private Long channelId;
    private long userId;
    private String sessionId;
    private boolean deaf;
    private boolean mute;
    private boolean selfDeaf;
    private boolean selfMute;
    @Nullable
    private Boolean selfStream;
    private boolean suppress;

    public VoiceStateBean(final GuildCreate.VoiceState voiceState, final long guildId) {
        this.guildId = guildId;
        channelId = voiceState.getChannelId();
        userId = voiceState.getUserId();
        sessionId = voiceState.getSessionId();
        deaf = voiceState.isDeaf();
        mute = voiceState.isMute();
        selfDeaf = voiceState.isSelfDeaf();
        selfMute = voiceState.isSelfMute();
        selfStream = voiceState.isSelfStream();
        suppress = voiceState.isSuppress();
    }

    public VoiceStateBean(final VoiceStateResponse response) {
        this.guildId = response.getGuildId();
        channelId = response.getChannelId();
        userId = response.getUserId();
        sessionId = response.getSessionId();
        deaf = response.isDeaf();
        mute = response.isMute();
        selfDeaf = response.isSelfDeaf();
        selfMute = response.isSelfMute();
        selfStream = response.isSelfStream();
        suppress = response.isSuppress();
    }

    public VoiceStateBean() {}

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(final long guildId) {
        this.guildId = guildId;
    }

    @Nullable
    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(@Nullable final Long channelId) {
        this.channelId = channelId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isDeaf() {
        return deaf;
    }

    public void setDeaf(final boolean deaf) {
        this.deaf = deaf;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(final boolean mute) {
        this.mute = mute;
    }

    public boolean isSelfDeaf() {
        return selfDeaf;
    }

    public void setSelfDeaf(final boolean selfDeaf) {
        this.selfDeaf = selfDeaf;
    }

    public boolean isSelfMute() {
        return selfMute;
    }

    public void setSelfMute(final boolean selfMute) {
        this.selfMute = selfMute;
    }

    @Nullable
    public Boolean isSelfStream() {
        return selfStream;
    }

    public void setSelfStream(@Nullable final Boolean selfStream) {
        this.selfStream = selfStream;
    }

    public boolean isSuppress() {
        return suppress;
    }

    public void setSuppress(final boolean suppress) {
        this.suppress = suppress;
    }

    @Override
    public String toString() {
        return "VoiceStateBean{" +
                "guildId=" + guildId +
                ", channelId=" + channelId +
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
