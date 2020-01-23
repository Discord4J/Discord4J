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
import discord4j.common.jackson.Possible;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

public class GuildCreateRequest {

    private final String name;
    private final Possible<String> region;
    @Nullable
    private final Possible<String> icon;
    @JsonProperty("verification_level")
    private final Possible<Integer> verificationLevel;
    @JsonProperty("default_message_notifications")
    private final Possible<Integer> defaultMessageNotifications;
    private final Possible<RoleCreateRequest[]> roles;
    private final Possible<PartialChannelRequest[]> channels;

    public GuildCreateRequest(String name, Possible<String> region, Possible<String> icon,
                              Possible<Integer> verificationLevel, Possible<Integer> defaultMessageNotifications,
                              Possible<RoleCreateRequest[]> roles, Possible<PartialChannelRequest[]> channels) {
        this.name = name;
        this.region = region;
        this.icon = icon;
        this.verificationLevel = verificationLevel;
        this.defaultMessageNotifications = defaultMessageNotifications;
        this.roles = roles;
        this.channels = channels;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private Possible<String> region = Possible.absent();
        @Nullable
        private Possible<String> icon = Possible.absent();
        private Possible<Integer> verificationLevel = Possible.absent();
        private Possible<Integer> defaultMessageNotifications = Possible.absent();
        private Possible<RoleCreateRequest[]> roles = Possible.absent();
        private Possible<PartialChannelRequest[]> channels = Possible.absent();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder region(String region) {
            this.region = Possible.of(region);
            return this;
        }

        public Builder icon(String icon) {
            this.icon = Possible.of(icon);
            return this;
        }

        public Builder verificationLevel(int verificationLevel) {
            this.verificationLevel = Possible.of(verificationLevel);
            return this;
        }

        public Builder defaultMessageNotifications(int defaultMessageNotifications) {
            this.defaultMessageNotifications = Possible.of(defaultMessageNotifications);
            return this;
        }

        public Builder roles(RoleCreateRequest[] roles) {
            this.roles = Possible.of(roles);
            return this;
        }

        public Builder channels(PartialChannelRequest[] channels) {
            this.channels = Possible.of(channels);
            return this;
        }

        public GuildCreateRequest build() {
            return new GuildCreateRequest(name, region, icon, verificationLevel, defaultMessageNotifications, roles,
                    channels);
        }
    }

    @Override
    public String toString() {
        return "GuildCreateRequest{" +
                "name='" + name + '\'' +
                ", region=" + region +
                ", icon=" + icon +
                ", verificationLevel=" + verificationLevel +
                ", defaultMessageNotifications=" + defaultMessageNotifications +
                ", roles=" + roles +
                ", channels=" + channels +
                '}';
    }
}
