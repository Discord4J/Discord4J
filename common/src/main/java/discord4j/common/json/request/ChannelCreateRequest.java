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
package discord4j.common.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;
import discord4j.common.jackson.PossibleLong;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.OverwriteEntity;

import javax.annotation.Nullable;
import java.util.OptionalLong;

@PossibleJson
public class ChannelCreateRequest {

	private final String name;
	private final Possible<Integer> type;
	private final Possible<Integer> bitrate;
	@JsonProperty("user_limit")
	private final Possible<Integer> userLimit;
	@JsonProperty("permission_overwrites")
	private final Possible<OverwriteEntity[]> permissionOverwrites;
	@JsonProperty("parent_id")
	@Nullable
	@UnsignedJson
	private final PossibleLong parentId;
	private final Possible<Boolean> nsfw;

	public ChannelCreateRequest(String name, Possible<Integer> type,
	                            Possible<Integer> bitrate, Possible<Integer> userLimit,
	                            Possible<OverwriteEntity[]> permissionOverwrites,
	                            @Nullable PossibleLong parentId,
	                            Possible<Boolean> nsfw) {
		this.name = name;
		this.type = type;
		this.bitrate = bitrate;
		this.userLimit = userLimit;
		this.permissionOverwrites = permissionOverwrites;
		this.parentId = parentId;
		this.nsfw = nsfw;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String name;
		private Possible<Integer> type = Possible.absent();
		private Possible<Integer> bitrate = Possible.absent();
		private Possible<Integer> userLimit = Possible.absent();
		private Possible<OverwriteEntity[]> permissionOverwrites = Possible.absent();
		private PossibleLong parentId = PossibleLong.absent();
		private Possible<Boolean> nsfw = Possible.absent();

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder type(int type) {
			this.type = Possible.of(type);
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

		public Builder parentId(OptionalLong parentId) {
			this.parentId = parentId.isPresent() ? PossibleLong.of(parentId.getAsLong()) : null;
			return this;
		}

		public Builder nsfw(boolean nsfw) {
			this.nsfw = Possible.of(nsfw);
			return this;
		}

		public ChannelCreateRequest build() {
			return new ChannelCreateRequest(name, type, bitrate, userLimit, permissionOverwrites, parentId, nsfw);
		}
	}

	@Override
	public String toString() {
		return "ChannelCreateRequest[" +
				"name=" + name +
				", type=" + type +
				", bitrate=" + bitrate +
				", userLimit=" + userLimit +
				", permissionOverwrites=" + permissionOverwrites +
				", parentId=" + parentId +
				", nsfw=" + nsfw +
				']';
	}
}
