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
import discord4j.common.jackson.PossibleJson;
import discord4j.common.jackson.PossibleLong;
import discord4j.common.jackson.UnsignedJson;

import javax.annotation.Nullable;

@PossibleJson
public class GuildModifyRequest {

    private final Possible<String> name;
    private final Possible<String> region;
    @JsonProperty("verification_level")
    private final Possible<Integer> verificationLevel;
    @JsonProperty("default_message_notifications")
    private final Possible<Integer> defaultMessageNotifications;
    @JsonProperty("afk_channel_id")
    @Nullable
    @UnsignedJson
    private final PossibleLong afkChannelId;
    @JsonProperty("afk_timeout")
    private final Possible<Integer> afkTimeout;
    @Nullable
    private final Possible<String> icon;
    @JsonProperty("owner_id")
    @UnsignedJson
    private final PossibleLong ownerId;
    @Nullable
    private final Possible<String> splash;

    public GuildModifyRequest(Possible<String> name, Possible<String> region, Possible<Integer> verificationLevel,
                              Possible<Integer> defaultMessageNotifications, @Nullable PossibleLong afkChannelId,
                              Possible<Integer> afkTimeout, @Nullable Possible<String> icon, PossibleLong ownerId,
                              @Nullable Possible<String> splash) {
        this.name = name;
        this.region = region;
        this.verificationLevel = verificationLevel;
        this.defaultMessageNotifications = defaultMessageNotifications;
        this.afkChannelId = afkChannelId;
        this.afkTimeout = afkTimeout;
        this.icon = icon;
        this.ownerId = ownerId;
        this.splash = splash;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Possible<String> name = Possible.absent();
        private Possible<String> region = Possible.absent();
        private Possible<Integer> verificationLevel = Possible.absent();
        private Possible<Integer> defaultMessageNotifications = Possible.absent();
        @Nullable
        private PossibleLong afkChannelId = PossibleLong.absent();
        private Possible<Integer> afkTimeout = Possible.absent();
        @Nullable
        private Possible<String> icon = Possible.absent();
        private PossibleLong ownerId = PossibleLong.absent();
        @Nullable
        private Possible<String> splash = Possible.absent();

        public Builder name(String name) {
            this.name = Possible.of(name);
            return this;
        }

        public Builder region(String region) {
            this.region = Possible.of(region);
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

        public Builder afkChannelId(@Nullable Long afkChannelId) {
            this.afkChannelId = afkChannelId == null ? null : PossibleLong.of(afkChannelId);
            return this;
        }

        public Builder afkTimeout(int afkTimeout) {
            this.afkTimeout = Possible.of(afkTimeout);
            return this;
        }

        public Builder icon(@Nullable String icon) {
            this.icon = icon == null ? null : Possible.of(icon);
            return this;
        }

        public Builder ownerId(long ownerId) {
            this.ownerId = PossibleLong.of(ownerId);
            return this;
        }

        public Builder splash(@Nullable String splash) {
            this.splash = splash == null ? null : Possible.of(splash);
            return this;
        }

        public GuildModifyRequest build() {
            return new GuildModifyRequest(name, region, verificationLevel, defaultMessageNotifications, afkChannelId,
                    afkTimeout, icon, ownerId, splash);
        }
    }

    @Override
    public String toString() {
        return "GuildModifyRequest{" +
                "name=" + name +
                ", region=" + region +
                ", verificationLevel=" + verificationLevel +
                ", defaultMessageNotifications=" + defaultMessageNotifications +
                ", afkChannelId=" + afkChannelId +
                ", afkTimeout=" + afkTimeout +
                ", icon=" + icon +
                ", ownerId=" + ownerId +
                ", splash=" + splash +
                '}';
    }
}
