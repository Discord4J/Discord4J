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

import javax.annotation.Nullable;
import java.util.Arrays;

public class ApplicationInfoResponse {

	@UnsignedJson
	private long id;
	private String name;
	@Nullable
	private String icon;
	@Nullable
	private String description;
	@JsonProperty("rpc_origins")
	@Nullable
	private String[] rpcOrigins;
	@JsonProperty("bot_public")
	private boolean botPublic;
	@JsonProperty("bot_require_code_grant")
	private boolean botRequireCodeGrant;
	private UserResponse owner;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Nullable
	public String getIcon() {
		return icon;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	@Nullable
	public String[] getRpcOrigins() {
		return rpcOrigins;
	}

	public boolean isBotPublic() {
		return botPublic;
	}

	public boolean isBotRequireCodeGrant() {
		return botRequireCodeGrant;
	}

	public UserResponse getOwner() {
		return owner;
	}

	@Override
	public String toString() {
		return "ApplicationInfoResponse{" +
				"id=" + id +
				", name='" + name + '\'' +
				", icon='" + icon + '\'' +
				", description='" + description + '\'' +
				", rpcOrigins=" + Arrays.toString(rpcOrigins) +
				", botPublic=" + botPublic +
				", botRequireCodeGrant=" + botRequireCodeGrant +
				", owner=" + owner +
				'}';
	}
}
