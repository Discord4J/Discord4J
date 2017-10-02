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
import discord4j.common.json.OverwriteEntity;

import javax.annotation.Nullable;

public class ChannelModifyRequest {

	private final Possible<String> name;
	private final Possible<Integer> position;
	@Nullable
	private final Possible<String> topic;
	private final Possible<Integer> bitrate;
	@JsonProperty("user_limit")
	private final Possible<Integer> userLimit;
	@JsonProperty("permission_overwrites")
	private final Possible<OverwriteEntity> permissionOverwrites;
	@JsonProperty("parent_id")
	@Nullable
	private final Possible<String> parentId;

	public ChannelModifyRequest(Possible<String> name, Possible<Integer> position,
	                            @Nullable Possible<String> topic, Possible<Integer> bitrate,
	                            Possible<Integer> userLimit,
	                            Possible<OverwriteEntity> permissionOverwrites,
	                            @Nullable Possible<String> parentId) {
		this.name = name;
		this.position = position;
		this.topic = topic;
		this.bitrate = bitrate;
		this.userLimit = userLimit;
		this.permissionOverwrites = permissionOverwrites;
		this.parentId = parentId;
	}
}
