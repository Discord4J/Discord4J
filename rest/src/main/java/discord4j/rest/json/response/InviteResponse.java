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
import discord4j.common.json.UserResponse;
import reactor.util.annotation.Nullable;

public class InviteResponse {

    private String code;
    private GuildResponse guild;
    private ChannelResponse channel;
    @JsonProperty("target_user")
    @Nullable
    private UserResponse targetUser;
    @JsonProperty("approximate_presence_count")
    @Nullable
    private Integer approximatePresenceCount;
    @JsonProperty("approximate_member_count")
    @Nullable
    private Integer approximateMemberCount;
    @Nullable
    private UserResponse inviter;
    @Nullable
    private Integer uses;
    @JsonProperty("max_uses")
    @Nullable
    private Integer maxUses;
    @JsonProperty("max_age")
    @Nullable
    private Integer maxAge;
    @Nullable
    private Boolean temporary;
    @JsonProperty("created_at")
    @Nullable
    private String createdAt;
    @JsonProperty("revoked")
    @Nullable
    private Boolean revoked;

    public String getCode() {
        return code;
    }

    public GuildResponse getGuild() {
        return guild;
    }

    public ChannelResponse getChannel() {
        return channel;
    }

    @Nullable
    public UserResponse getTargetUser() {
        return targetUser;
    }

    @Nullable
    public Integer getApproximatePresenceCount() {
        return approximatePresenceCount;
    }

    @Nullable
    public Integer getApproximateMemberCount() {
        return approximateMemberCount;
    }

    @Nullable
    public UserResponse getInviter() {
        return inviter;
    }

    @Nullable
    public Integer getUses() {
        return uses;
    }

    @Nullable
    public Integer getMaxUses() {
        return maxUses;
    }

    @Nullable
    public Integer getMaxAge() {
        return maxAge;
    }

    @Nullable
    public Boolean getTemporary() {
        return temporary;
    }

    @Nullable
    public String getCreatedAt() {
        return createdAt;
    }

    @Nullable
    public Boolean getRevoked() {
        return revoked;
    }

    @Override
    public String toString() {
        return "InviteResponse{" +
                "code='" + code + '\'' +
                ", guild=" + guild +
                ", channel=" + channel +
                ", targetUser=" + targetUser +
                ", approximatePresenceCount=" + approximatePresenceCount +
                ", approximateMemberCount=" + approximateMemberCount +
                ", inviter=" + inviter +
                ", uses=" + uses +
                ", maxUses=" + maxUses +
                ", maxAge=" + maxAge +
                ", temporary=" + temporary +
                ", createdAt='" + createdAt + '\'' +
                ", revoked=" + revoked +
                '}';
    }
}
