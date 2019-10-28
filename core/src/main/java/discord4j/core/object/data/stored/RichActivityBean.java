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
import discord4j.common.json.EmojiResponse;
import discord4j.gateway.json.response.GameAssetsResponse;
import discord4j.gateway.json.response.GamePartyResponse;
import discord4j.gateway.json.response.GameResponse;
import discord4j.gateway.json.response.GameTimestampsResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public final class RichActivityBean extends ActivityBean implements Serializable {

    private static final long serialVersionUID = 8799038480262188518L;

    @Nullable
    private Long start;
    @Nullable
    private Long end;
    @Nullable
    private String sessionId;
    @Nullable
    private Long applicationId;
    @Nullable
    private String details;
    @Nullable
    private String syncId;
    @Nullable
    private String state;
    @Nullable
    private EmojiResponse emoji;
    @Nullable
    private Integer flags;
    @Nullable
    private String partyId;
    @Nullable
    private Long currentPartySize;
    @Nullable
    private Long maxPartySize;
    @Nullable
    private String largeImage;
    @Nullable
    private String largeText;
    @Nullable
    private String smallImage;
    @Nullable
    private String smallText;

    public RichActivityBean(final GameResponse response) {
        super(response);

        final GameTimestampsResponse timestamps = response.getTimestamps();
        start = (timestamps == null) ? null : timestamps.getStart();
        end = (timestamps == null) ? null : timestamps.getEnd();
        sessionId = response.getSessionId();
        applicationId = response.getApplicationId();
        details = response.getDetails();
        syncId = response.getSyncId();
        state = response.getState();
        emoji = response.getEmoji();
        flags = response.getFlags();
        final GamePartyResponse party = response.getParty();
        partyId = (party == null) ? null : party.getId();
        final long[] size = (party == null) ? null : party.getSize();
        currentPartySize = (size == null) ? null : size[0];
        maxPartySize = (size == null) ? null : size[1];
        final GameAssetsResponse assets = response.getAssets();
        largeImage = (assets == null) ? null : assets.getLargeImage();
        largeText = (assets == null) ? null : assets.getLargeText();
        smallImage = (assets == null) ? null : assets.getSmallImage();
        smallText = (assets == null) ? null : assets.getSmallText();
    }

    public RichActivityBean() {}

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Nullable
    public Long getStart() {
        return start;
    }

    public void setStart(@Nullable Long start) {
        this.start = start;
    }

    @Nullable
    public Long getEnd() {
        return end;
    }

    public void setEnd(@Nullable Long end) {
        this.end = end;
    }

    @Nullable
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(@Nullable String sessionId) {
        this.sessionId = sessionId;
    }

    @Nullable
    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(@Nullable Long applicationId) {
        this.applicationId = applicationId;
    }

    @Nullable
    public String getDetails() {
        return details;
    }

    public void setDetails(@Nullable String details) {
        this.details = details;
    }

    @Nullable
    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(@Nullable String syncId) {
        this.syncId = syncId;
    }

    @Nullable
    public String getState() {
        return state;
    }

    public void setState(@Nullable String state) {
        this.state = state;
    }

    @Nullable
    public EmojiResponse getEmoji() {
        return emoji;
    }

    public void setState(@Nullable EmojiResponse emoji) {
        this.emoji = emoji;
    }

    @Nullable
    public Integer getFlags() {
        return flags;
    }

    public void setFlags(@Nullable Integer flags) {
        this.flags = flags;
    }

    @Nullable
    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(@Nullable String partyId) {
        this.partyId = partyId;
    }

    @Nullable
    public Long getCurrentPartySize() {
        return currentPartySize;
    }

    public void setCurrentPartySize(@Nullable Long currentPartySize) {
        this.currentPartySize = currentPartySize;
    }

    @Nullable
    public Long getMaxPartySize() {
        return maxPartySize;
    }

    public void setMaxPartySize(@Nullable Long maxPartySize) {
        this.maxPartySize = maxPartySize;
    }

    @Nullable
    public String getLargeImage() {
        return largeImage;
    }

    public void setLargeImage(@Nullable String largeImage) {
        this.largeImage = largeImage;
    }

    @Nullable
    public String getLargeText() {
        return largeText;
    }

    public void setLargeText(@Nullable String largeText) {
        this.largeText = largeText;
    }

    @Nullable
    public String getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(@Nullable String smallImage) {
        this.smallImage = smallImage;
    }

    @Nullable
    public String getSmallText() {
        return smallText;
    }

    public void setSmallText(@Nullable String smallText) {
        this.smallText = smallText;
    }

    @Override
    public String toString() {
        return "RichActivityBean{" +
                "start=" + start +
                ", end=" + end +
                ", sessionId='" + sessionId + '\'' +
                ", applicationId=" + applicationId +
                ", details='" + details + '\'' +
                ", syncId='" + syncId + '\'' +
                ", state='" + state + '\'' +
                ", emoji='" + state + '\'' +
                ", flags=" + flags +
                ", partyId='" + partyId + '\'' +
                ", currentPartySize=" + currentPartySize +
                ", maxPartySize=" + maxPartySize +
                ", largeImage='" + largeImage + '\'' +
                ", largeText='" + largeText + '\'' +
                ", smallImage='" + smallImage + '\'' +
                ", smallText='" + smallText + '\'' +
                "} " + super.toString();
    }
}
