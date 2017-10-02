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

import javax.annotation.Nullable;

public class AuditLogEntryResponse {

	@JsonProperty("target_id")
	@Nullable
	private String targetId;
	private AuditLogChangeResponse[] changes;
	@JsonProperty("user_id")
	private String userId;
	private String id;
	@JsonProperty("action_type")
	private int actionType;
	@Nullable
	private AuditLogEntryOptionsResponse[] options;
	@Nullable
	private String reason;

	@Nullable
	public String getTargetId() {
		return targetId;
	}

	public AuditLogChangeResponse[] getChanges() {
		return changes;
	}

	public String getUserId() {
		return userId;
	}

	public String getId() {
		return id;
	}

	public int getActionType() {
		return actionType;
	}

	@Nullable
	public AuditLogEntryOptionsResponse[] getOptions() {
		return options;
	}

	@Nullable
	public String getReason() {
		return reason;
	}
}
