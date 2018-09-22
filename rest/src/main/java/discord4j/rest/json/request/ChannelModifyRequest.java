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
import discord4j.common.json.OverwriteEntity;

import javax.annotation.Nullable;

@PossibleJson
public class ChannelModifyRequest {

    private final Possible<String> name;
    private final Possible<Integer> position;
    @Nullable
    private final Possible<String> topic;
    private final Possible<Boolean> nsfw;
    private final Possible<Integer> bitrate;
    @JsonProperty("user_limit")
    private final Possible<Integer> userLimit;
    @JsonProperty("permission_overwrites")
    private final Possible<OverwriteEntity[]> permissionOverwrites;
    @JsonProperty("parent_id")
    @Nullable
    @UnsignedJson
    private final PossibleLong parentId;

    public ChannelModifyRequest(Possible<String> name, Possible<Integer> position,
                                @Nullable Possible<String> topic, Possible<Boolean> nsfw, Possible<Integer> bitrate,
                                Possible<Integer> userLimit, Possible<OverwriteEntity[]> permissionOverwrites,
                                @Nullable PossibleLong parentId) {
        this.name = name;
        this.position = position;
        this.topic = topic;
        this.nsfw = nsfw;
        this.bitrate = bitrate;
        this.userLimit = userLimit;
        this.permissionOverwrites = permissionOverwrites;
        this.parentId = parentId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Possible<String> name = Possible.absent();
        private Possible<Integer> position = Possible.absent();
        @Nullable
        private Possible<String> topic = Possible.absent();
        private Possible<Boolean> nsfw = Possible.absent();
        private Possible<Integer> bitrate = Possible.absent();
        private Possible<Integer> userLimit = Possible.absent();
        private Possible<OverwriteEntity[]> permissionOverwrites = Possible.absent();
        @Nullable
        private PossibleLong parentId = PossibleLong.absent();

        public Builder name(String name) {
            this.name = Possible.of(name);
            return this;
        }

        public Builder position(int position) {
            this.position = Possible.of(position);
            return this;
        }

        public Builder topic(@Nullable String topic) {
            this.topic = topic == null ? null : Possible.of(topic);
            return this;
        }

        public Builder nsfw(boolean nsfw) {
            this.nsfw = Possible.of(nsfw);
            return this;
        }

        public Builder bitrate(int bitrate) {
            this.bitrate = Possible.of(bitrate);
            return this;
        }

        public Builder userLimit(int userLimit) {
            this.userLimit = Possible.of(userLimit);
            return this;
        }

        public Builder permissionOverwrites(OverwriteEntity[] permissionOverwrites) {
            this.permissionOverwrites = Possible.of(permissionOverwrites);
            return this;
        }

        public Builder parentId(@Nullable Long parentId) {
            this.parentId = parentId == null ? null : PossibleLong.of(parentId);
            return this;
        }

        public ChannelModifyRequest build() {
            return new ChannelModifyRequest(name, position, topic, nsfw, bitrate, userLimit, permissionOverwrites,
                    parentId);
        }

    }

    @Override
    public String toString() {
        return "ChannelModifyRequest{" +
                "name=" + name +
                ", position=" + position +
                ", topic=" + topic +
                ", nsfw=" + nsfw +
                ", bitrate=" + bitrate +
                ", userLimit=" + userLimit +
                ", permissionOverwrites=" + permissionOverwrites +
                ", parentId=" + parentId +
                '}';
    }
}
