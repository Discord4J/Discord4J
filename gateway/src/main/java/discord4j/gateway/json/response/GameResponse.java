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
import discord4j.common.json.EmojiResponse;
import reactor.util.annotation.Nullable;

public class GameResponse {

    private String name;
    private int type;
    @Nullable
    private String url;
    @Nullable
    private GameTimestampsResponse timestamps;
    @JsonProperty("session_id")
    @Nullable
    private String sessionId;
    @JsonProperty("application_id")
    @Nullable
    @UnsignedJson
    private Long applicationId;
    @Nullable
    private String details;
    @JsonProperty("sync_id")
    @Nullable
    private String syncId;
    @Nullable
    private EmojiResponse emoji;
    @Nullable
    private String state;
    @Nullable
    private Integer flags;
    @Nullable
    private GamePartyResponse party;
    @Nullable
    private GameAssetsResponse assets;

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public GameTimestampsResponse getTimestamps() {
        return timestamps;
    }

    @Nullable
    public String getSessionId() {
        return sessionId;
    }

    @Nullable
    public Long getApplicationId() {
        return applicationId;
    }

    @Nullable
    public String getDetails() {
        return details;
    }

    @Nullable
    public String getSyncId() {
        return syncId;
    }

    @Nullable
    public String getState() {
        return state;
    }

    @Nullable
    public EmojiResponse getEmoji() {
        return emoji;
    }

    @Nullable
    public Integer getFlags() {
        return flags;
    }

    @Nullable
    public GamePartyResponse getParty() {
        return party;
    }

    @Nullable
    public GameAssetsResponse getAssets() {
        return assets;
    }

    @Override
    public String toString() {
        return "GameResponse{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", timestamps=" + timestamps +
                ", sessionId='" + sessionId + '\'' +
                ", applicationId=" + applicationId +
                ", details='" + details + '\'' +
                ", syncId='" + syncId + '\'' +
                ", state='" + state + '\'' +
                ", state='" + emoji + '\'' +
                ", emoji='" + emoji + '\'' +
                ", flags=" + flags +
                ", party=" + party +
                ", assets=" + assets +
                '}';
    }
}
