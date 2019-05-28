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
import discord4j.gateway.json.response.ActivityResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

public class ActivityBean implements Serializable {

    private String name;
    private int type;
    @Nullable
    private String url;
    @Nullable
    private Integer start;
    @Nullable
    private Integer end;
    @Nullable
    private Long applicationId;
    @Nullable
    private String details;
    @Nullable
    private String state;
    @Nullable
    private String partyId;
    @Nullable
    private Integer currentPartySize;
    @Nullable
    private Integer maxPartySize;
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
    private boolean instance;
    @Nullable
    private Integer flags;

    public ActivityBean(final ActivityResponse response) {
        this.name = response.getName();
        this.type = response.getType();
        this.url = Possible.orElse(response.getUrl());

        ActivityResponse.Timestamps timestamps = Possible.orElse(response.getTimestamps());
        if (timestamps == null) {
            this.start = null;
            this.end = null;
        } else {
            this.start = Possible.orElse(timestamps.getStart());
            this.end = Possible.orElse(timestamps.getEnd());
        }

        this.applicationId = response.getApplicationId().isAbsent()  ? null : response.getApplicationId().get();
        this.details = Possible.orElse(response.getDetails());
        this.state = Possible.orElse(response.getState());

        ActivityResponse.Party party = Possible.orElse(response.getParty());
        if (party == null) {
            this.partyId = null;
            this.currentPartySize = null;
            this.maxPartySize = null;
        } else {
            this.partyId = Possible.orElse(party.getId());
            int[] size = Possible.orElse(party.getSize());
            if (size == null) {
                this.currentPartySize = null;
                this.maxPartySize = null;
            } else {
                this.currentPartySize = size[0];
                this.maxPartySize = size[1];
            }
        }

        ActivityResponse.Assets assets = Possible.orElse(response.getAssets());
        if (assets == null) {
            this.largeImage = null;
            this.largeText = null;
            this.smallImage = null;
            this.smallText = null;
        } else {
            this.largeImage = Possible.orElse(assets.getLargeImage());
            this.largeText = Possible.orElse(assets.getLargeText());
            this.smallImage = Possible.orElse(assets.getSmallImage());
            this.smallText = Possible.orElse(assets.getSmallText());
        }

        ActivityResponse.Secrets secrets = Possible.orElse(response.getSecrets());
        if (secrets == null) {
            this.joinSecret = null;
            this.spectateSecret = null;
            this.matchSecret = null;
        } else {
            this.joinSecret = Possible.orElse(secrets.getJoin());
            this.spectateSecret = Possible.orElse(secrets.getSpectate());
            this.matchSecret = Possible.orElse(secrets.getMatch());
        }

        this.instance = response.getInstance().isAbsent() ? false : response.getInstance().get();
        this.flags = Possible.orElse(response.getFlags());
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
    public Integer getStart() {
        return start;
    }

    public void setStart(@Nullable Integer start) {
        this.start = start;
    }

    @Nullable
    public Integer getEnd() {
        return end;
    }

    public void setEnd(@Nullable Integer end) {
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
    public Integer getCurrentPartySize() {
        return currentPartySize;
    }

    public void setCurrentPartySize(@Nullable Integer currentPartySize) {
        this.currentPartySize = currentPartySize;
    }

    @Nullable
    public Integer getMaxPartySize() {
        return maxPartySize;
    }

    public void setMaxPartySize(@Nullable Integer maxPartySize) {
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

    public boolean getInstance() {
        return instance;
    }

    public void setInstance(boolean instance) {
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
            ", instance=" + instance +
            ", flags=" + flags +
            '}';
    }
}
