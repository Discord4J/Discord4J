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
package discord4j.rest.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

public class GuildCreateRequest {

    private final String name;
    private final String region;
    @Nullable
    private final String icon;
    @JsonProperty("verification_level")
    private final int verificationLevel;
    @JsonProperty("default_message_notifications")
    private final int defaultMessageNotifications;
    private final RoleCreateRequest[] roles;
    private final PartialChannelRequest[] channels;

    public GuildCreateRequest(String name, String region, @Nullable String icon, int verificationLevel,
                              int defaultMessageNotifications, RoleCreateRequest[] roles,
                              PartialChannelRequest[] channels) {
        this.name = name;
        this.region = region;
        this.icon = icon;
        this.verificationLevel = verificationLevel;
        this.defaultMessageNotifications = defaultMessageNotifications;
        this.roles = roles;
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "GuildCreateRequest{" +
                "name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", icon='" + icon + '\'' +
                ", verificationLevel=" + verificationLevel +
                ", defaultMessageNotifications=" + defaultMessageNotifications +
                ", roles=" + Arrays.toString(roles) +
                ", channels=" + Arrays.toString(channels) +
                '}';
    }
}
