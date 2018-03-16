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

public class IntegrationResponse {

    @UnsignedJson
    private long id;
    private String name;
    private String type;
    private boolean enabled;
    private boolean syncing;
    @JsonProperty("role_id")
    @UnsignedJson
    private long roleId;
    @JsonProperty("expire_behavior")
    private int expireBehavior;
    @JsonProperty("expire_grace_period")
    private int expireGracePeriod;
    private UserResponse user;
    private IntegrationAccountResponse account;
    @JsonProperty("synced_at")
    private String syncedAt;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSyncing() {
        return syncing;
    }

    public long getRoleId() {
        return roleId;
    }

    public int getExpireBehavior() {
        return expireBehavior;
    }

    public int getExpireGracePeriod() {
        return expireGracePeriod;
    }

    public UserResponse getUser() {
        return user;
    }

    public IntegrationAccountResponse getAccount() {
        return account;
    }

    public String getSyncedAt() {
        return syncedAt;
    }

    @Override
    public String toString() {
        return "IntegrationResponse[" +
                "id=" + id +
                ", name=" + name +
                ", type=" + type +
                ", enabled=" + enabled +
                ", syncing=" + syncing +
                ", roleId=" + roleId +
                ", expireBehavior=" + expireBehavior +
                ", expireGracePeriod=" + expireGracePeriod +
                ", user=" + user +
                ", account=" + account +
                ", syncedAt=" + syncedAt +
                ']';
    }
}
