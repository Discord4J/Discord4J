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
import java.util.OptionalLong;

public class AuditLogEntryResponse {

	@JsonProperty("target_id")
	@UnsignedJson
	private OptionalLong targetId;
	@Nullable
	private AuditLogChangeResponse[] changes;
	@JsonProperty("user_id")
	@UnsignedJson
	private long userId;
	@UnsignedJson
	private long id;
	@JsonProperty("action_type")
	private int actionType;
	@Nullable
	private AuditLogEntryOptionsResponse options;
	@Nullable
	private String reason;

	public OptionalLong getTargetId() {
		return targetId;
	}

	@Nullable
	public AuditLogChangeResponse[] getChanges() {
		return changes;
	}

	public long getUserId() {
		return userId;
	}

	public long getId() {
		return id;
	}

	public int getActionType() {
		return actionType;
	}

	@Nullable
	public AuditLogEntryOptionsResponse getOptions() {
		return options;
	}

	@Nullable
	public String getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return "AuditLogEntryResponse[" +
				"targetId=" + targetId +
				", changes=" + Arrays.toString(changes) +
				", userId=" + userId +
				", id=" + id +
				", actionType=" + actionType +
				", options=" + options +
				", reason=" + reason +
				']';
	}
}
