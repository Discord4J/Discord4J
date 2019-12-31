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

import discord4j.common.jackson.Possible;
import discord4j.common.json.EmojiResponse;
import discord4j.gateway.json.response.ActivityResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

public class ActivityBean implements Serializable {

    private String name;
    private int type;
    @Nullable
    private String url;
    @Nullable
    private Long start;
    @Nullable
    private Long end;
    @Nullable
    private Long applicationId;
    @Nullable
    private String details;
    @Nullable
    private String state;
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
    @Nullable
    private String joinSecret;
    @Nullable
    private String spectateSecret;
    @Nullable
    private String matchSecret;
    @Nullable
    private EmojiResponse emoji;
    @Nullable
    private Boolean instance;
    @Nullable
    private Integer flags;

    public ActivityBean(final ActivityResponse response) {
        this.name = response.getName();
        this.type = response.getType();
        this.url = Possible.orElseNull(response.getUrl());

        ActivityResponse.Timestamps timestamps = Possible.orElseNull(response.getTimestamps());
        if (timestamps == null) {
            this.start = null;
            this.end = null;
        } else {
            this.start = Possible.orElseNull(timestamps.getStart());
            this.end = Possible.orElseNull(timestamps.getEnd());
        }

        this.applicationId = response.getApplicationId().isAbsent()  ? null : response.getApplicationId().get();
        this.details = Possible.orElseNull(response.getDetails());
        this.state = Possible.orElseNull(response.getState());

        ActivityResponse.Party party = Possible.orElseNull(response.getParty());
        if (party == null) {
            this.partyId = null;
            this.currentPartySize = null;
            this.maxPartySize = null;
        } else {
            this.partyId = Possible.orElseNull(party.getId());
            long[] size = Possible.orElseNull(party.getSize());
            if (size == null) {
                this.currentPartySize = null;
                this.maxPartySize = null;
            } else {
                this.currentPartySize = size[0];
                this.maxPartySize = size[1];
            }
        }

        ActivityResponse.Assets assets = Possible.orElseNull(response.getAssets());
        if (assets == null) {
            this.largeImage = null;
            this.largeText = null;
            this.smallImage = null;
            this.smallText = null;
        } else {
            this.largeImage = Possible.orElseNull(assets.getLargeImage());
            this.largeText = Possible.orElseNull(assets.getLargeText());
            this.smallImage = Possible.orElseNull(assets.getSmallImage());
            this.smallText = Possible.orElseNull(assets.getSmallText());
        }

        ActivityResponse.Secrets secrets = Possible.orElseNull(response.getSecrets());
        if (secrets == null) {
            this.joinSecret = null;
            this.spectateSecret = null;
            this.matchSecret = null;
        } else {
            this.joinSecret = Possible.orElseNull(secrets.getJoin());
            this.spectateSecret = Possible.orElseNull(secrets.getSpectate());
            this.matchSecret = Possible.orElseNull(secrets.getMatch());
        }

        this.emoji = Possible.orElseNull(response.getEmoji());
        this.instance = response.getInstance().isAbsent() ? false : response.getInstance().get();
        this.flags = Possible.orElseNull(response.getFlags());
    }

    public ActivityBean(int type, String name, @Nullable String url) {
        this.type = type;
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(@Nullable String url) {
        this.url = url;
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
    public String getState() {
        return state;
    }

    public void setState(@Nullable String state) {
        this.state = state;
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

    @Nullable
    public String getJoinSecret() {
        return joinSecret;
    }

    public void setJoinSecret(@Nullable String joinSecret) {
        this.joinSecret = joinSecret;
    }

    @Nullable
    public String getSpectateSecret() {
        return spectateSecret;
    }

    public void setSpectateSecret(@Nullable String spectateSecret) {
        this.spectateSecret = spectateSecret;
    }

    @Nullable
    public String getMatchSecret() {
        return matchSecret;
    }

    public void setMatchSecret(@Nullable String matchSecret) {
        this.matchSecret = matchSecret;
    }

    @Nullable
    public EmojiResponse getEmoji() {
        return emoji;
    }

    public void setEmoji(@Nullable EmojiResponse emoji) {
        this.emoji = emoji;
    }

    @Nullable
    public Boolean getInstance() {
        return instance;
    }

    public void setInstance(@Nullable Boolean instance) {
        this.instance = instance;
    }

    @Nullable
    public Integer getFlags() {
        return flags;
    }

    public void setFlags(@Nullable Integer flags) {
        this.flags = flags;
    }

    public ActivityBean() {}

    @Override
    public String toString() {
        return "ActivityBean{" +
            "name='" + name + '\'' +
            ", type=" + type +
            ", url='" + url + '\'' +
            ", start=" + start +
            ", end=" + end +
            ", applicationId=" + applicationId +
            ", details='" + details + '\'' +
            ", state='" + state + '\'' +
            ", partyId='" + partyId + '\'' +
            ", currentPartySize=" + currentPartySize +
            ", maxPartySize=" + maxPartySize +
            ", largeImage='" + largeImage + '\'' +
            ", largeText='" + largeText + '\'' +
            ", smallImage='" + smallImage + '\'' +
            ", smallText='" + smallText + '\'' +
            ", joinSecret='" + joinSecret + '\'' +
            ", spectateSecret='" + spectateSecret + '\'' +
            ", matchSecret='" + matchSecret + '\'' +
            ", emoji=" + emoji +
            ", instance=" + instance +
            ", flags=" + flags +
            '}';
    }
}
